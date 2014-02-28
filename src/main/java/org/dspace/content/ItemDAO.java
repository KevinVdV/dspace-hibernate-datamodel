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
import java.sql.Timestamp;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.app.util.AuthorizeUtil;
import org.dspace.authorize.AuthorizeConfiguration;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.browse.BrowseException;
import org.dspace.browse.IndexBrowse;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.content.authority.Choices;
import org.dspace.content.authority.ChoiceAuthorityManager;
import org.dspace.content.authority.MetadataAuthorityManager;
import org.dspace.event.Event;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.handle.HandleManager;
import org.dspace.hibernate.HibernateQueryUtil;
import org.dspace.identifier.IdentifierException;
import org.dspace.identifier.IdentifierService;
import org.dspace.utils.DSpace;
import org.dspace.versioning.VersioningService;
import org.hibernate.Query;

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
public class ItemDAO extends DSpaceObjectDAO<Item>
{

    /** log4j category */
    private static final Logger log = Logger.getLogger(Item.class);
    /** The bundles in this item - kept in sync with DB */
    //TODO/ IMPLEMENT WHEN BUNDLE BECOMES AVAILABLE
//    private List<Bundle> bundles;

    /** The Dublin Core metadata - inner class for lazy loading */
//    MetadataCache dublinCore = new MetadataCache();

    /** Handle, if any */
//    private String handle;


    /**
     * Construct an item with the given table row
     *
     * @throws SQLException
     */
    public ItemDAO() throws SQLException
    {
        clearDetails();

        // Get our Handle if any
//        handle = HandleManager.findHandle(context, this);
    }

    private TableRowIterator retrieveMetadata() throws SQLException
    {
        return DatabaseManager.queryTable(ourContext, "MetadataValue",
                "SELECT * FROM MetadataValue WHERE item_id= ? ORDER BY metadata_field_id, place",
                itemRow.getIntColumn("item_id"));
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
    public Item find(Context context, int id) throws SQLException
    {
        // First check the cache

        Item item = (Item) context.getDBConnection().get(Item.class, id);

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
    Item create(Context context) throws SQLException, AuthorizeException
    {
        Item item = new Item();

        // set discoverable to true (default)
        item.setDiscoverable(true);

        // Call update to give the item a last modified date. OK this isn't
        // amazingly efficient but creates don't happen that often.
        context.turnOffAuthorisationSystem();
        update(context, item);
        context.restoreAuthSystemState();

        context.addEvent(new Event(Event.CREATE, Constants.ITEM, item.getID(), null));

        log.info(LogManager.getHeader(context, "create_item", "item_id="
                + item.getID()));

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
    public Iterator<Item> findAll(Context context) throws SQLException
    {
        Query query = context.getDBConnection().createQuery("FROM item WHERE in_archive= :in_archive");
        query.setParameter("in_archive", true);
        return query.iterate();
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
	public Iterator<Item> findAllUnfiltered(Context context) throws SQLException
    {
        Query query = context.getDBConnection().createQuery("FROM item WHERE in_archive= :in_archive or withdrawn = :withdrawn");
        query.setParameter("in_archive", true);
        query.setParameter("withdrawn", true);
        query.iterate();

        return query.iterate();
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
    public Iterator<Item> findBySubmitter(Context context, EPerson eperson)
            throws SQLException
    {
        Query query = context.getDBConnection().createQuery("FROM item WHERE in_archive= :in_archive and submitter_id= :submitter_id");
        query.setParameter("in_archive", true);
        query.setParameter("submitter_id", true);

        return query.iterate();
    }

    /**
     * @see org.dspace.content.DSpaceObject#getHandle()
     */
    public String getHandle()
    {
        if(handle == null) {
                try {
                                handle = HandleManager.findHandle(this.ourContext, this);
                        } catch (SQLException e) {
                                // TODO Auto-generated catch block
                                //e.printStackTrace();
                        }
        }
        return handle;
    }

    /**
     * Method that updates the last modified date of the item
     */
    public void updateLastModified()
    {
        try {
            Date lastModified = new Timestamp(new Date().getTime());
            itemRow.setColumn("last_modified", lastModified);
            DatabaseManager.updateQuery(ourContext, "UPDATE item SET last_modified = ? WHERE item_id= ? ", lastModified, getID());
            //Also fire a modified event since the item HAS been modified
            ourContext.addEvent(new Event(Event.MODIFY, Constants.ITEM, getID(), null));
        } catch (SQLException e) {
            log.error(LogManager.getHeader(ourContext, "Error while updating last modified timestamp", "Item: " + getID()));
        }
    }

    /**
     * Get Dublin Core metadata for the item.
     * Passing in a <code>null</code> value for <code>qualifier</code>
     * or <code>lang</code> only matches Dublin Core fields where that
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
     * <code>item.getDC( "title", null, Item.ANY );</code>
     * <P>
     * Return all US English values of the "title" element, with any qualifier
     * (including unqualified):
     * <P>
     * <code>item.getDC( "title", Item.ANY, "en_US" );</code>
     * <P>
     * The ordering of values of a particular element/qualifier/language
     * combination is significant. When retrieving with wildcards, values of a
     * particular element/qualifier/language combinations will be adjacent, but
     * the overall ordering of the combinations is indeterminate.
     *
     * @param element
     *            the Dublin Core element. <code>Item.ANY</code> matches any
     *            element. <code>null</code> doesn't really make sense as all
     *            DC must have an element.
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
     * @return Dublin Core fields that match the parameters
     */
    @Deprecated
    public DCValue[] getDC(String element, String qualifier, String lang)
    {
        return getMetadata(MetadataSchema.DC_SCHEMA, element, qualifier, lang);
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
    public DCValue[] getMetadata(Item item, String schema, String element, String qualifier,
            String lang)
    {
        // Build up list of matching values
        List<DCValue> values = new ArrayList<DCValue>();
        for (DCValue dcv : item.getMetadata())
        {
            if (match(schema, element, qualifier, lang, dcv))
            {
                // We will return a copy of the object in case it is altered
                DCValue copy = new DCValue();
                copy.element = dcv.element;
                copy.qualifier = dcv.qualifier;
                copy.value = dcv.value;
                copy.language = dcv.language;
                copy.schema = dcv.schema;
                copy.authority = dcv.authority;
                copy.confidence = dcv.confidence;
                values.add(copy);
            }
        }

        // Create an array of matching values
        DCValue[] valueArray = new DCValue[values.size()];
        valueArray = values.toArray(valueArray);
        return valueArray;
    }
    
    /**
     * Retrieve metadata field values from a given metadata string
     * of the form <schema prefix>.<element>[.<qualifier>|.*]
     *
     * @param mdString
     *            The metadata string of the form
     *            <schema prefix>.<element>[.<qualifier>|.*]
     */
    public DCValue[] getMetadata(Item item, String mdString)
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
        
        DCValue[] values;
        if ("*".equals(qualifier))
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
     * Add Dublin Core metadata fields. These are appended to existing values.
     * Use <code>clearDC</code> to remove values. The ordering of values
     * passed in is maintained.
     *
     * @param element
     *            the Dublin Core element
     * @param qualifier
     *            the Dublin Core qualifier, or <code>null</code> for
     *            unqualified
     * @param lang
     *            the ISO639 language code, optionally followed by an underscore
     *            and the ISO3166 country code. <code>null</code> means the
     *            value has no language (for example, a date).
     * @param values
     *            the values to add.
     */
    @Deprecated
    public void addDC(Item item, String element, String qualifier, String lang,
            String[] values)
    {
        addMetadata(item, MetadataSchema.DC_SCHEMA, element, qualifier, lang, values);
    }

    /**
     * Add a single Dublin Core metadata field. This is appended to existing
     * values. Use <code>clearDC</code> to remove values.
     *
     * @param element
     *            the Dublin Core element
     * @param qualifier
     *            the Dublin Core qualifier, or <code>null</code> for
     *            unqualified
     * @param lang
     *            the ISO639 language code, optionally followed by an underscore
     *            and the ISO3166 country code. <code>null</code> means the
     *            value has no language (for example, a date).
     * @param value
     *            the value to add.
     */
    @Deprecated
    public void addDC(Item item, String element, String qualifier, String lang,
            String value)
    {
        addMetadata(item, MetadataSchema.DC_SCHEMA, element, qualifier, lang, value);
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
    public void addMetadata(Item item, String schema, String element, String qualifier, String lang,
            String[] values)
    {
        MetadataAuthorityManager mam = MetadataAuthorityManager.getManager();
        String fieldKey = MetadataAuthorityManager.makeFieldKey(schema, element, qualifier);
        if (mam.isAuthorityControlled(fieldKey))
        {
            String authorities[] = new String[values.length];
            int confidences[] = new int[values.length];
            for (int i = 0; i < values.length; ++i)
            {
                Choices c = ChoiceAuthorityManager.getManager().getBestMatch(fieldKey, values[i], item.getOwningCollectionID(), null);
                authorities[i] = c.values.length > 0 ? c.values[0].authority : null;
                confidences[i] = c.confidence;
            }
            addMetadata(item, schema, element, qualifier, lang, values, authorities, confidences);
        }
        else
        {
            addMetadata(item, schema, element, qualifier, lang, values, null, null);
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
    public void addMetadata(Item item, String schema, String element, String qualifier, String lang,
            String[] values, String authorities[], int confidences[])
    {
        List<DCValue> dublinCore = item.getMetadata();
        MetadataAuthorityManager mam = MetadataAuthorityManager.getManager();        
        boolean authorityControlled = mam.isAuthorityControlled(schema, element, qualifier);
        boolean authorityRequired = mam.isAuthorityRequired(schema, element, qualifier);
        String fieldName = schema+"."+element+((qualifier==null)? "": "."+qualifier);

        // We will not verify that they are valid entries in the registry
        // until update() is called.
        for (int i = 0; i < values.length; i++)
        {
            DCValue dcv = new DCValue();
            dcv.schema = schema;
            dcv.element = element;
            dcv.qualifier = qualifier;
            dcv.language = (lang == null ? null : lang.trim());

            // Logic to set Authority and Confidence:
            //  - normalize an empty string for authority to NULL.
            //  - if authority key is present, use given confidence or NOVALUE if not given
            //  - otherwise, preserve confidence if meaningful value was given since it may document a failed authority lookup
            //  - CF_UNSET signifies no authority nor meaningful confidence.
            //  - it's possible to have empty authority & CF_ACCEPTED if e.g. user deletes authority key
            if (authorityControlled)
            {
                if (authorities != null && authorities[i] != null && authorities[i].length() > 0)
                {
                    dcv.authority = authorities[i];
                    dcv.confidence = confidences == null ? Choices.CF_NOVALUE : confidences[i];
                }
                else
                {
                    dcv.authority = null;
                    dcv.confidence = confidences == null ? Choices.CF_UNSET : confidences[i];
                }
                // authority sanity check: if authority is required, was it supplied?
                // XXX FIXME? can't throw a "real" exception here without changing all the callers to expect it, so use a runtime exception
                if (authorityRequired && (dcv.authority == null || dcv.authority.length() == 0))
                {
                    throw new IllegalArgumentException("The metadata field \"" + fieldName + "\" requires an authority key but none was provided. Vaue=\"" + dcv.value + "\"");
                }
            }
            if (values[i] != null)
            {
                // remove control unicode char
                String temp = values[i].trim();
                char[] dcvalue = temp.toCharArray();
                for (int charPos = 0; charPos < dcvalue.length; charPos++)
                {
                    if (Character.isISOControl(dcvalue[charPos]) &&
                        !String.valueOf(dcvalue[charPos]).equals("\u0009") &&
                        !String.valueOf(dcvalue[charPos]).equals("\n") &&
                        !String.valueOf(dcvalue[charPos]).equals("\r"))
                    {
                        dcvalue[charPos] = ' ';
                    }
                }
                dcv.value = String.valueOf(dcvalue);
            }
            else
            {
                dcv.value = null;
            }
            //Set the place to be the next place in the line
            dcv.setPlace(getMetadata(item, schema, element, qualifier, Item.ANY).length + 1);
            dublinCore.add(dcv);
            addDetails(fieldName);
        }
        item.setMetadata(dublinCore);
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
    public void addMetadata(Item item, String schema, String element, String qualifier,
            String lang, String value)
    {
        String[] valArray = new String[1];
        valArray[0] = value;

        addMetadata(item, schema, element, qualifier, lang, valArray);
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
    public void addMetadata(Item item, String schema, String element, String qualifier,
            String lang, String value, String authority, int confidence)
    {
        String[] valArray = new String[1];
        String[] authArray = new String[1];
        int[] confArray = new int[1];
        valArray[0] = value;
        authArray[0] = authority;
        confArray[0] = confidence;

        addMetadata(item, schema, element, qualifier, lang, valArray, authArray, confArray);
    }

    /**
     * Clear Dublin Core metadata values. As with <code>getDC</code> above,
     * passing in <code>null</code> only matches fields where the qualifier or
     * language is actually <code>null</code>.<code>Item.ANY</code> will
     * match any element, qualifier or language, including <code>null</code>.
     * Thus, <code>item.clearDC(Item.ANY, Item.ANY, Item.ANY)</code> will
     * remove all Dublin Core metadata associated with an item.
     *
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
    @Deprecated
    public void clearDC(Item item, String element, String qualifier, String lang)
    {
        clearMetadata(item, MetadataSchema.DC_SCHEMA, element, qualifier, lang);
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
    public void clearMetadata(Item item, String schema, String element, String qualifier,
            String lang)
    {
        // We will build a list of values NOT matching the values to clear
        List<DCValue> values = new ArrayList<DCValue>();
        for (DCValue dcv : item.getMetadata())
        {
            if (!match(schema, element, qualifier, lang, dcv))
            {
                values.add(dcv);
            }
        }

        // Now swap the old list of values for the new, unremoved values
        item.setMetadata(values);
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
     * @param dcv
     *            the Dublin Core value
     * @return <code>true</code> if there is a match
     */
    private boolean match(String schema, String element, String qualifier,
            String language, DCValue dcv)
    {
        // We will attempt to disprove a match - if we can't we have a match
        if (!element.equals(Item.ANY) && !element.equals(dcv.element))
        {
            // Elements do not match, no wildcard
            return false;
        }

        if (qualifier == null)
        {
            // Value must be unqualified
            if (dcv.qualifier != null)
            {
                // Value is qualified, so no match
                return false;
            }
        }
        else if (!qualifier.equals(Item.ANY))
        {
            // Not a wildcard, so qualifier must match exactly
            if (!qualifier.equals(dcv.qualifier))
            {
                return false;
            }
        }

        if (language == null)
        {
            // Value must be null language to match
            if (dcv.language != null)
            {
                // Value is qualified, so no match
                return false;
            }
        }
        else if (!language.equals(Item.ANY))
        {
            // Not a wildcard, so language must match exactly
            if (!language.equals(dcv.language))
            {
                return false;
            }
        }

        if (!schema.equals(Item.ANY))
        {
            if (dcv.schema != null && !dcv.schema.equals(schema))
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
    public boolean isIn(Item item, Collection collection) throws SQLException
    {
        TableRow tr = DatabaseManager.querySingle(ourContext,
                "SELECT COUNT(*) AS count" +
                " FROM collection2item" +
                " WHERE collection_id = ? AND item_id = ?",
                collection.getID(), itemRow.getIntColumn("item_id"));
        return tr.getLongColumn("count") > 0;
    }

    /**
     * Get the communities this item is in. Returns an unordered array of the
     * communities that house the collections this item is in, including parent
     * communities of the owning collections.
     *
     * @return the communities this item is in.
     * @throws SQLException
     */
    public Community[] getCommunities() throws SQLException
    {
        List<Community> communities = new ArrayList<Community>();

        // Get community table rows
        TableRowIterator tri = DatabaseManager.queryTable(ourContext,"community",
                        "SELECT community.* FROM community, community2item " +
                        "WHERE community2item.community_id=community.community_id " +
                        "AND community2item.item_id= ? ",
                        itemRow.getIntColumn("item_id"));

        try
        {
            while (tri.hasNext())
            {
                TableRow row = tri.next();

                // First check the cache
                Community owner = (Community) ourContext.fromCache(Community.class,
                        row.getIntColumn("community_id"));

                if (owner == null)
                {
                    owner = new Community(ourContext, row);
                }

                communities.add(owner);

                // now add any parent communities
                Community[] parents = owner.getAllParents();
                communities.addAll(Arrays.asList(parents));
            }
        }
        finally
        {
            // close the TableRowIterator to free up resources
            if (tri != null)
            {
                tri.close();
            }
        }

        Community[] communityArray = new Community[communities.size()];
        communityArray = (Community[]) communities.toArray(communityArray);

        return communityArray;
    }

    /**
     * Get the bundles matching a bundle name (name corresponds roughly to type)
     *
     * @param name
     *            name of bundle (ORIGINAL/TEXT/THUMBNAIL)
     *
     * @return the bundles in an unordered array
     */
    public Bundle[] getBundles(Item item, String name) throws SQLException
    {
        List<Bundle> matchingBundles = new ArrayList<Bundle>();

        // now only keep bundles with matching names
        List<Bundle> bunds = item.getBundles();
        for (Bundle bund : bunds) {
            if (name.equals(bund.getName())) {
                matchingBundles.add(bund);
            }
        }

        Bundle[] bundleArray = new Bundle[matchingBundles.size()];
        bundleArray = matchingBundles.toArray(bundleArray);

        return bundleArray;
    }

    /**
     * Create a bundle in this item, with immediate effect
     *
     * @param name
     *            bundle name (ORIGINAL/TEXT/THUMBNAIL)
     * @return the newly created bundle
     * @throws SQLException
     * @throws AuthorizeException
     */
    public Bundle createBundle(Context context, Item item, String name) throws SQLException,
            AuthorizeException
    {
        if ((name == null) || "".equals(name))
        {
            throw new SQLException("Bundle must be created with non-null name");
        }

        // Check authorisation
        AuthorizeManager.authorizeAction(context, item, Constants.ADD);

        Bundle b = new BundleDAO().create(context);
        b.setName(name);
        HibernateQueryUtil.update(context, b);

        addBundle(context, item, b);

        return b;
    }

    /**
     * Add an existing bundle to this item. This has immediate effect.
     *
     * @param b
     *            the bundle to add
     * @throws SQLException
     * @throws AuthorizeException
     */
    public void addBundle(Context context, Item item, Bundle b) throws SQLException, AuthorizeException
    {
        // Check authorisation
        AuthorizeManager.authorizeAction(context, item, Constants.ADD);

        log.info(LogManager.getHeader(context, "add_bundle", "item_id="
                + item.getID() + ",bundle_id=" + b.getID()));

        // Check it's not already there
        List<Bundle> bunds = item.getBundles();
        for (Bundle bund : bunds) {
            if (b.getID() == bund.getID()) {
                // Bundle is already there; no change
                return;
            }
        }

        // now add authorization policies from owning item
        // hmm, not very "multiple-inclusion" friendly
        AuthorizeManager.inheritPolicies(context, item, b);

        // Add the bundle to in-memory list
        item.addBundle(b);

        // Insert the mapping
        context.addEvent(new Event(Event.ADD, Constants.ITEM, item.getID(), Constants.BUNDLE, b.getID(), b.getName()));
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
    public void removeBundle(Context context, Item item, Bundle b) throws SQLException, AuthorizeException,
            IOException
    {
        // Check authorisation
        AuthorizeManager.authorizeAction(context, item, Constants.REMOVE);

        log.info(LogManager.getHeader(context, "remove_bundle", "item_id="
                + item.getID() + ",bundle_id=" + b.getID()));

        // We've found the bundle to remove
        item.removeBundle(b);
        HibernateQueryUtil.delete(context, b);


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
    public Bitstream createSingleBitstream(Context context, Item item, InputStream is, String name)
            throws AuthorizeException, IOException, SQLException
    {
        // Authorisation is checked by methods below
        // Create a bundle
        Bundle bnd = createBundle(context, item, name);
        Bitstream bitstream = new BundleDAO().createBitstream(context, bnd, is);
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
    public Bitstream[] getNonInternalBitstreams(Item item) throws SQLException
    {
        List<Bitstream> bitstreamList = new ArrayList<Bitstream>();

        // Go through the bundles and bitstreams picking out ones which aren't
        // of internal formats
        List<Bundle> bunds = item.getBundles();

        for (Bundle bund : bunds) {
            List<Bitstream> bitstreams = bund.getBitstreams();

            for (Bitstream bitstream : bitstreams) {
                if (!bitstream.getFormat().isInternal()) {
                    // Bitstream is not of an internal format
                    bitstreamList.add(bitstream);
                }
            }
        }

        return bitstreamList.toArray(new Bitstream[bitstreamList.size()]);
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
    public void removeDSpaceLicense(Context context, Item item) throws SQLException, AuthorizeException,
            IOException
    {
        // get all bundles with name "LICENSE" (these are the DSpace license
        // bundles)
        Bundle[] bunds = getBundles(item, "LICENSE");

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
    public void removeLicenses(Context context, Item item) throws SQLException, AuthorizeException,
            IOException
    {
        // Find the License format
        BitstreamFormat bf = new BitstreamFormatDAO().findByShortDescription(context, "License");
        int licensetype = bf.getID();

        // search through bundles, looking for bitstream type license
        List<Bundle> bunds = item.getBundles();

        for (Bundle bund : bunds) {
            boolean removethisbundle = false;

            List<Bitstream> bits = bund.getBitstreams();

            for (Bitstream bit : bits) {
                BitstreamFormat bft = bit.getFormat();

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
     * Update the item "in archive" flag and Dublin Core metadata in the
     * database
     *
     * @throws SQLException
     * @throws AuthorizeException
     */
    public void update(Context context, Item item) throws SQLException, AuthorizeException
    {
        // Check authorisation
        // only do write authorization if user is not an editor
        if (!canEdit(context, item))
        {
            AuthorizeManager.authorizeAction(context, item, Constants.WRITE);
        }

        log.info(LogManager.getHeader(context, "update_item", "item_id="
                + item.getID()));

        // Set sequence IDs for bitstreams in item
        int sequence = 0;
        Bundle[] bunds = getBundles(item);

        // find the highest current sequence number
        for (int i = 0; i < bunds.length; i++)
        {
            List<Bitstream> streams = bunds[i].getBitstreams();

            for(Bitstream bitstream : streams)
            {
                if (streams[k].getSequenceID() > sequence)
                {
                    sequence = streams[k].getSequenceID();
                }
            }
        }

        // start sequencing bitstreams without sequence IDs
        sequence++;

        for (int i = 0; i < bunds.length; i++)
        {
            Bitstream[] streams = bunds[i].getBitstreams();

            for (int k = 0; k < streams.length; k++)
            {
                if (streams[k].getSequenceID() < 0)
                {
                    streams[k].setSequenceID(sequence);
                    sequence++;
                    streams[k].update();
                    modified = true;
                }
            }
        }

        // Map counting number of values for each element/qualifier.
        // Keys are Strings: "element" or "element.qualifier"
        // Values are Integers indicating number of values written for a
        // element/qualifier
        Map<String,Integer> elementCount = new HashMap<String,Integer>();

        // Redo Dublin Core if it's changed
        if (dublinCoreChanged)
        {
            dublinCoreChanged = false;

            // Arrays to store the working information required
            int[]     placeNum = new int[getMetadata().size()];
            boolean[] storedDC = new boolean[getMetadata().size()];
            MetadataField[] dcFields = new MetadataField[getMetadata().size()];


        if (dublinCoreChanged || modified)
        {
            // Set the last modified date
            item.setLastModified(new Date());


            DatabaseManager.update(ourContext, itemRow);

            if (dublinCoreChanged)
            {
                context.addEvent(new Event(Event.MODIFY_METADATA, Constants.ITEM, item.getID(), getDetails()));
                clearDetails();
                dublinCoreChanged = false;
            }

            context.addEvent(new Event(Event.MODIFY, Constants.ITEM, item.getID(), null));
            modified = false;
        }
    }

    private transient MetadataField[] allMetadataFields = null;
    private MetadataField getMetadataField(DCValue dcv) throws SQLException, AuthorizeException
    {
        if (allMetadataFields == null)
        {
            allMetadataFields = new MetadataFieldDAO().findAll(ourContext);
        }

        if (allMetadataFields != null)
        {
            int schemaID = getMetadataSchemaID(dcv);
            for (MetadataField field : allMetadataFields)
            {
                if (field.getSchemaID() == schemaID &&
                        StringUtils.equals(field.getElement(), dcv.element) &&
                        StringUtils.equals(field.getQualifier(), dcv.qualifier))
                {
                    return field;
                }
            }
        }

        return null;
    }

    private int getMetadataSchemaID(DCValue dcv) throws SQLException
    {
        int schemaID;
        MetadataSchema schema = MetadataSchema.find(ourContext,dcv.schema);
        if (schema == null)
        {
            schemaID = MetadataSchema.DC_SCHEMA_ID;
        }
        else
        {
            schemaID = schema.getSchemaID();
        }
        return schemaID;
    }

    /**
     * Withdraw the item from the archive. It is kept in place, and the content
     * and metadata are not deleted, but it is not publicly accessible.
     *
     * @throws SQLException
     * @throws AuthorizeException
     * @throws IOException
     */
    public void withdraw(Context context, Item item) throws SQLException, AuthorizeException, IOException
    {
        // Check permission. User either has to have REMOVE on owning collection
        // or be COLLECTION_EDITOR of owning collection
        AuthorizeUtil.authorizeWithdrawItem(context, this);

        String timestamp = DCDate.getCurrent().toString();

        // Add suitable provenance - includes user, date, collections +
        // bitstream checksums
        EPerson e = context.getCurrentUser();

        // Build some provenance data while we're at it.
        StringBuilder prov = new StringBuilder();

        prov.append("Item withdrawn by ").append(e.getFullName()).append(" (")
                .append(e.getEmail()).append(") on ").append(timestamp).append("\n")
                .append("Item was in collections:\n");

        Collection[] colls = item.getCollections();

        for (Collection coll : colls) {
            prov.append(coll.getName()).append(" (ID: ").append(coll.getID()).append(")\n");
        }

        // Set withdrawn flag. timestamp will be set; last_modified in update()
        item.setWithdrawn(true);

        // in_archive flag is now false
        item.setInArchive(false);

        prov.append(InstallItem.getBitstreamProvenanceMessage(this));

        addDC("description", "provenance", "en", prov.toString());

        // Update item in DB
        update(context, item);

        context.addEvent(new Event(Event.MODIFY, Constants.ITEM, item.getID(), "WITHDRAW"));

        // remove all authorization policies, saving the custom ones
        AuthorizeManager.removeAllPoliciesByDSOAndTypeNotEqualsTo(context, this, ResourcePolicy.TYPE_CUSTOM);

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
    public void reinstate(Context context, Item item) throws SQLException, AuthorizeException,
            IOException
    {
        // check authorization
        AuthorizeUtil.authorizeReinstateItem(context, this);

        String timestamp = DCDate.getCurrent().toString();

        // Check permission. User must have ADD on all collections.
        // Build some provenance data while we're at it.
        Collection[] colls = item.getCollections();

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
        prov.append(InstallItem.getBitstreamProvenanceMessage(this));

        addDC("description", "provenance", "en", prov.toString());

        // Update item in DB
        update(context, item);

        context.addEvent(new Event(Event.MODIFY, Constants.ITEM, item.getID(), "REINSTATE"));

        // authorization policies
        if (colls.length > 0)
        {
            // FIXME: not multiple inclusion friendly - just apply access
            // policies from first collection
            // remove the item's policies and replace them with
            // the defaults from the collection
            inheritCollectionDefaultPolicies(colls[0]);
        }

        // Write log
        log.info(LogManager.getHeader(context, "reinstate_item", "user="
                + e.getEmail() + ",item_id=" + item.getID()));
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
    void delete(Context context, Item item) throws SQLException, AuthorizeException, IOException
    {
        // Check authorisation here. If we don't, it may happen that we remove the
        // metadata but when getting to the point of removing the bundles we get an exception
        // leaving the database in an inconsistent state
        AuthorizeManager.authorizeAction(context, item, Constants.REMOVE);

        context.addEvent(new Event(Event.DELETE, Constants.ITEM, item.getID(), getHandle()));

        log.info(LogManager.getHeader(context, "delete_item", "item_id="
                + item.getID()));

        // Remove from browse indices, if appropriate
        /** XXX FIXME
         ** Although all other Browse index updates are managed through
         ** Event consumers, removing an Item *must* be done *here* (inline)
         ** because otherwise, tables are left in an inconsistent state
         ** and the DB transaction will fail.
         ** Any fix would involve too much work on Browse code that
         ** is likely to be replaced soon anyway.   --lcs, Aug 2006
         **
         ** NB Do not check to see if the item is archived - withdrawn /
         ** non-archived items may still be tracked in some browse tables
         ** for administrative purposes, and these need to be removed.
         **/
//               FIXME: there is an exception handling problem here
        try
        {
//               Remove from indices
            IndexBrowse ib = new IndexBrowse(ourContext);
            ib.itemRemoved(this);
        }
        catch (BrowseException e)
        {
            log.error("caught exception: ", e);
            throw new SQLException(e.getMessage(), e);
        }

        // Delete the Dublin Core
        removeMetadataFromDatabase();

        // Remove bundles
        List<Bundle> bunds = item.getBundles();

        for (Bundle bundle : bunds) {
            removeBundle(context, item, bundle);
        }

        // remove all of our authorization policies
        AuthorizeManager.removeAllPolicies(context, item);
        
        // Remove any Handle
        HandleManager.unbindHandle(context, item);
        
        // remove version attached to the item
        removeVersion();


        // Finally remove item row
        HibernateQueryUtil.delete(context, item);
    }
    
    private void removeVersion() throws AuthorizeException, SQLException
    {
        VersioningService versioningService = new DSpace().getSingletonService(VersioningService.class);
        if(versioningService.getVersion(ourContext, this)!=null)
        {
            versioningService.removeVersion(ourContext, this);
        }else{
            IdentifierService identifierService = new DSpace().getSingletonService(IdentifierService.class);
            try {
                identifierService.delete(ourContext, this);
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
    public boolean isOwningCollection(Item item, Collection c)
    {
        int owner_id = item.getOwningCollection();

        if (c.getID() == owner_id)
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
    public void replaceAllItemPolicies(Context context, Item item, List<ResourcePolicy> newpolicies) throws SQLException,
            AuthorizeException
    {
        // remove all our policies, add new ones
        AuthorizeManager.removeAllPolicies(context, item);
        AuthorizeManager.addPolicies(context, newpolicies, item);
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
    public void replaceAllBitstreamPolicies(Context context, Item item, List<ResourcePolicy> newpolicies)
            throws SQLException, AuthorizeException
    {
        // remove all policies from bundles, add new ones
        // Remove bundles
        List<Bundle> bunds = item.getBundles();

        BundleDAO bundleDAO = new BundleDAO();
        for (Bundle mybundle : bunds) {
            bundleDAO.replaceAllBitstreamPolicies(context, mybundle, newpolicies);
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
    public void removeGroupPolicies(Context context, Item item, Group g) throws SQLException
    {
        // remove Group's policies from Item
        AuthorizeManager.removeGroupPolicies(context, item, g);

        // remove all policies from bundles
        List<Bundle> bunds = item.getBundles();

        for (Bundle mybundle : bunds) {
            List<Bitstream> bs = mybundle.getBitstreams();

            for (Bitstream b : bs) {
                // remove bitstream policies
                AuthorizeManager.removeGroupPolicies(context, b, g);
            }

            // change bundle policies
            AuthorizeManager.removeGroupPolicies(context, mybundle, g);
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
    public void inheritCollectionDefaultPolicies(Context context, Item item, Collection c)
            throws java.sql.SQLException, AuthorizeException
    {
        adjustItemPolicies(context, item, c);
        adjustBundleBitstreamPolicies(c);

        log.debug(LogManager.getHeader(context, "item_inheritCollectionDefaultPolicies",
                                                   "item_id=" + item.getID()));
    }

    public void adjustBundleBitstreamPolicies(Context context, Item item, Collection c) throws SQLException, AuthorizeException {

        List<ResourcePolicy> defaultCollectionPolicies = AuthorizeManager.getPoliciesActionFilter(context, c, Constants.DEFAULT_BITSTREAM_READ);

        if (defaultCollectionPolicies.size() < 1){
            throw new SQLException("Collection " + c.getID()
                    + " (" + c.getHandle() + ")"
                    + " has no default bitstream READ policies");
        }

        // remove all policies from bundles, add new ones
        // Remove bundles
        Bundle[] bunds = getBundles();
        for (int i = 0; i < bunds.length; i++){
            Bundle mybundle = bunds[i];

            // if come from InstallItem: remove all submission/workflow policies
            AuthorizeManager.removeAllPoliciesByDSOAndType(context, mybundle, ResourcePolicy.TYPE_SUBMISSION);
            AuthorizeManager.removeAllPoliciesByDSOAndType(context, mybundle, ResourcePolicy.TYPE_WORKFLOW);

            List<ResourcePolicy> policiesBundleToAdd = filterPoliciesToAdd(context, defaultCollectionPolicies, mybundle);
            AuthorizeManager.addPolicies(context, policiesBundleToAdd, mybundle);

            for(Bitstream bitstream : mybundle.getBitstreams()){
                // if come from InstallItem: remove all submission/workflow policies
                AuthorizeManager.removeAllPoliciesByDSOAndType(context, bitstream, ResourcePolicy.TYPE_SUBMISSION);
                AuthorizeManager.removeAllPoliciesByDSOAndType(context, bitstream, ResourcePolicy.TYPE_WORKFLOW);

                List<ResourcePolicy> policiesBitstreamToAdd = filterPoliciesToAdd(context, defaultCollectionPolicies, bitstream);
                AuthorizeManager.addPolicies(context, policiesBitstreamToAdd, bitstream);
            }
        }
    }

    public void adjustItemPolicies(Context context, Item item, Collection c) throws SQLException, AuthorizeException {
        // read collection's default READ policies
        List<ResourcePolicy> defaultCollectionPolicies = AuthorizeManager.getPoliciesActionFilter(context, c, Constants.DEFAULT_ITEM_READ);

        // MUST have default policies
        if (defaultCollectionPolicies.size() < 1)
        {
            throw new SQLException("Collection " + c.getID()
                    + " (" + c.getHandle() + ")"
                    + " has no default item READ policies");
        }

        // if come from InstallItem: remove all submission/workflow policies
        AuthorizeManager.removeAllPoliciesByDSOAndType(context, item, ResourcePolicy.TYPE_SUBMISSION);
        AuthorizeManager.removeAllPoliciesByDSOAndType(context, item, ResourcePolicy.TYPE_WORKFLOW);

        // add default policies only if not already in place
        List<ResourcePolicy> policiesToAdd = filterPoliciesToAdd(context, defaultCollectionPolicies, item);
        AuthorizeManager.addPolicies(context, policiesToAdd, item);
    }

    private List<ResourcePolicy> filterPoliciesToAdd(Context context, List<ResourcePolicy> defaultCollectionPolicies, DSpaceObject dso) throws SQLException {
        List<ResourcePolicy> policiesToAdd = new ArrayList<ResourcePolicy>();
        for (ResourcePolicy rp : defaultCollectionPolicies){
            rp.setAction(Constants.READ);
            // if an identical policy is already in place don't add it
            if(!AuthorizeManager.isAnIdenticalPolicyAlreadyInPlace(context, dso, rp)){
                rp.setRpType(ResourcePolicy.TYPE_INHERITED);
                policiesToAdd.add(rp);
            }
        }
        return policiesToAdd;
    }

    /**
     * Moves the item from one collection to another one
     *
     * @throws SQLException
     * @throws AuthorizeException
     * @throws IOException
     */
    public void move (Context context, Item item, Collection from, Collection to) throws SQLException, AuthorizeException, IOException
    {
        // Use the normal move method, and default to not inherit permissions
        move(context, item, from, to, false);
    }

    /**
     * Moves the item from one collection to another one
     *
     * @throws SQLException
     * @throws AuthorizeException
     * @throws IOException
     */
    public void move (Context context, Item item, Collection from, Collection to, boolean inheritDefaultPolicies) throws SQLException, AuthorizeException, IOException
    {
        // Check authorisation on the item before that the move occur
        // otherwise we will need edit permission on the "target collection" to archive our goal
        // only do write authorization if user is not an editor
        if (!canEdit(context, item))
        {
            AuthorizeManager.authorizeAction(context, item, Constants.WRITE);
        }
        
        // Move the Item from one Collection to the other
        to.addItem(this);
        from.removeItem(this);

        // If we are moving from the owning collection, update that too
        if (isOwningCollection(item, from))
        {
            // Update the owning collection
            log.info(LogManager.getHeader(context, "move_item",
                                          "item_id=" + item.getID() + ", from " +
                                          "collection_id=" + from.getID() + " to " +
                                          "collection_id=" + to.getID()));
            item.setOwningCollection(to);

            // If applicable, update the item policies
            if (inheritDefaultPolicies)
            {
                log.info(LogManager.getHeader(context, "move_item",
                         "Updating item with inherited policies"));
                inheritCollectionDefaultPolicies(context, item, to);
            }

            // Update the item
            context.turnOffAuthorisationSystem();
            update(context, item);
            context.restoreAuthSystemState();
        }
        else
        {
            // Although we haven't actually updated anything within the item
            // we'll tell the event system that it has, so that any consumers that
            // care about the structure of the repository can take account of the move

            // Note that updating the owning collection above will have the same effect,
            // so we only do this here if the owning collection hasn't changed.
            
            context.addEvent(new Event(Event.MODIFY, Constants.ITEM, item.getID(), null));
        }
    }
    
    /**
     * Check the bundle ORIGINAL to see if there are any uploaded files
     *
     * @return true if there is a bundle named ORIGINAL with one or more
     *         bitstreams inside
     * @throws SQLException
     */
    public boolean hasUploadedFiles(Item item) throws SQLException
    {
        Bundle[] bundles = getBundles(item, "ORIGINAL");
        if (bundles.length == 0)
        {
            // if no ORIGINAL bundle,
            // return false that there is no file!
            return false;
        }
        else
        {
            List<Bitstream> bitstreams = bundles[0].getBitstreams();
            if (bitstreams.size() == 0)
            {
                // no files in ORIGINAL bundle!
                return false;
            }
        }
        return true;
    }
    
    /**
     * Get the collections this item is not in.
     *
     * @return the collections this item is not in, if any.
     * @throws SQLException
     */
    public Collection[] getCollectionsNotLinked() throws SQLException
    {
        Collection[] allCollections = Collection.findAll(ourContext);
        Collection[] linkedCollections = getCollections();
        Collection[] notLinkedCollections = new Collection[allCollections.length - linkedCollections.length];

        if ((allCollections.length - linkedCollections.length) == 0)
        {
                return notLinkedCollections;
        }
        
        int i = 0;
                 
        for (Collection collection : allCollections)
        {
                 boolean alreadyLinked = false;
                         
                 for (Collection linkedCommunity : linkedCollections)
                 {
                         if (collection.getID() == linkedCommunity.getID())
                         {
                                 alreadyLinked = true;
                                 break;
                         }
                 }
                         
                 if (!alreadyLinked)
                 {
                         notLinkedCollections[i++] = collection;
                 }
        }
        
        return notLinkedCollections;
    }

    /**
     * return TRUE if context's user can edit item, false otherwise
     *
     * @return boolean true = current user can edit item
     * @throws SQLException
     */
    public boolean canEdit(Context context, Item item) throws java.sql.SQLException
    {
        // can this person write to the item?
        if (AuthorizeManager.authorizeActionBoolean(context, item,
                Constants.WRITE))
        {
            return true;
        }

        // is this collection not yet created, and an item template is created
        if (item.getOwningCollection() == null)
        {
            return true;
        }

        // is this person an COLLECTION_EDITOR for the owning collection?
        if (item.getOwningCollection().canEditBoolean(false))
        {
            return true;
        }

        return false;
    }
    
    public String getName()
    {
        DCValue t[] = getMetadata("dc", "title", null, Item.ANY);
        return (t.length >= 1) ? t[0].value : null;
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
    public ItemIterator findByMetadataField(Context context,
               String schema, String element, String qualifier, String value)
          throws SQLException, AuthorizeException, IOException
    {
        MetadataSchema mds = MetadataSchema.find(context, schema);
        if (mds == null)
        {
            throw new IllegalArgumentException("No such metadata schema: " + schema);
        }
        MetadataField mdf = new MetadataFieldDAO().findByElement(context, mds.getSchemaID(), element, qualifier);
        if (mdf == null)
        {
            throw new IllegalArgumentException(
                    "No such metadata field: schema=" + schema + ", element=" + element + ", qualifier=" + qualifier);
        }
        
        String query = "SELECT item.* FROM metadatavalue,item WHERE item.in_archive='1' "+
                       "AND item.item_id = metadatavalue.item_id AND metadata_field_id = ?";
        TableRowIterator rows = null;
        if (Item.ANY.equals(value))
        {
                rows = DatabaseManager.queryTable(context, "item", query, mdf.getFieldID());
        }
        else
        {
                query += " AND metadatavalue.text_value = ?";
                rows = DatabaseManager.queryTable(context, "item", query, mdf.getFieldID(), value);
        }
        return new ItemIterator(context, rows);
     }
    
    public DSpaceObject getAdminObject(Context context, Item item, int action) throws SQLException
    {
        DSpaceObject adminObject = null;
        //Items are always owned by collections
        Collection collection = (Collection) getParentObject(context, item);
        Community community = null;
        if (collection != null)
        {
            community = collection.getOwningCommunity();
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
            return new CollectionDAO().findByTemplateItem(context, item);
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
    public ItemIterator findByAuthorityValue(Context context,
            String schema, String element, String qualifier, String value)
        throws SQLException, AuthorizeException, IOException
    {
        MetadataSchema mds = MetadataSchema.find(context, schema);
        if (mds == null)
        {
            throw new IllegalArgumentException("No such metadata schema: " + schema);
        }
        MetadataField mdf = new MetadataFieldDAO().findByElement(context, mds.getSchemaID(), element, qualifier);
        if (mdf == null)
        {
            throw new IllegalArgumentException("No such metadata field: schema=" + schema + ", element=" + element + ", qualifier=" + qualifier);
        }

        TableRowIterator rows = DatabaseManager.queryTable(context, "item",
            "SELECT item.* FROM metadatavalue,item WHERE item.in_archive='1' "+
            "AND item.item_id = metadatavalue.item_id AND metadata_field_id = ? AND authority = ?",
            mdf.getFieldID(), value);
        return new ItemIterator(context, rows);
    }

}