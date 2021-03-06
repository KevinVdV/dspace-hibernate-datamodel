/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.dspace.app.util.AuthorizeUtil;
import org.dspace.authorize.*;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.authorize.service.ResourcePolicyService;
import org.dspace.content.authority.service.ChoiceAuthorityService;
import org.dspace.content.authority.service.MetadataAuthorityService;
import org.dspace.content.dao.ItemDAO;
import org.dspace.content.service.*;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.I18nUtil;
import org.dspace.core.LogManager;
import org.dspace.content.authority.Choices;
import org.dspace.event.Event;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.handle.service.HandleService;
import org.dspace.identifier.IdentifierException;
import org.dspace.identifier.service.IdentifierService;
import org.dspace.versioning.factory.DSpaceVersionServiceFactory;
import org.dspace.versioning.service.VersionService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class representing an item in DSpace.
 * <P>
 * This class holds in memory the item Dublin Core metadata, the bundles in the
 * item, and the bitstreams in those bundles. When modifying the item, if you
 * modify the Dublin Core or the "in archive" flag, you must call
 * <code>update</code> for the changes to be written to the database.
 * Creating, adding or removing bundles or bitstreams has immediate effect in
 * the database.
 *
 * @author Robert Tansley
 * @author Martin Hald
 * @version $Revision$
 */
public class ItemServiceImpl extends DSpaceObjectServiceImpl<Item> implements ItemService
{

    /** log4j category */
    protected static final Logger log = Logger.getLogger(Item.class);
    /** The bundles in this item - kept in sync with DB */

    @Autowired(required = true)
    protected ItemDAO itemDAO;

    @Autowired(required = true)
    protected MetadataSchemaService metadataSchemaService;
    @Autowired(required = true)
    protected MetadataValueService metadataValueService;
    @Autowired(required = true)
    protected MetadataFieldService metadataFieldService;
    @Autowired(required = true)
    protected CommunityService communityService;
    @Autowired(required = true)
    protected BundleService bundleService;
    @Autowired(required = true)
    protected BitstreamFormatService bitstreamFormatService;
    @Autowired(required = true)
    protected BitstreamService bitstreamService;
    @Autowired(required = true)
    protected HandleService handleService;
    @Autowired(required = true)
    protected InstallItemService installItemService;
    @Autowired(required = true)
    protected AuthorizeService authorizeService;
    @Autowired(required = true)
    protected ChoiceAuthorityService choiceAuthorityService;
    @Autowired(required = true)
    protected MetadataAuthorityService metadataAuthorityService;
    @Autowired(required = true)
    protected IdentifierService identifierService;
    @Autowired(required = true)
    protected ResourcePolicyService resourcePolicyService;

    public ItemServiceImpl()
    {
    }

    /**
     * Get an item from the database. The item, its Dublin Core metadata, and
     * the bundle and bitstream metadata are all loaded into memory.
     *
     * @param context
     *            DSpace context object
     * @param id
     *            Internal ID of the item
     * @return the item, or null if the internal ID is invalid.
     * @throws SQLException
     */
    @Override
    public Item find(Context context, UUID id) throws SQLException
    {
        Item item = itemDAO.findByID(context, Item.class, id);
        if (item == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug(LogManager.getHeader(context, "find_item",
                        "not_found,item_id=" + id));
            }
            return null;
        }

        // not null, return item
        if (log.isDebugEnabled())
        {
            log.debug(LogManager.getHeader(context, "find_item", "item_id="
                    + id));
        }

        return item;
    }

    @Override
    public String getName(Item item) {
        List<MetadataValue> titles = getMetadata(item, MetadataSchema.DC_SCHEMA, "title", null, Item.ANY);
        if(CollectionUtils.isEmpty(titles))
        {
            //TODO: Use other string that isn't dependent on WorkflowManager
            return I18nUtil.getMessage("org.dspace.workflow.WorkflowManager.untitled ");
        }else{
            return titles.iterator().next().getValue();
        }
    }

    /**
     * Create a new item, with a new internal ID. This method is not public,
     * since items need to be created as workspace items. Authorisation is the
     * responsibility of the caller.
     *
     * @param context
     *            DSpace context object
     * @return the newly created item
     * @throws SQLException
     * @throws AuthorizeException
     */
    @Override
    public Item create(Context context, WorkspaceItem workspaceItem) throws SQLException, AuthorizeException
    {
        if(workspaceItem.getItem() != null)
        {
            throw new IllegalArgumentException("Attempting to create an item for a workspace item that already contains an item");
        }

        Item item = createItem(context);
        workspaceItem.setItem(item);
        return item;
    }

    /**
     * Create an empty template item for this collection. If one already exists,
     * no action is taken. Caution: Make sure you call <code>update</code> on
     * the collection after doing this, or the item will have been created but
     * the collection record will not refer to it.
     *
     * @throws SQLException
     * @throws AuthorizeException
     */
    @Override
    public Item createTemplateItem(Context context, Collection collection) throws SQLException, AuthorizeException
    {
        if(collection == null || collection.getTemplateItem() != null)
        {
            throw new IllegalArgumentException("Collection is null or already contains template item.");
        }
        AuthorizeUtil.authorizeManageTemplateItem(context, collection);

        if (collection.getTemplateItem() == null) {
            Item template = createItem(context);
            collection.setTemplate(template);
            template.setTemplateItemOf(collection);

            log.info(LogManager.getHeader(context, "create_template_item",
                    "collection_id=" + collection.getID() + ",template_item_id="
                            + template.getID()));

            return template;
        }else{
            return collection.getTemplateItem();
        }
    }

    protected Item createItem(Context context) throws SQLException, AuthorizeException {
        Item item = itemDAO.create(context, new Item());
        // set discoverable to true (default)
        item.setDiscoverable(true);

        // Call update to give the item a last modified date. OK this isn't
        // amazingly efficient but creates don't happen that often.
        context.turnOffAuthorisationSystem();
        update(context, item);
        context.restoreAuthSystemState();

        context.addEvent(new Event(Event.CREATE, Constants.ITEM, item.getID(), null));

        log.info(LogManager.getHeader(context, "create_item", "item_id=" + item.getID()));

        return item;
    }

    /**
     * Get all the items in the archive. Only items with the "in archive" flag
     * set are included. The order of the list is indeterminate.
     *
     * @param context
     *            DSpace context object
     * @return an iterator over the items in the archive.
     * @throws SQLException
     */
    @Override
    public Iterator<Item> findAll(Context context) throws SQLException
    {
        return itemDAO.findAll(context, true);
    }
    
    /**
     * Get all "final" items in the archive, both archived ("in archive" flag) or
     * withdrawn items are included. The order of the list is indeterminate.
     *
     * @param context
     *            DSpace context object
     * @return an iterator over the items in the archive.
     * @throws SQLException
     */
	@Override
    public Iterator<Item> findAllUnfiltered(Context context) throws SQLException
    {
        return itemDAO.findAll(context, true, true);
	}

    /**
     * Find all the items in the archive by a given submitter. The order is
     * indeterminate. Only items with the "in archive" flag set are included.
     *
     * @param context
     *            DSpace context object
     * @param eperson
     *            the submitter
     * @return an iterator over the items submitted by eperson
     * @throws SQLException
     */
    @Override
    public Iterator<Item> findBySubmitter(Context context, EPerson eperson) throws SQLException
    {
        return itemDAO.findBySubmitter(context, eperson);
    }

    @Override
    public Iterator<Item> findArchivedItemsByCollection(Context context, Collection collection) throws SQLException {
        return findArchivedItemsByCollection(context, collection, null, null);
    }

    @Override
    public Iterator<Item> findArchivedItemsByCollection(Context context, Collection collection, Integer limit, Integer offset) throws SQLException {
        return itemDAO.findArchivedByCollection(context, collection, limit, offset);
    }

    @Override
    public Iterator<Item> findByCollection(Context context, Collection collection) throws SQLException {
        return itemDAO.findAllByCollection(context, collection);
    }

    @Override
    public List<MetadataValue> getMetadata(Item item, MetadataField metadataField, String lang)
    {
        return getMetadata(item, metadataField.getMetadataSchema().getName(), metadataField.getElement(), metadataField.getQualifier(), lang);
    }

    /**
     * Get metadata for the item in a chosen schema.
     * See <code>MetadataSchema</code> for more information about schemas.
     * Passing in a <code>null</code> value for <code>qualifier</code>
     * or <code>lang</code> only matches metadata fields where that
     * qualifier or languages is actually <code>null</code>.
     * Passing in <code>Item.ANY</code>
     * retrieves all metadata fields with any value for the qualifier or
     * language, including <code>null</code>
     * <P>
     * Examples:
     * <P>
     * Return values of the unqualified "title" field, in any language.
     * Qualified title fields (e.g. "title.uniform") are NOT returned:
     * <P>
     * <code>item.getMetadata("dc", "title", null, Item.ANY );</code>
     * <P>
     * Return all US English values of the "title" element, with any qualifier
     * (including unqualified):
     * <P>
     * <code>item.getMetadata("dc, "title", Item.ANY, "en_US" );</code>
     * <P>
     * The ordering of values of a particular element/qualifier/language
     * combination is significant. When retrieving with wildcards, values of a
     * particular element/qualifier/language combinations will be adjacent, but
     * the overall ordering of the combinations is indeterminate.
     *
     * @param schema
     *            the schema for the metadata field. <em>Must</em> match
     *            the <code>name</code> of an existing metadata schema.
     * @param element
     *            the element name. <code>Item.ANY</code> matches any
     *            element. <code>null</code> doesn't really make sense as all
     *            metadata must have an element.
     * @param qualifier
     *            the qualifier. <code>null</code> means unqualified, and
     *            <code>Item.ANY</code> means any qualifier (including
     *            unqualified.)
     * @param lang
     *            the ISO639 language code, optionally followed by an underscore
     *            and the ISO3166 country code. <code>null</code> means only
     *            values with no language are returned, and
     *            <code>Item.ANY</code> means values with any country code or
     *            no country code are returned.
     * @return metadata fields that match the parameters
     */
    @Override
    public List<MetadataValue> getMetadata(Item item, String schema, String element, String qualifier, String lang)
    {
        // Build up list of matching values
        List<MetadataValue> values = new ArrayList<MetadataValue>();
        for (MetadataValue dcv : item.getMetadata())
        {
            if (match(schema, element, qualifier, lang, dcv))
            {
                values.add(dcv);
            }
        }

        // Create an array of matching values
        return values;
    }
    
    /**
     * Retrieve metadata field values from a given metadata string
     * of the form <schema prefix>.<element>[.<qualifier>|.*]
     *
     * @param mdString
     *            The metadata string of the form
     *            <schema prefix>.<element>[.<qualifier>|.*]
     */
    @Override
    public List<MetadataValue> getMetadata(Item item, String mdString)
    {
        StringTokenizer dcf = new StringTokenizer(mdString, ".");

        String[] tokens = { "", "", "" };
        int i = 0;
        while(dcf.hasMoreTokens())
        {
            tokens[i] = dcf.nextToken().trim();
            i++;
        }
        String schema = tokens[0];
        String element = tokens[1];
        String qualifier = tokens[2];
        
        List<MetadataValue> values;
        if (Item.ANY.equals(qualifier))
        {
            values = getMetadata(item, schema, element, Item.ANY, Item.ANY);
        }
        else if ("".equals(qualifier))
        {
            values = getMetadata(item, schema, element, null, Item.ANY);
        }
        else
        {
            values = getMetadata(item, schema, element, qualifier, Item.ANY);
        }
        
        return values;
    }

    /**
     * Add metadata fields. These are appended to existing values.
     * Use <code>clearDC</code> to remove values. The ordering of values
     * passed in is maintained.
     * <p>
     * If metadata authority control is available, try to get authority
     * values.  The authority confidence depends on whether authority is
     * <em>required</em> or not.
     * @param schema
     *            the schema for the metadata field. <em>Must</em> match
     *            the <code>name</code> of an existing metadata schema.
     * @param element
     *            the metadata element name
     * @param qualifier
     *            the metadata qualifier name, or <code>null</code> for
     *            unqualified
     * @param lang
     *            the ISO639 language code, optionally followed by an underscore
     *            and the ISO3166 country code. <code>null</code> means the
     *            value has no language (for example, a date).
     * @param values
     *            the values to add.
     */
    @Override
    public void addMetadata(Context context, Item item, String schema, String element, String qualifier, String lang, List<String> values) throws SQLException {
        String fieldKey = metadataAuthorityService.makeFieldKey(schema, element, qualifier);
        if (metadataAuthorityService.isAuthorityControlled(fieldKey))
        {
            List<String> authorities = new ArrayList<String>();
            List<Integer> confidences = new ArrayList<Integer>();
            for (int i = 0; i < values.size(); ++i)
            {
                Collection owningCollection = item.getOwningCollection();
                Collection collection = null;
                if(owningCollection != null)
                {
                    collection = owningCollection;
                }
                Choices c = choiceAuthorityService.getBestMatch(fieldKey, values.get(i), collection, null);
                authorities.add(c.values.length > 0 ? c.values[0].authority : null);
                confidences.add(c.confidence);
            }
            addMetadata(context, item, schema, element, qualifier, lang, values, authorities, confidences);
        }
        else
        {
            addMetadata(context, item, schema, element, qualifier, lang, values, null, null);
        }
    }

    /**
     * Add metadata fields. These are appended to existing values.
     * Use <code>clearDC</code> to remove values. The ordering of values
     * passed in is maintained.
     * @param schema
     *            the schema for the metadata field. <em>Must</em> match
     *            the <code>name</code> of an existing metadata schema.
     * @param element
     *            the metadata element name
     * @param qualifier
     *            the metadata qualifier name, or <code>null</code> for
     *            unqualified
     * @param lang
     *            the ISO639 language code, optionally followed by an underscore
     *            and the ISO3166 country code. <code>null</code> means the
     *            value has no language (for example, a date).
     * @param values
     *            the values to add.
     * @param authorities
     *            the external authority key for this value (or null)
     * @param confidences
     *            the authority confidence (default 0)
     */
    @Override
    public void addMetadata(Context context, Item item, String schema, String element, String qualifier, String lang,
            List<String> values, List<String> authorities, List<Integer> confidences) throws SQLException {

        // We will not verify that they are valid entries in the registry
        // until update() is called.
        MetadataSchema metadataSchema = metadataSchemaService.find(context, schema);
        MetadataField metadataField = metadataFieldService.findByElement(context, metadataSchema, element, qualifier);
        if (metadataSchema == null || metadataField == null) {
            throw new SQLException("bad_dublin_core schema=" + schema + "." + element + "." + qualifier);
        }
        addMetadata(context, item, metadataField, lang, values, authorities, confidences);
    }

    @Override
    public void addMetadata(Context context, Item item, MetadataField metadataField, String lang, String value) throws SQLException
    {
        addMetadata(context, item, metadataField, lang, Arrays.asList(value), null, null);
    }

    @Override
    public void addMetadata(Context context, Item item, MetadataField metadataField, String lang, List<String> values, List<String> authorities, List<Integer> confidences) throws SQLException
    {
        for (int i = 0; i < values.size(); i++)
        {
            String value = values.get(i);
            if(metadataField == null)
            {
                throw new SQLException("Metadata field cannot be null");
            }
            if (value == null) {
                //Do not allow "null" values in our metadata
                return;
            }
            boolean authorityControlled = metadataAuthorityService.isAuthorityControlled(metadataField);
            boolean authorityRequired = metadataAuthorityService.isAuthorityRequired(metadataField);

            MetadataValue metadataValue = metadataValueService.create(context, item, metadataField);


            metadataValue.setLanguage(lang == null ? null : lang.trim());

            // Logic to set Authority and Confidence:
            //  - normalize an empty string for authority to NULL.
            //  - if authority key is present, use given confidence or NOVALUE if not given
            //  - otherwise, preserve confidence if meaningful value was given since it may document a failed authority lookup
            //  - CF_UNSET signifies no authority nor meaningful confidence.
            //  - it's possible to have empty authority & CF_ACCEPTED if e.g. user deletes authority key
            if (authorityControlled) {
                if (authorities != null && authorities.get(i) != null && authorities.get(i).length() > 0) {
                    metadataValue.setAuthority(authorities.get(i));
                    metadataValue.setConfidence(confidences == null ? Choices.CF_NOVALUE : confidences.get(i));
                } else {
                    metadataValue.setAuthority(null);
                    metadataValue.setConfidence(confidences == null ? Choices.CF_UNSET : confidences.get(i));
                }
                // authority sanity check: if authority is required, was it supplied?
                // XXX FIXME? can't throw a "real" exception here without changing all the callers to expect it, so use a runtime exception
                if (authorityRequired && (metadataValue.getAuthority() == null || metadataValue.getAuthority().length() == 0)) {
                    throw new IllegalArgumentException("The metadata field \"" + metadataValue.getMetadataField().toString() + "\" requires an authority key but none was provided. Vaue=\"" + metadataValue.getValue() + "\"");
                }
            }
            // remove control unicode char
            String temp = value.trim();
            char[] dcvalue = temp.toCharArray();
            for (int charPos = 0; charPos < dcvalue.length; charPos++) {
                if (Character.isISOControl(dcvalue[charPos]) &&
                        !String.valueOf(dcvalue[charPos]).equals("\u0009") &&
                        !String.valueOf(dcvalue[charPos]).equals("\n") &&
                        !String.valueOf(dcvalue[charPos]).equals("\r")) {
                    dcvalue[charPos] = ' ';
                }
            }
            metadataValue.setValue(String.valueOf(dcvalue));
            //Set the place to be the next place in the line
            metadataValue.setPlace(getMetadata(item, metadataField, Item.ANY).size() + 1);
            item.addMetadata(metadataValue);
            metadataValueService.update(context, metadataValue);
        }
    }


    /**
     * Add a single metadata field. This is appended to existing
     * values. Use <code>clearDC</code> to remove values.
     *
     * @param schema
     *            the schema for the metadata field. <em>Must</em> match
     *            the <code>name</code> of an existing metadata schema.
     * @param element
     *            the metadata element name
     * @param qualifier
     *            the metadata qualifier, or <code>null</code> for
     *            unqualified
     * @param lang
     *            the ISO639 language code, optionally followed by an underscore
     *            and the ISO3166 country code. <code>null</code> means the
     *            value has no language (for example, a date).
     * @param value
     *            the value to add.
     */
    @Override
    public void addMetadata(Context context, Item item, String schema, String element, String qualifier,
            String lang, String value) throws SQLException {
        addMetadata(context, item, schema, element, qualifier, lang, Arrays.asList(value));
    }

    /**
     * Add a single metadata field. This is appended to existing
     * values. Use <code>clearDC</code> to remove values.
     *
     * @param schema
     *            the schema for the metadata field. <em>Must</em> match
     *            the <code>name</code> of an existing metadata schema.
     * @param element
     *            the metadata element name
     * @param qualifier
     *            the metadata qualifier, or <code>null</code> for
     *            unqualified
     * @param lang
     *            the ISO639 language code, optionally followed by an underscore
     *            and the ISO3166 country code. <code>null</code> means the
     *            value has no language (for example, a date).
     * @param value
     *            the value to add.
     * @param authority
     *            the external authority key for this value (or null)
     * @param confidence
     *            the authority confidence (default 0)
     */
    @Override
    public void addMetadata(Context context, Item item, String schema, String element, String qualifier,
            String lang, String value, String authority, int confidence) throws SQLException {
        addMetadata(context, item, schema, element, qualifier, lang, Arrays.asList(value), Arrays.asList(authority), Arrays.asList(confidence));
    }

    /**
     * Clear metadata values. As with <code>getDC</code> above,
     * passing in <code>null</code> only matches fields where the qualifier or
     * language is actually <code>null</code>.<code>Item.ANY</code> will
     * match any element, qualifier or language, including <code>null</code>.
     * Thus, <code>item.clearDC(Item.ANY, Item.ANY, Item.ANY)</code> will
     * remove all Dublin Core metadata associated with an item.
     *
     * @param schema
     *            the schema for the metadata field. <em>Must</em> match
     *            the <code>name</code> of an existing metadata schema.
     * @param element
     *            the Dublin Core element to remove, or <code>Item.ANY</code>
     * @param qualifier
     *            the qualifier. <code>null</code> means unqualified, and
     *            <code>Item.ANY</code> means any qualifier (including
     *            unqualified.)
     * @param lang
     *            the ISO639 language code, optionally followed by an underscore
     *            and the ISO3166 country code. <code>null</code> means only
     *            values with no language are removed, and <code>Item.ANY</code>
     *            means values with any country code or no country code are
     *            removed.
     */
    @Override
    public void clearMetadata(Context context, Item item, String schema, String element, String qualifier,
            String lang) throws SQLException {
        Iterator<MetadataValue> metadata = item.getMetadata().iterator();
        while (metadata.hasNext()) {
            MetadataValue metadataValue = metadata.next();
            if (match(schema, element, qualifier, lang, metadataValue))
            {
                metadata.remove();
                metadataValueService.delete(context, metadataValue);
            }
        }
    }

    @Override
    public void removeMetadataValues(Context context, Item item, List<MetadataValue> values) throws SQLException {
        Iterator<MetadataValue> metadata = item.getMetadata().iterator();
        while (metadata.hasNext()) {
            MetadataValue metadataValue = metadata.next();
            if(values.contains(metadataValue))
            {
                metadata.remove();
                metadataValueService.delete(context, metadataValue);
            }
        }
    }

    /**
     * Utility method for pattern-matching metadata elements.  This
     * method will return <code>true</code> if the given schema,
     * element, qualifier and language match the schema, element,
     * qualifier and language of the <code>DCValue</code> object passed
     * in.  Any or all of the element, qualifier and language passed
     * in can be the <code>Item.ANY</code> wildcard.
     *
     * @param schema
     *            the schema for the metadata field. <em>Must</em> match
     *            the <code>name</code> of an existing metadata schema.
     * @param element
     *            the element to match, or <code>Item.ANY</code>
     * @param qualifier
     *            the qualifier to match, or <code>Item.ANY</code>
     * @param language
     *            the language to match, or <code>Item.ANY</code>
     * @param metadataValue
     *            the Dublin Core value
     * @return <code>true</code> if there is a match
     */
    protected boolean match(String schema, String element, String qualifier,
            String language, MetadataValue metadataValue)
    {

        MetadataField metadataField = metadataValue.getMetadataField();
        MetadataSchema metadataSchema = metadataField.getMetadataSchema();
        // We will attempt to disprove a match - if we can't we have a match
        if (!element.equals(Item.ANY) && !element.equals(metadataField.getElement()))
        {
            // Elements do not match, no wildcard
            return false;
        }

        if (qualifier == null)
        {
            // Value must be unqualified
            if (metadataField.getQualifier() != null)
            {
                // Value is qualified, so no match
                return false;
            }
        }
        else if (!qualifier.equals(Item.ANY))
        {
            // Not a wildcard, so qualifier must match exactly
            if (!qualifier.equals(metadataField.getQualifier()))
            {
                return false;
            }
        }

        if (language == null)
        {
            // Value must be null language to match
            if (metadataValue.getLanguage() != null)
            {
                // Value is qualified, so no match
                return false;
            }
        }
        else if (!language.equals(Item.ANY))
        {
            // Not a wildcard, so language must match exactly
            if (!language.equals(metadataValue.getLanguage()))
            {
                return false;
            }
        }

        if (!schema.equals(Item.ANY))
        {
            if (metadataSchema != null && !metadataSchema.getName().equals(schema))
            {
                // The namespace doesn't match
                return false;
            }
        }

        // If we get this far, we have a match
        return true;
    }


    /**
     * See whether this Item is contained by a given Collection.
     * @param collection
     * @return true if {@code collection} contains this Item.
     * @throws SQLException
     */
    @Override
    public boolean isIn(Item item, Collection collection) throws SQLException
    {
        List<Collection> collections = item.getCollections();
        return collections != null && collections.contains(collection);
    }

    /**
     * Get the communities this item is in. Returns an unordered array of the
     * communities that house the collections this item is in, including parent
     * communities of the owning collections.
     *
     * @return the communities this item is in.
     * @throws SQLException
     */
    @Override
    public List<Community> getCommunities(Context context, Item item) throws SQLException
    {
        List<Community> result = new ArrayList<Community>();
        List<Collection> collections = item.getCollections();
        for(Collection collection : collections)
        {
            List<Community> owningCommunities = collection.getCommunities();
            for (Community community : owningCommunities) {
                result.add(community);
                result.addAll(communityService.getAllParents(context, community));
            }
        }

        return result;
    }

    /**
     * Get the bundles matching a bundle name (name corresponds roughly to type)
     *
     * @param name
     *            name of bundle (ORIGINAL/TEXT/THUMBNAIL)
     *
     * @return the bundles in an unordered array
     */
    @Override
    public List<Bundle> getBundles(Item item, String name) throws SQLException
    {
        List<Bundle> matchingBundles = new ArrayList<Bundle>();
        // now only keep bundles with matching names
        List<Bundle> bunds = item.getBundles();
        for (Bundle bund : bunds) {
            if (name.equals(bund.getName())) {
                matchingBundles.add(bund);
            }
        }
        return matchingBundles;
    }

    /**
     * Add an existing bundle to this item. This has immediate effect.
     *
     * @param b
     *            the bundle to add
     * @throws SQLException
     * @throws AuthorizeException
     */
    @Override
    public void addBundle(Context context, Item item, Bundle b) throws SQLException, AuthorizeException
    {
        // Check authorisation
        authorizeService.authorizeAction(context, item, Constants.ADD);

        log.info(LogManager.getHeader(context, "add_bundle", "item_id="
                + item.getID() + ",bundle_id=" + b.getID()));

        // Check it's not already there
        if (item.getBundles().contains(b)) {
            // Bundle is already there; no change
            return;
        }

        // now add authorization policies from owning item
        // hmm, not very "multiple-inclusion" friendly
        authorizeService.inheritPolicies(context, item, b);

        // Add the bundle to in-memory list
        item.addBundle(b);
        b.setItems(Arrays.asList(item));

        // Insert the mapping
        context.addEvent(new Event(Event.ADD, Constants.ITEM, item.getID(), Constants.BUNDLE, b.getID(), b.getName()));
    }

    @Override
    public void removeAllBundles(Context context, Item item) throws AuthorizeException, SQLException, IOException {
        Iterator<Bundle> bundles = item.getBundles().iterator();
        while(bundles.hasNext())
        {
            Bundle bundle = bundles.next();
            bundles.remove();
            deleteBundle(context, item, bundle);
        }
    }

    /**
     * Remove a bundle. This may result in the bundle being deleted, if the
     * bundle is orphaned.
     *
     * @param b
     *            the bundle to remove
     * @throws SQLException
     * @throws AuthorizeException
     * @throws IOException
     */
    @Override
    public void removeBundle(Context context, Item item, Bundle b) throws SQLException, AuthorizeException, IOException {
        deleteBundle(context,  item, b);
        // We've found the bundle to remove
        item.removeBundle(b);
    }
     protected void deleteBundle(Context context, Item item, Bundle b) throws AuthorizeException, SQLException, IOException {
                          // Check authorisation
        authorizeService.authorizeAction(context, item, Constants.REMOVE);

        bundleService.delete(context, b);

        log.info(LogManager.getHeader(context, "remove_bundle", "item_id="
                + item.getID() + ",bundle_id=" + b.getID()));
        context.addEvent(new Event(Event.REMOVE, Constants.ITEM, item.getID(), Constants.BUNDLE, b.getID(), b.getName()));
    }

    /**
     * Create a single bitstream in a new bundle. Provided as a convenience
     * method for the most common use.
     *
     * @param is
     *            the stream to create the new bitstream from
     * @param name
     *            is the name of the bundle (ORIGINAL, TEXT, THUMBNAIL)
     * @return Bitstream that is created
     * @throws AuthorizeException
     * @throws IOException
     * @throws SQLException
     */
    @Override
    public Bitstream createSingleBitstream(Context context, Item item, InputStream is, String name)
            throws AuthorizeException, IOException, SQLException
    {
        // Authorisation is checked by methods below
        // Create a bundle
        Bundle bnd = bundleService.create(context, item, name);
        Bitstream bitstream = bitstreamService.create(context, bnd, is);
        addBundle(context, item, bnd);

        // FIXME: Create permissions for new bundle + bitstream
        return bitstream;
    }

    /**
     * Convenience method, calls createSingleBitstream() with name "ORIGINAL"
     *
     * @param is
     *            InputStream
     * @return created bitstream
     * @throws AuthorizeException
     * @throws IOException
     * @throws SQLException
     */
    @Override
    public Bitstream createSingleBitstream(Context context, Item item, InputStream is)
            throws AuthorizeException, IOException, SQLException
    {
        return createSingleBitstream(context, item, is, "ORIGINAL");
    }

    /**
     * Get all non-internal bitstreams in the item. This is mainly used for
     * auditing for provenance messages and adding format.* DC values. The order
     * is indeterminate.
     *
     * @return non-internal bitstreams.
     */
    @Override
    public List<Bitstream> getNonInternalBitstreams(Item item) throws SQLException
    {
        List<Bitstream> bitstreamList = new ArrayList<Bitstream>();

        // Go through the bundles and bitstreams picking out ones which aren't
        // of internal formats
        List<Bundle> bunds = item.getBundles();

        for (Bundle bund : bunds) {
            List<BundleBitstream> bitstreams = bund.getBitstreams();

            for (BundleBitstream bundleBitstream : bitstreams) {
                Bitstream bitstream = bundleBitstream.getBitstream();
                if (!bitstream.getFormat().isInternal()) {
                    // Bitstream is not of an internal format
                    bitstreamList.add(bitstream);
                }
            }
        }

        return bitstreamList;
    }

    /**
     * Remove just the DSpace license from an item This is useful to update the
     * current DSpace license, in case the user must accept the DSpace license
     * again (either the item was rejected, or resumed after saving)
     * <p>
     * This method is used by the org.dspace.submit.step.LicenseStep class
     *
     * @throws SQLException
     * @throws AuthorizeException
     * @throws IOException
     */
    @Override
    public void removeDSpaceLicense(Context context, Item item) throws SQLException, AuthorizeException,
            IOException
    {
        // get all bundles with name "LICENSE" (these are the DSpace license
        // bundles)
        List<Bundle> bunds = getBundles(item, "LICENSE");

        for (Bundle bund : bunds) {
            // FIXME: probably serious troubles with Authorizations
            // fix by telling system not to check authorization?
            removeBundle(context, item, bund);
        }
    }

    /**
     * Remove all licenses from an item - it was rejected
     *
     * @throws SQLException
     * @throws AuthorizeException
     * @throws IOException
     */
    @Override
    public void removeLicenses(Context context, Item item) throws SQLException, AuthorizeException, IOException
    {
        // Find the License format
        BitstreamFormat bf = bitstreamFormatService.findByShortDescription(context, "License");
        int licensetype = bf.getID();

        // search through bundles, looking for bitstream type license
        List<Bundle> bunds = item.getBundles();

        for (Bundle bund : bunds) {
            boolean removethisbundle = false;

            List<BundleBitstream> bits = bund.getBitstreams();

            for (BundleBitstream bit : bits) {
                BitstreamFormat bft = bit.getBitstream().getFormat();

                if (bft.getID() == licensetype) {
                    removethisbundle = true;
                }
            }

            // probably serious troubles with Authorizations
            // fix by telling system not to check authorization?
            if (removethisbundle) {
                removeBundle(context, item, bund);
            }
        }
    }

    /**
     * Method that updates the last modified date of the item
     */
    @Override
    public void updateLastModified(Context context, Item item) throws SQLException, AuthorizeException {
        item.setLastModified(new Date());
        update(context, item);
        //Also fire a modified event since the item HAS been modified
        context.addEvent(new Event(Event.MODIFY, Constants.ITEM, item.getID(), null));
    }

    /**
     * Update the item "in archive" flag and Dublin Core metadata in the
     * database
     *
     * @throws SQLException
     * @throws AuthorizeException
     */
    @Override
    public void update(Context context, Item item) throws SQLException, AuthorizeException
    {
        // Check authorisation
        // only do write authorization if user is not an editor
        if (!canEdit(context, item))
        {
            authorizeService.authorizeAction(context, item, Constants.WRITE);
        }

        log.info(LogManager.getHeader(context, "update_item", "item_id="
                + item.getID()));

        // Set sequence IDs for bitstreams in item
        int sequence = 0;
        List<Bundle> bunds = item.getBundles();

        // find the highest current sequence number
        for (Bundle bund : bunds) {
            List<BundleBitstream> streams = bund.getBitstreams();

            for (BundleBitstream bundleBitstream : streams) {
                Bitstream bitstream = bundleBitstream.getBitstream();
                if (bitstream.getSequenceID() > sequence) {
                    sequence = bitstream.getSequenceID();
                }
            }
        }

        // start sequencing bitstreams without sequence IDs
        sequence++;

        for (Bundle bund : bunds) {
            List<BundleBitstream> streams = bund.getBitstreams();

            for (BundleBitstream bundleBitstream : streams) {
                Bitstream stream = bundleBitstream.getBitstream();
                if (stream.getSequenceID() < 0) {
                    stream.setSequenceID(sequence);
                    sequence++;
                    bitstreamService.update(context, stream);
//                    modified = true;
                }
            }
        }

        // Map counting number of values for each element/qualifier.
        // Keys are Strings: "element" or "element.qualifier"
        // Values are Integers indicating number of values written for a
        // element/qualifier

        if (item.isMetadataModified() || item.isModified())
        {
            // Set the last modified date
            item.setLastModified(new Date());


            itemDAO.save(context, item);

            if (item.isMetadataModified())
            {
                context.addEvent(new Event(Event.MODIFY_METADATA, Constants.ITEM, item.getID(), item.getDetails()));
                item.clearDetails();
                item.setMetadataModified(false);
            }

            context.addEvent(new Event(Event.MODIFY, Constants.ITEM, item.getID(), null));
            item.setModified(false);
        }
    }


    /**
     * Withdraw the item from the archive. It is kept in place, and the content
     * and metadata are not deleted, but it is not publicly accessible.
     *
     * @throws SQLException
     * @throws AuthorizeException
     * @throws IOException
     */
    @Override
    public void withdraw(Context context, Item item) throws SQLException, AuthorizeException
    {
        // Check permission. User either has to have REMOVE on owning collection
        // or be COLLECTION_EDITOR of owning collection
        AuthorizeUtil.authorizeWithdrawItem(context, item);

        String timestamp = DCDate.getCurrent().toString();

        // Add suitable provenance - includes user, date, collections +
        // bitstream checksums
        EPerson e = context.getCurrentUser();

        // Build some provenance data while we're at it.
        StringBuilder prov = new StringBuilder();

        prov.append("Item withdrawn by ").append(e.getFullName()).append(" (")
                .append(e.getEmail()).append(") on ").append(timestamp).append("\n")
                .append("Item was in collections:\n");

        List<Collection> colls = item.getCollections();

        for (Collection coll : colls) {
            prov.append(coll.getName()).append(" (ID: ").append(coll.getID()).append(")\n");
        }

        // Set withdrawn flag. timestamp will be set; last_modified in update()
        item.setWithdrawn(true);

        // in_archive flag is now false
        item.setInArchive(false);

        prov.append(installItemService.getBitstreamProvenanceMessage(item));

        addMetadata(context, item, MetadataSchema.DC_SCHEMA, "description", "provenance", "en", prov.toString());

        // Update item in DB
        update(context, item);

        context.addEvent(new Event(Event.MODIFY, Constants.ITEM, item.getID(), "WITHDRAW"));

        // remove all authorization policies, saving the custom ones
        authorizeService.removeAllPoliciesByDsoAndTypeNotEqualsTo(context, item, ResourcePolicy.TYPE_CUSTOM);

        // Write log
        log.info(LogManager.getHeader(context, "withdraw_item", "user="
                + e.getEmail() + ",item_id=" + item.getID()));
    }

    /**
     * Reinstate a withdrawn item
     *
     * @throws SQLException
     * @throws AuthorizeException
     * @throws IOException
     */
    @Override
    public void reinstate(Context context, Item item) throws SQLException, AuthorizeException
    {
        // check authorization
        AuthorizeUtil.authorizeReinstateItem(context, item);

        String timestamp = DCDate.getCurrent().toString();

        // Check permission. User must have ADD on all collections.
        // Build some provenance data while we're at it.
        List<Collection> colls = item.getCollections();

        // Add suitable provenance - includes user, date, collections +
        // bitstream checksums
        EPerson e = context.getCurrentUser();
        StringBuilder prov = new StringBuilder();
        prov.append("Item reinstated by ").append(e.getFullName()).append(" (")
                .append(e.getEmail()).append(") on ").append(timestamp).append("\n")
                .append("Item was in collections:\n");

        for (Collection coll : colls) {
            prov.append(coll.getName()).append(" (ID: ").append(coll.getID()).append(")\n");
        }
        
        // Clear withdrawn flag
        item.setWithdrawn(false);

        // in_archive flag is now true
        item.setInArchive(true);

        // Add suitable provenance - includes user, date, collections +
        // bitstream checksums
        prov.append(installItemService.getBitstreamProvenanceMessage(item));

        addMetadata(context, item, MetadataSchema.DC_SCHEMA, "description", "provenance", "en", prov.toString());

        // Update item in DB
        update(context, item);

        context.addEvent(new Event(Event.MODIFY, Constants.ITEM, item.getID(), "REINSTATE"));

        // authorization policies
        if (colls.size() > 0)
        {
            // FIXME: not multiple inclusion friendly - just apply access
            // policies from first collection
            // remove the item's policies and replace them with
            // the defaults from the collection
            inheritCollectionDefaultPolicies(context, item, colls.iterator().next());
        }

        // Write log
        log.info(LogManager.getHeader(context, "reinstate_item", "user="
                + e.getEmail() + ",item_id=" + item.getID()));
    }

    /**
     * Delete an archived or withdrawn item
     * @param context
     * @param item
     */
    @Override
    public void delete(Context context, Item item) throws SQLException, IOException, AuthorizeException {
        //TODO: HIBERNATE CHECK COLLECTION REMOVE ?
        // Check authorisation here. If we don't, it may happen that we remove the
        // collections leaving the database in an inconsistent state
        authorizeService.authorizeAction(context, item, Constants.REMOVE);
        item.getCollections().clear();
        rawDelete(context,  item);
    }

    /**
     * Delete (expunge) the item. Bundles and bitstreams are also deleted if
     * they are not also included in another item. The Dublin Core metadata is
     * deleted.
     *
     * @throws SQLException
     * @throws AuthorizeException
     * @throws IOException
     */
    void rawDelete(Context context, Item item) throws SQLException, AuthorizeException, IOException
    {
        // Check authorisation here. If we don't, it may happen that we remove the
        // metadata but when getting to the point of removing the bundles we get an exception
        // leaving the database in an inconsistent state
        authorizeService.authorizeAction(context, item, Constants.REMOVE);

        context.addEvent(new Event(Event.DELETE, Constants.ITEM, item.getID(), item.getHandle(context)));

        log.info(LogManager.getHeader(context, "delete_item", "item_id="
                + item.getID()));

        // Remove bundles
        removeAllBundles(context, item);

        // remove all of our authorization policies
        authorizeService.removeAllPolicies(context, item);
        
        // Remove any Handle
        handleService.unbindHandle(context, item);
        
        // remove version attached to the item
        removeVersion(context, item);


        // Finally remove item row
        itemDAO.delete(context, item);
    }

    protected void removeVersion(Context context, Item item) throws AuthorizeException, SQLException
    {
        VersionService versionService = DSpaceVersionServiceFactory.getInstance().getVersionService();
        if(versionService.findByItem(context, item)!=null)
        {
            versionService.removeVersion(context, item);
        }else{
            try {
                identifierService.delete(context, item);
            } catch (IdentifierException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Return true if this Collection 'owns' this item
     *
     * @param c
     *            Collection
     * @return true if this Collection owns this item
     */
    @Override
    public boolean isOwningCollection(Item item, Collection c)
    {
        Collection collection = item.getOwningCollection();

        if (collection != null && c.getID() == collection.getID())
        {
            return true;
        }

        // not the owner
        return false;
    }

    /**
     * remove all of the policies for item and replace them with a new list of
     * policies
     *
     * @param newpolicies -
     *            this will be all of the new policies for the item and its
     *            contents
     * @throws SQLException
     * @throws AuthorizeException
     */
    @Override
    public void replaceAllItemPolicies(Context context, Item item, List<ResourcePolicy> newpolicies) throws SQLException,
            AuthorizeException
    {
        // remove all our policies, add new ones
        authorizeService.removeAllPolicies(context, item);
        authorizeService.addPolicies(context, newpolicies, item);
    }

    /**
     * remove all of the policies for item's bitstreams and bundles and replace
     * them with a new list of policies
     *
     * @param newpolicies -
     *            this will be all of the new policies for the bundle and
     *            bitstream contents
     * @throws SQLException
     * @throws AuthorizeException
     */
    @Override
    public void replaceAllBitstreamPolicies(Context context, Item item, List<ResourcePolicy> newpolicies)
            throws SQLException, AuthorizeException
    {
        // remove all policies from bundles, add new ones
        // Remove bundles
        List<Bundle> bunds = item.getBundles();

        for (Bundle mybundle : bunds) {
            bundleService.replaceAllBitstreamPolicies(context, mybundle, newpolicies);
        }
    }

    /**
     * remove all of the policies for item's bitstreams and bundles that belong
     * to a given Group
     *
     * @param g
     *            Group referenced by policies that needs to be removed
     * @throws SQLException
     */
    @Override
    public void removeGroupPolicies(Context context, Item item, Group g) throws SQLException, AuthorizeException {
        // remove Group's policies from Item
        authorizeService.removeGroupPolicies(context, item, g);

        // remove all policies from bundles
        List<Bundle> bunds = item.getBundles();

        for (Bundle mybundle : bunds) {
            List<BundleBitstream> bs = mybundle.getBitstreams();

            for (BundleBitstream b : bs) {
                Bitstream bitstream = b.getBitstream();
                // remove bitstream policies
                authorizeService.removeGroupPolicies(context, bitstream, g);
            }

            // change bundle policies
            authorizeService.removeGroupPolicies(context, mybundle, g);
        }
    }

    /**
     * remove all policies on an item and its contents, and replace them with
     * the DEFAULT_ITEM_READ and DEFAULT_BITSTREAM_READ policies belonging to
     * the collection.
     *
     * @param c
     *            Collection
     * @throws java.sql.SQLException
     *             if an SQL error or if no default policies found. It's a bit
     *             draconian, but default policies must be enforced.
     * @throws AuthorizeException
     */
    @Override
    public void inheritCollectionDefaultPolicies(Context context, Item item, Collection c)
            throws java.sql.SQLException, AuthorizeException
    {
        adjustItemPolicies(context, item, c);
        adjustBundleBitstreamPolicies(context, item, c);

        log.debug(LogManager.getHeader(context, "item_inheritCollectionDefaultPolicies",
                                                   "item_id=" + item.getID()));
    }

    @Override
    public void adjustBundleBitstreamPolicies(Context context, Item item, Collection c) throws SQLException, AuthorizeException {

        List<ResourcePolicy> defaultCollectionPolicies = authorizeService.getPoliciesActionFilter(context, c, Constants.DEFAULT_BITSTREAM_READ);

        if (defaultCollectionPolicies.size() < 1){
            throw new SQLException("Collection " + c.getID()
                    + " (" + c.getHandle(context) + ")"
                    + " has no default bitstream READ policies");
        }

        // remove all policies from bundles, add new ones
        // Remove bundles
        List<Bundle> bunds = item.getBundles();
        for (Bundle mybundle : bunds) {
            // if come from InstallItem: remove all submission/workflow policies
            authorizeService.removeAllPoliciesByDsoAndType(context, mybundle, ResourcePolicy.TYPE_SUBMISSION);
            authorizeService.removeAllPoliciesByDsoAndType(context, mybundle, ResourcePolicy.TYPE_WORKFLOW);

            List<ResourcePolicy> policiesBundleToAdd = filterPoliciesToAdd(context, defaultCollectionPolicies, mybundle);
            authorizeService.addPolicies(context, policiesBundleToAdd, mybundle);

            for (BundleBitstream bundleBitstream : mybundle.getBitstreams()) {
                Bitstream bitstream = bundleBitstream.getBitstream();
                // if come from InstallItem: remove all submission/workflow policies
                authorizeService.removeAllPoliciesByDsoAndType(context, bitstream, ResourcePolicy.TYPE_SUBMISSION);
                authorizeService.removeAllPoliciesByDsoAndType(context, bitstream, ResourcePolicy.TYPE_WORKFLOW);

                List<ResourcePolicy> policiesBitstreamToAdd = filterPoliciesToAdd(context, defaultCollectionPolicies, bitstream);
                authorizeService.addPolicies(context, policiesBitstreamToAdd, bitstream);
            }
        }
    }

    @Override
    public void adjustItemPolicies(Context context, Item item, Collection collection) throws SQLException, AuthorizeException {
        // read collection's default READ policies
        List<ResourcePolicy> defaultCollectionPolicies = authorizeService.getPoliciesActionFilter(context, collection, Constants.DEFAULT_ITEM_READ);

        // MUST have default policies
        if (defaultCollectionPolicies.size() < 1)
        {
            throw new SQLException("Collection " + collection.getID()
                    + " (" + collection.getHandle(context) + ")"
                    + " has no default item READ policies");
        }

        // if come from InstallItem: remove all submission/workflow policies
        authorizeService.removeAllPoliciesByDsoAndType(context, item, ResourcePolicy.TYPE_SUBMISSION);
        authorizeService.removeAllPoliciesByDsoAndType(context, item, ResourcePolicy.TYPE_WORKFLOW);

        // add default policies only if not already in place
        List<ResourcePolicy> policiesToAdd = filterPoliciesToAdd(context, defaultCollectionPolicies, item);
        authorizeService.addPolicies(context, policiesToAdd, item);
    }

    protected List<ResourcePolicy> filterPoliciesToAdd(Context context, List<ResourcePolicy> defaultCollectionPolicies, DSpaceObject dso) throws SQLException, AuthorizeException {
        List<ResourcePolicy> policiesToAdd = null;
        policiesToAdd = new ArrayList<ResourcePolicy>();
        for (ResourcePolicy defaultCollectionPolicy : defaultCollectionPolicies){
            //We do NOT alter the defaultCollectionPolicy since we would lose it if we do, instead clone it
            ResourcePolicy rp = (ResourcePolicy) resourcePolicyService.clone(context, defaultCollectionPolicy);

            rp.setAction(Constants.READ);
            // if an identical policy is already in place don't add it
            if(!authorizeService.isAnIdenticalPolicyAlreadyInPlace(context, dso, rp)){
                rp.setRpType(ResourcePolicy.TYPE_INHERITED);
                policiesToAdd.add(rp);
            }
        }
        return policiesToAdd;
    }

    /**
     * Check the bundle ORIGINAL to see if there are any uploaded files
     *
     * @return true if there is a bundle named ORIGINAL with one or more
     *         bitstreams inside
     * @throws SQLException
     */
    @Override
    public boolean hasUploadedFiles(Item item) throws SQLException
    {
        List<Bundle> bundles = getBundles(item, "ORIGINAL");
        for (Bundle bundle : bundles) {
            if (CollectionUtils.isNotEmpty(bundle.getBitstreams())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * return TRUE if context's user can edit item, false otherwise
     *
     * @return boolean true = current user can edit item
     * @throws SQLException
     */
    @Override
    public boolean canEdit(Context context, Item item) throws java.sql.SQLException
    {
        // can this person write to the item?
        return authorizeService.authorizeActionBoolean(context, item, Constants.WRITE, true);
    }

    /**
     * Returns an iterator of Items possessing the passed metadata field, or only
     * those matching the passed value, if value is not Item.ANY
     *
     * @param context DSpace context object
     * @param schema metadata field schema
     * @param element metadata field element
     * @param qualifier metadata field qualifier
     * @param value field value or Item.ANY to match any value
     * @return an iterator over the items matching that authority value
     * @throws SQLException, AuthorizeException, IOException
     *
     */
    @Override
    public Iterator<Item> findByMetadataField(Context context, String schema, String element, String qualifier, String value)
          throws SQLException, AuthorizeException
    {
        MetadataSchema mds = metadataSchemaService.find(context, schema);
        if (mds == null)
        {
            throw new IllegalArgumentException("No such metadata schema: " + schema);
        }
        MetadataField mdf = metadataFieldService.findByElement(context, mds, element, qualifier);
        if (mdf == null)
        {
            throw new IllegalArgumentException(
                    "No such metadata field: schema=" + schema + ", element=" + element + ", qualifier=" + qualifier);
        }
        if (Item.ANY.equals(value))
        {
            return itemDAO.findByMetadataField(context, mdf, null, true);
        }
        else
        {
            return itemDAO.findByMetadataField(context, mdf, value, true);
        }
     }
    
    @Override
    public DSpaceObject getAdminObject(Context context, Item item, int action) throws SQLException
    {
        DSpaceObject adminObject = null;
        //Items are always owned by collections
        Collection collection = (Collection) getParentObject(context, item);
        Community community = null;
        if (collection != null)
        {
            community = collection.getCommunities().iterator().next();
        }

        switch (action)
        {
            case Constants.ADD:
                // ADD a cc license is less general than add a bitstream but we can't/won't
                // add complex logic here to know if the ADD action on the item is required by a cc or
                // a generic bitstream so simply we ignore it.. UI need to enforce the requirements.
                if (AuthorizeConfiguration.canItemAdminPerformBitstreamCreation())
                {
                    adminObject = item;
                }
                else if (AuthorizeConfiguration.canCollectionAdminPerformBitstreamCreation())
                {
                    adminObject = collection;
                }
                else if (AuthorizeConfiguration.canCommunityAdminPerformBitstreamCreation())
                {
                    adminObject = community;
                }
                break;
            case Constants.REMOVE:
                // see comments on ADD action, same things...
                if (AuthorizeConfiguration.canItemAdminPerformBitstreamDeletion())
                {
                    adminObject = item;
                }
                else if (AuthorizeConfiguration.canCollectionAdminPerformBitstreamDeletion())
                {
                    adminObject = collection;
                }
                else if (AuthorizeConfiguration.canCommunityAdminPerformBitstreamDeletion())
                {
                    adminObject = community;
                }
                break;
            case Constants.DELETE:
                if (item.getOwningCollection() != null)
                {
                    if (AuthorizeConfiguration.canCollectionAdminPerformItemDeletion())
                    {
                        adminObject = collection;
                    }
                    else if (AuthorizeConfiguration.canCommunityAdminPerformItemDeletion())
                    {
                        adminObject = community;
                    }
                }
                else
                {
                    if (AuthorizeConfiguration.canCollectionAdminManageTemplateItem())
                    {
                        adminObject = collection;
                    }
                    else if (AuthorizeConfiguration.canCommunityAdminManageCollectionTemplateItem())
                    {
                        adminObject = community;
                    }
                }
                break;
            case Constants.WRITE:
                // if it is a template item we need to check the
                // collection/community admin configuration
                if (item.getOwningCollection() == null)
                {
                    if (AuthorizeConfiguration.canCollectionAdminManageTemplateItem())
                    {
                        adminObject = collection;
                    }
                    else if (AuthorizeConfiguration.canCommunityAdminManageCollectionTemplateItem())
                    {
                        adminObject = community;
                    }
                }
                else
                {
                    adminObject = item;
                }
                break;
            default:
                adminObject = item;
                break;
            }
        return adminObject;
    }

    @Override
    public DSpaceObject getParentObject(Context context, Item item) throws SQLException
    {
        Collection ownCollection = item.getOwningCollection();
        if (ownCollection != null)
        {
            return ownCollection;
        }
        else
        {
            // is a template item?
            return item.getTemplateItemOf();
        }
    }

    /**
     * Find all the items in the archive with a given authority key value
     * in the indicated metadata field.
     *
     * @param context DSpace context object
     * @param schema metadata field schema
     * @param element metadata field element
     * @param qualifier metadata field qualifier
     * @param value the value of authority key to look for
     * @return an iterator over the items matching that authority value
     * @throws SQLException, AuthorizeException, IOException
     */
    @Override
    public Iterator<Item> findByAuthorityValue(Context context,
            String schema, String element, String qualifier, String value)
        throws SQLException, AuthorizeException
    {
        MetadataSchema mds = metadataSchemaService.find(context, schema);
        if (mds == null)
        {
            throw new IllegalArgumentException("No such metadata schema: " + schema);
        }
        MetadataField mdf = metadataFieldService.findByElement(context, mds, element, qualifier);
        if (mdf == null)
        {
            throw new IllegalArgumentException("No such metadata field: schema=" + schema + ", element=" + element + ", qualifier=" + qualifier);
        }

        return itemDAO.findByAuthorityValue(context, mdf, value, true);
    }
}