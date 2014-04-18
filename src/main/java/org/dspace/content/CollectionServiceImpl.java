/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.app.util.AuthorizeUtil;
import org.dspace.authorize.*;
import org.dspace.content.dao.CollectionDAO;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.CommunityService;
import org.dspace.core.*;
import org.dspace.eperson.Group;
import org.dspace.eperson.service.GroupService;
import org.dspace.event.Event;
import org.dspace.factory.DSpaceServiceFactory;
import org.dspace.handle.HandleServiceImpl;
import org.dspace.handle.service.HandleService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;

/**
 * Class representing a collection.
 * <P>
 * The collection's metadata (name, introductory text etc), workflow groups, and
 * default group of submitters are loaded into memory. Changes to metadata are
 * not written to the database until <code>update</code> is called. If you
 * create or remove a workflow group, the change is only reflected in the
 * database after calling <code>update</code>. The default group of
 * submitters is slightly different - creating or removing this has instant
 * effect.
 *
 * @author Robert Tansley
 * @version $Revision$
 */
public class CollectionServiceImpl extends DSpaceObjectServiceImpl<Collection> implements CollectionService
{
    /** log4j category */
    private static Logger log = Logger.getLogger(Collection.class);

    @Autowired(required = true)
    protected CollectionDAO collectionDAO;

    @Autowired(required = true)
    protected ItemService itemService;
    @Autowired(required = true)
    protected CommunityService communityService;
    @Autowired(required = true)
    protected GroupService groupService;
    @Autowired(required = true)
    protected BitstreamService bitstreamService;
    @Autowired(required = true)
    protected HandleService handleService;


    /**
     * Construct a collection with the given table row
     *
     * @throws SQLException
     */
    public CollectionServiceImpl()
    {

    }

    /**
     * Get a collection from the database. Loads in the metadata
     *
     * @param context
     *            DSpace context object
     * @param id
     *            ID of the collection
     *
     * @return the collection, or null if the ID is invalid.
     * @throws SQLException
     */
    public Collection find(Context context, int id) throws SQLException
    {
        Collection collection = collectionDAO.findByID(context, Collection.class, id);
        if (collection == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug(LogManager.getHeader(context, "find_collection",
                        "not_found,collection_id=" + id));
            }

            return null;
        }

        // not null, return Collection
        if (log.isDebugEnabled())
        {
            log.debug(LogManager.getHeader(context, "find_collection",
                    "collection_id=" + id));
        }

        return collection;
    }

    public String getName(Collection dso) {
        return dso.getNameInternal();
    }

    /**
     * Create a new collection with a new ID.
     * Once created the collection is added to the given community
     *
     * @param context
     *            DSpace context object
     *
     * @return the newly created collection
     * @throws SQLException
     * @throws AuthorizeException
     */
    public Collection create(Context context, Community community) throws SQLException,
            AuthorizeException
    {
        return create(context, community, null);
    }

    /**
     * Create a new collection with the supplied handle and with a new ID.
     * Once created the collection is added to the given community
     *
     * @param context
     *            DSpace context object
     *
     * @param handle the pre-determined Handle to assign to the new community
     * @return the newly created collection
     * @throws SQLException
     * @throws AuthorizeException
     */
    public Collection create(Context context, Community community, String handle) throws SQLException,
            AuthorizeException
    {
        Collection newCollection = collectionDAO.create(context, new Collection());
        //Add our newly created collection to our community, authorization checks occur in THIS method
        communityService.addCollection(context, community, newCollection);

        //Update our community so we have a collection identifier
        if(handle == null)
        {
            handleService.createHandle(context, newCollection);
        }else{
            handleService.createHandle(context, newCollection, handle);
        }

        // create the default authorization policy for collections
        // of 'anonymous' READ
        Group anonymousGroup = groupService.find(context, 0);

        AuthorizeManager.createResourcePolicy(context, newCollection, anonymousGroup, null, Constants.READ);
        // now create the default policies for submitted items
        AuthorizeManager.createResourcePolicy(context, newCollection, anonymousGroup, null, Constants.DEFAULT_ITEM_READ);
        AuthorizeManager.createResourcePolicy(context, newCollection, anonymousGroup, null, Constants.DEFAULT_BITSTREAM_READ);

        context.addEvent(new Event(Event.CREATE, Constants.COLLECTION, newCollection.getID(), newCollection.getHandle(context)));

        log.info(LogManager.getHeader(context, "create_collection",
                "collection_id=" + newCollection.getID())
                + ",handle=" + newCollection.getHandle(context));

        collectionDAO.save(context, newCollection);
        return newCollection;
    }

    /**
     * Get all collections in the system. These are alphabetically sorted by
     * collection name.
     *
     * @param context
     *            DSpace context object
     *
     * @return the collections in the system
     * @throws SQLException
     */
    public List<Collection> findAll(Context context) throws SQLException
    {
        return collectionDAO.findAll(context, "name");
    }

    /**
     * Get all collections in the system. Adds support for limit and offset.
     * @param context
     * @param limit
     * @param offset
     * @return
     * @throws SQLException
     */
    public List<Collection> findAll(Context context, Integer limit, Integer offset) throws SQLException
    {
        return collectionDAO.findAll(context, "name", limit, offset);

    }


    /**
     * Get the in_archive items in this collection. The order is indeterminate.
     *
     * @return an iterator over the items in the collection.
     * @throws SQLException
     */
    public Iterator<Item> getItems(Context context, Collection collection) throws SQLException
    {
        return collectionDAO.getItems(context, collection, true);
    }

    /**
     * Get the in_archive items in this collection. The order is indeterminate.
     * Provides the ability to use limit and offset, for efficient paging.
     * @param limit Max number of results in set
     * @param offset Number of results to jump ahead by. 100 = 100th result is first, not 100th page.
     * @return an iterator over the items in the collection.
     * @throws SQLException
     */
    public Iterator<Item> getItems(Context context, Collection collection, Integer limit, Integer offset) throws SQLException
    {
        return collectionDAO.getItems(context, collection, true, limit, offset);
    }

    /**
     * Get all the items in this collection. The order is indeterminate.
     *
     * @return an iterator over the items in the collection.
     * @throws SQLException
     */
    public Iterator<Item> getAllItems(Context context, Collection collection) throws SQLException
    {
        return collectionDAO.getAllItems(context, collection);
    }

    /**
     * Set a metadata value
     *
     * @param value
     *            value to set the field to
     *
     * @exception IllegalArgumentException
     *                if the requested metadata field doesn't exist
     * @exception MissingResourceException
     */
    public void setName(Collection collection, String value) throws MissingResourceException
    {
        if (StringUtils.isBlank(value))
        {
            try
            {
                value = I18nUtil.getMessage("org.dspace.workflow.WorkflowManager.untitled");
            }
            catch (MissingResourceException e)
            {
                value = "Untitled";
            }
        }
        collection.setNameInteral(value);
    }

    /**
     * Give the collection a logo. Passing in <code>null</code> removes any
     * existing logo. You will need to set the format of the new logo bitstream
     * before it will work, for example to "JPEG". Note that
     * <code>update</code> will need to be called for the change to take
     * effect.  Setting a logo and not calling <code>update</code> later may
     * result in a previous logo lying around as an "orphaned" bitstream.
     *
     * @param  is the stream to use as the new logo
     *
     * @return   the new logo bitstream, or <code>null</code> if there is no
     *           logo (<code>null</code> was passed in)
     * @throws AuthorizeException
     * @throws IOException
     * @throws SQLException
     */
    public Bitstream setLogo(Context context, Collection collection, InputStream is) throws AuthorizeException,
            IOException, SQLException
    {
        // Check authorisation
        // authorized to remove the logo when DELETE rights
        // authorized when canEdit
        if (!((is == null) && AuthorizeManager.authorizeActionBoolean(
                context, collection, Constants.DELETE)))
        {
            canEdit(context, collection, true);
        }

        // First, delete any existing logo
        if (collection.getLogo() != null)
        {
            bitstreamService.delete(context, collection.getLogo());
        }

        if (is == null)
        {
            collection.setLogo(null);
            log.info(LogManager.getHeader(context, "remove_logo",
                    "collection_id=" + collection.getID()));
        }
        else
        {
            Bitstream newLogo = bitstreamService.create(context, is);
            collection.setLogo(newLogo);

            // now create policy for logo bitstream
            // to match our READ policy
            List<ResourcePolicy> policies = AuthorizeManager.getPoliciesActionFilter(context, collection, Constants.READ);
            AuthorizeManager.addPolicies(context, policies, newLogo);

            log.info(LogManager.getHeader(context, "set_logo",
                    "collection_id=" + collection.getID() + "logo_bitstream_id="
                            + newLogo.getID()));
        }

        return collection.getLogo();
    }

    /**
     * Create a workflow group for the given step if one does not already exist.
     * Returns either the newly created group or the previously existing one.
     * Note that while the new group is created in the database, the association
     * between the group and the collection is not written until
     * <code>update</code> is called.
     *
     * @param step
     *            the step (1-3) of the workflow to create or get the group for
     *
     * @return the workflow group associated with this collection
     * @throws SQLException
     * @throws AuthorizeException
     */
    public Group createWorkflowGroup(Context context, Collection collection, int step) throws SQLException,
            AuthorizeException, IllegalAccessException {
        // Check authorisation - Must be an Admin to create Workflow Group
        AuthorizeUtil.authorizeManageWorkflowsGroup(context, collection);

        if (getWorkflowGroup(collection, step) == null)
        {
            //turn off authorization so that Collection Admins can create Collection Workflow Groups
            context.turnOffAuthorisationSystem();
            Group g = groupService.create(context);
            context.restoreAuthSystemState();

            g.setName("COLLECTION_" + collection.getID() + "_WORKFLOW_STEP_" + step);
            groupService.update(context, g);
            setWorkflowGroup(collection, step, g);

            AuthorizeManager.addPolicy(context, collection, Constants.ADD, g);
        }

        return getWorkflowGroup(collection, step);
    }

    /**
     * Set the workflow group corresponding to a particular workflow step.
     * <code>null</code> can be passed in if there should be no associated
     * group for that workflow step; any existing group is NOT deleted.
     *
     * @param step
     *            the workflow step (1-3)
     * @param g
     *            the new workflow group, or <code>null</code>
     */
    public void setWorkflowGroup(Collection collection, int step, Group g)
    {
        switch (step)
        {
            case 1:
                collection.setWorkflowStep1(g);
                break;
            case 2:
                collection.setWorkflowStep2(g);
                break;
            case 3:
                collection.setWorkflowStep3(g);
                break;
            default:
                new IllegalAccessException("Illegal step count: " + step);
        }
    }

    /**
     * Get the the workflow group corresponding to a particular workflow step.
     * This returns <code>null</code> if there is no group associated with
     * this collection for the given step.
     *
     * @param step
     *            the workflow step (1-3)
     *
     * @return the group of reviewers or <code>null</code>
     */
    public Group getWorkflowGroup(Collection collection, int step) throws IllegalStateException {
        switch (step)
        {
            case 1:
                return collection.getWorkflowStep1();
            case 2:
                return collection.getWorkflowStep2();
            case 3:
                return collection.getWorkflowStep3();
            default:
                throw new IllegalStateException("Illegal step count: " + step);
        }
    }

    /**
     * Create a default submitters group if one does not already exist. Returns
     * either the newly created group or the previously existing one. Note that
     * other groups may also be allowed to submit to this collection by the
     * authorization system.
     *
     * @return the default group of submitters associated with this collection
     * @throws SQLException
     * @throws AuthorizeException
     */
    public Group createSubmitters(Context context, Collection collection) throws SQLException, AuthorizeException
    {
        // Check authorisation - Must be an Admin to create Submitters Group
        AuthorizeUtil.authorizeManageSubmittersGroup(context, collection);

        Group submitters = collection.getSubmitters();
        if (submitters == null)
        {
            //turn off authorization so that Collection Admins can create Collection Submitters
            context.turnOffAuthorisationSystem();
            submitters = groupService.create(context);
            context.restoreAuthSystemState();

            submitters.setName("COLLECTION_" + collection.getID() + "_SUBMIT");
            groupService.update(context, submitters);

            // register this as the submitter group
            collection.setSubmitters(submitters);
            AuthorizeManager.addPolicy(context, collection, Constants.ADD, submitters);


        }
        return submitters;
    }

    /**
     * Remove the submitters group, if no group has already been created
     * then return without error. This will merely dereference the current
     * submitters group from the collection so that it may be deleted
     * without violating database constraints.
     */
    public void removeSubmitters(Context context, Collection collection) throws SQLException, AuthorizeException
    {
    	// Check authorisation - Must be an Admin to delete Submitters Group
        AuthorizeUtil.authorizeManageSubmittersGroup(context, collection);

        // just return if there is no administrative group.
        if (collection.getSubmitters() == null)
        {
            return;
        }

        // Remove the link to the collection table.
        collection.setSubmitters(null);
    }

    /**
     * Create a default administrators group if one does not already exist.
     * Returns either the newly created group or the previously existing one.
     * Note that other groups may also be administrators.
     *
     * @return the default group of editors associated with this collection
     * @throws SQLException
     * @throws AuthorizeException
     */
    public Group createAdministrators(Context context, Collection collection) throws SQLException, AuthorizeException
    {
        // Check authorisation - Must be an Admin to create more Admins
        AuthorizeUtil.authorizeManageAdminGroup(context, collection);

        Group admins = collection.getSubmitters();
        if (admins == null)
        {
            //turn off authorization so that Community Admins can create Collection Admins
            context.turnOffAuthorisationSystem();
            admins = groupService.create(context);
            context.restoreAuthSystemState();

            admins.setName("COLLECTION_" + collection.getID() + "_ADMIN");
            groupService.update(context, admins);
        }

        AuthorizeManager.addPolicy(context, collection,
                Constants.ADMIN, admins);

        // register this as the admin group
        collection.setAdmins(admins);

        return admins;
    }

    /**
     * Remove the administrators group, if no group has already been created
     * then return without error. This will merely dereference the current
     * administrators group from the collection so that it may be deleted
     * without violating database constraints.
     */
    public void removeAdministrators(Context context, Collection collection) throws SQLException, AuthorizeException
    {
        // Check authorisation - Must be an Admin of the parent community to delete Admin Group
        AuthorizeUtil.authorizeRemoveAdminGroup(context, collection);

        Group admins = collection.getAdministrators();
        // just return if there is no administrative group.
        if (admins == null)
        {
            return;
        }

        // Remove the link to the collection table.
        collection.setAdmins(null);
    }

    /**
     * Get the license that users must grant before submitting to this
     * collection. If the collection does not have a specific license, the
     * site-wide default is returned.
     *
     * @return the license for this collection
     */
    public String getLicense(Collection collection)
    {
        String license = collection.getLicenseInternal();

        if (license == null || license.trim().equals(""))
        {
            // Fallback to site-wide default
            license = LicenseManager.getDefaultSubmissionLicense();
        }

        return license;
    }

    /**
     * Get the license that users must grant before submitting to this
     * collection.
     *
     * @return the license for this collection
     */
    public String getLicenseCollection(Collection collection)
    {
        return collection.getLicenseInternal();
    }

    /**
     * Find out if the collection has a custom license
     *
     * @return <code>true</code> if the collection has a custom license
     */
    public boolean hasCustomLicense(Collection collection)
    {
        String license = collection.getLicenseInternal();
        return StringUtils.isNotBlank(license);
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
    public Item createTemplateItem(Context context, Collection collection) throws SQLException, AuthorizeException
    {
        // Check authorisation
        AuthorizeUtil.authorizeManageTemplateItem(context, collection);

        if (collection.getTemplateItem() == null)
        {
            Item template = itemService.createTemplateItem(context, collection);

            log.info(LogManager.getHeader(context, "create_template_item",
                    "collection_id=" + collection.getID() + ",template_item_id="
                            + template.getID()));
        }
        return collection.getTemplateItem();
    }

    /**
     * Remove the template item for this collection, if there is one. Note that
     * since this has to remove the old template item ID from the collection
     * record in the database, the collection record will be changed, including
     * any other changes made; in other words, this method does an
     * <code>update</code>.
     *
     * @throws SQLException
     * @throws AuthorizeException
     * @throws IOException
     */
    public void removeTemplateItem(Context context, Collection collection) throws SQLException, AuthorizeException,
            IOException
    {
        // Check authorisation
        AuthorizeUtil.authorizeManageTemplateItem(context, collection);

        Item template = collection.getTemplateItem();

        if (template != null)
        {
            log.info(LogManager.getHeader(context, "remove_template_item",
                    "collection_id=" + collection.getID() + ",template_item_id="
                            + template.getID()));
            // temporarily turn off auth system, we have already checked the permission on the top of the method
            // check it again will fail because we have already broken the relation between the collection and the item
            context.turnOffAuthorisationSystem();
            itemService.delete(context, template);
            context.restoreAuthSystemState();
            collection.setTemplate(null);
        }

        context.addEvent(new Event(Event.MODIFY, Constants.COLLECTION, collection.getID(), "remove_template_item"));
    }

    public Collection findByTemplateItem(Context context, Item item) throws SQLException {
        return collectionDAO.findByTemplateItem(context, item);
    }

    /**
     * Add an item to the collection. This simply adds a relationship between
     * the item and the collection - it does nothing like set an issue date,
     * remove a personal workspace item etc. This has instant effect;
     * <code>update</code> need not be called.
     *
     * @param item
     *            item to add
     * @throws SQLException
     * @throws AuthorizeException
     */
    public void addItem(Context context, Collection collection, Item item) throws SQLException, AuthorizeException
    {
        // Check authorisation
        AuthorizeManager.authorizeAction(context, collection, Constants.ADD);

        log.info(LogManager.getHeader(context, "add_item", "collection_id="
                + collection.getID() + ",item_id=" + item.getID()));

        // We do NOT add the item to the collection template since we would have to load in all our items
        // Instead we add the collection to an item which works in the same way.
        if(!item.getCollections().contains(collection))
        {
            item.addCollection(collection);
        }

        context.addEvent(new Event(Event.ADD, Constants.COLLECTION, collection.getID(), Constants.ITEM, item.getID(), item.getHandle(context)));
    }

    /**
     * Remove an item. If the item is then orphaned, it is deleted.
     *
     * @param item
     *            item to remove
     * @throws SQLException
     * @throws AuthorizeException
     * @throws IOException
     */
    public void removeItem(Context context, Collection collection, Item item) throws SQLException, AuthorizeException,
            IOException
    {
        // Check authorisation
        AuthorizeManager.authorizeAction(context, collection, Constants.REMOVE);

        //Remove the item from the collection
        item.removeCollection(collection);

        //TODO: HIBERNATE DO WE NEED TO REFRESH COLLECTION TO REFLECT THE CHANGE ?

        //Check if we orphaned our poor item
        if (item.getCollections().size() == 0)
        {
            // Orphan; delete it
            itemService.delete(context, item);
        }
        log.info(LogManager.getHeader(context, "remove_item",
                "collection_id=" + collection.getID() + ",item_id=" + item.getID()));

        context.addEvent(new Event(Event.REMOVE, Constants.COLLECTION, collection.getID(), Constants.ITEM, item.getID(), item.getHandle(context)));
    }

    public void updateLastModified(Context context, Collection collection) {
        //Also fire a modified event since the collection HAS been modified
        context.addEvent(new Event(Event.MODIFY, Constants.COLLECTION, collection.getID(), null));
    }

    /**
     * Update the collection metadata (including logo and workflow groups) to
     * the database. Inserts if this is a new collection.
     *
     * @throws SQLException
     * @throws IOException
     * @throws AuthorizeException
     */
    public void update(Context context, Collection collection) throws SQLException, AuthorizeException
    {
        // Check authorisation
        canEdit(context, collection, true);

        log.info(LogManager.getHeader(context, "update_collection",
                "collection_id=" + collection.getID()));

        collectionDAO.save(context, collection);
        if (collection.isModified())
        {
            context.addEvent(new Event(Event.MODIFY, Constants.COLLECTION, collection.getID(), null));
            collection.clearModified();
        }
        if (collection.isModifiedMetadata())
        {
            context.addEvent(new Event(Event.MODIFY_METADATA, Constants.COLLECTION, collection.getID(), collection.getDetails()));
            collection.clearModifiedMetadata();
            collection.clearDetails();
        }
    }

    public boolean canEditBoolean(Context context, Collection collection) throws java.sql.SQLException
    {
        return canEditBoolean(context, collection, true);
    }

    public boolean canEditBoolean(Context context, Collection collection, boolean useInheritance) throws java.sql.SQLException
    {
        try
        {
            canEdit(context, collection, useInheritance);

            return true;
        }
        catch (AuthorizeException e)
        {
            return false;
        }
    }

    public void canEdit(Context context, Collection collection)  throws AuthorizeException, SQLException
    {
        canEdit(context, collection, true);
    }

    public void canEdit(Context context, Collection collection, boolean useInheritance) throws AuthorizeException, SQLException
    {
        List<Community> parents = collection.getCommunities();
        for (Community parent : parents) {
            if (AuthorizeManager.authorizeActionBoolean(context, parent,
                    Constants.WRITE, useInheritance)) {
                return;
            }

            if (AuthorizeManager.authorizeActionBoolean(context, parent,
                    Constants.ADD, useInheritance)) {
                return;
            }
        }
        AuthorizeManager.authorizeAction(context, collection, Constants.WRITE, useInheritance);
    }

    /**
     * Delete the collection, including the metadata and logo. Items that are
     * then orphans are deleted. Groups associated with this collection
     * (workflow participants and submitters) are NOT deleted.
     *
     * @throws SQLException
     * @throws AuthorizeException
     * @throws IOException
     */
    //TODO: create unit test for this !
    public void delete(Context context, Collection collection) throws SQLException, AuthorizeException, IOException
    {
        log.info(LogManager.getHeader(context, "delete_collection",
                "collection_id=" + collection.getID()));
        //Check community delete rights, we should be able to remove the collection from EVERY community we are linked to
        //TODO: move this to the community impl ?
        List<Community> communities = collection.getCommunities();
        for (Community community : communities) {
            AuthorizeManager.authorizeAction(context, community, Constants.REMOVE);
        }
        collection.getCommunities().clear();

        context.addEvent(new Event(Event.DELETE, Constants.COLLECTION, collection.getID(), collection.getHandle(context)));

        // remove subscriptions - hmm, should this be in Subscription.java?
        //TODO: HIBERNATE DESCRIPTION
//        DatabaseManager.updateQuery(context,
//                "DELETE FROM subscription WHERE collection_id= ? ",
//                getID());

        // Remove Template Item
        removeTemplateItem(context, collection);

        // Remove items
        Iterator<Item> items = getAllItems(context, collection);

        while (items.hasNext())
        {
            Item item = items.next();
            if (itemService.isOwningCollection(item, collection))
            {
                // the collection to be deleted is the owning collection, thus remove
                // the item from all collections it belongs to
                List<Collection> collections = item.getCollections();
                for (Collection coll : collections)
                {
                    // Browse.itemRemoved(ourContext, itemId);
                    removeItem(context, coll, item);
                }

            }
            // the item was only mapped to this collection, so just remove it
            else
            {
                // Browse.itemChanged(ourContext, item);
                removeItem(context, collection, item);
            }
        }

        // Delete bitstream logo
        setLogo(context, collection, null);

        // Remove all authorization policies
        AuthorizeManager.removeAllPolicies(context, collection);

        /*
        TODO: HIBERNATE, IMPLEMENT WHEN WORKFLOW BECOMES AVAILABLE
        if(ConfigurationManager.getProperty("workflow","workflow.framework").equals("xmlworkflow")){
            // Remove any xml_WorkflowItems
            XmlWorkflowItem[] xmlWfarray = XmlWorkflowItem
                    .findByCollection(ourContext, this);

            for (XmlWorkflowItem aXmlWfarray : xmlWfarray) {
                // remove the workflowitem first, then the item
                Item myItem = aXmlWfarray.getItem();
                aXmlWfarray.deleteWrapper();
                myItem.delete();
            }
        }else{
            // Remove any WorkflowItems
            WorkflowItem[] wfarray = WorkflowItem
                    .findByCollection(ourContext, this);

            for (WorkflowItem aWfarray : wfarray) {
                // remove the workflowitem first, then the item
                Item myItem = aWfarray.getItem();
                aWfarray.deleteWrapper();
                myItem.delete();
            }
        }



        // Remove any WorkspaceItems
        WorkspaceItem[] wsarray = WorkspaceItem.findByCollection(ourContext,
                this);

        for (WorkspaceItem aWsarray : wsarray) {
            aWsarray.deleteAll();
        }
        */

        //  get rid of the content count cache if it exists
        //TODO: HIBERNATE IMPLEMENT ITEM COUNTER
        /*
        try
        {
        	ItemCounter ic = new ItemCounter(ourContext);
        	ic.remove(this);
        }
        catch (ItemCountException e)
        {
        	// FIXME: upside down exception handling due to lack of good
        	// exception framework
        	throw new IllegalStateException(e.getMessage(), e);
        }
         */
        // Remove any Handle
        handleService.unbindHandle(context, collection);

                /*
        TODO: HIBERNATE, IMPLEMENT WHEN WORKFLOW BECOMES AVAILABLE

        if(ConfigurationManager.getProperty("workflow","workflow.framework").equals("xmlworkflow")){
            // delete all CollectionRoles for this Collection
            for (CollectionRole collectionRole : CollectionRole.findByCollection(context, collection.getID())) {
                collectionRole.delete();
            }
        }

        // Delete collection row
        DatabaseManager.delete(ourContext, collectionRow);
*/
        // Remove any workflow groups - must happen after deleting collection
        Group g = null;

        g = collection.getWorkflowStep1();

        if (g != null)
        {
            groupService.delete(context, g);
        }

        g = collection.getWorkflowStep2();

        if (g != null)
        {
            groupService.delete(context, g);
        }

        g = collection.getWorkflowStep3();

        if (g != null)
        {
            groupService.delete(context, g);
        }

        // Remove default administrators group
        g = collection.getAdministrators();

        if (g != null)
        {
            groupService.delete(context, g);
        }

        // Remove default submitters group
        g = collection.getSubmitters();

        if (g != null)
        {
            groupService.delete(context, g);
        }
        collectionDAO.delete(context, collection);
    }

    /**
     * return an array of collections that user has a given permission on
     * (useful for trimming 'select to collection' list) or figuring out which
     * collections a person is an editor for.
     *
     * @param context
     * @param comm
     *            (optional) restrict search to a community, else null
     * @param actionID
     *            of the action
     *
     * @return Collection [] of collections with matching permissions
     * @throws SQLException
     */
    public List<Collection> findAuthorized(Context context, Community comm,
            int actionID) throws java.sql.SQLException
    {
        List<Collection> myResults = new ArrayList<Collection>();

        java.util.Collection<Collection> myCollections = null;

        if (comm != null)
        {
            myCollections = comm.getCollections();
        }
        else
        {
            myCollections = findAll(context);
        }

        // now build a list of collections you have authorization for
        for (Collection myCollection : myCollections) {
            if (AuthorizeManager.authorizeActionBoolean(context,
                    myCollection, actionID)) {
                myResults.add(myCollection);
            }
        }
        return myResults;
    }

	/**
     * counts items in this collection
     *
     * @return  total items
     */
    //TODO: HIBERNATE ITEM COUNT
    /*
    public int countItems()
        throws SQLException
     {
         int itemcount = 0;
         PreparedStatement statement = null;
         ResultSet rs = null;

         try
         {
             String query = "SELECT count(*) FROM collection2item, item WHERE "
                    + "collection2item.collection_id =  ? "
                    + "AND collection2item.item_id = item.item_id "
                    + "AND in_archive ='1' AND item.withdrawn='0' ";

            statement = ourContext.getDBConnection().prepareStatement(query);
            statement.setInt(1,getID());

            rs = statement.executeQuery();
            if (rs != null)
            {
                rs.next();
                itemcount = rs.getInt(1);
            }
         }
         finally
         {
             if (rs != null)
             {
                 try { rs.close(); } catch (SQLException sqle) { }
             }

             if (statement != null)
             {
                 try { statement.close(); } catch (SQLException sqle) { }
             }
         }

        return itemcount;
     }
     */

    /**
     * Get the collections this item is not in.
     *
     * @return the collections this item is not in, if any.
     * @throws SQLException
     */
    public List<Collection> getCollectionsNotLinked(Context context, Item item) throws SQLException
    {
        List<Collection> allCollections = findAll(context);
        List<Collection> linkedCollections = item.getCollections();
        List<Collection> notLinkedCollections = new ArrayList<Collection>(allCollections.size() - linkedCollections.size());

        if ((allCollections.size() - linkedCollections.size()) == 0)
        {
            return notLinkedCollections;
        }
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
                     notLinkedCollections.add(collection);
                 }
        }

        return notLinkedCollections;
    }

    /**
     * Moves the item from one collection to another one
     *
     * @throws SQLException
     * @throws AuthorizeException
     * @throws IOException
     */
    public void move(Context context, Item item, Collection from, Collection to) throws SQLException, AuthorizeException, IOException
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
    public void move(Context context, Item item, Collection from, Collection to, boolean inheritDefaultPolicies) throws SQLException, AuthorizeException, IOException
    {
        // Check authorisation on the item before that the move occur
        // otherwise we will need edit permission on the "target collection" to archive our goal
        // only do write authorization if user is not an editor
        if (!itemService.canEdit(context, item))
        {
            AuthorizeManager.authorizeAction(context, item, Constants.WRITE);
        }

        // Move the Item from one Collection to the other
        addItem(context, to, item);
        removeItem(context, from, item);

        // If we are moving from the owning collection, update that too
        if (itemService.isOwningCollection(item, from))
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
                itemService.inheritCollectionDefaultPolicies(context, item, to);
            }

            // Update the item
            context.turnOffAuthorisationSystem();
            itemService.update(context, item);
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




    public DSpaceObject getAdminObject(Context context, Collection collection, int action) throws SQLException
    {
        DSpaceObject adminObject = null;
        List<Community> communities = collection.getCommunities();
        Community community = null;
        if(CollectionUtils.isNotEmpty(communities))
        {
            community = communities.iterator().next();
        }

        switch (action)
        {
            case Constants.REMOVE:
                if (AuthorizeConfiguration.canCollectionAdminPerformItemDeletion())
                {
                    adminObject = collection;
                }
                else if (AuthorizeConfiguration.canCommunityAdminPerformItemDeletion())
                {
                    adminObject = community;
                }
                break;

            case Constants.DELETE:
                if (AuthorizeConfiguration.canCommunityAdminPerformSubelementDeletion())
                {
                    adminObject = community;
                }
                break;
            default:
                adminObject = collection;
                break;
        }
        return adminObject;
    }

    @Override
    public DSpaceObject getParentObject(Context context, Collection collection) throws SQLException
    {
        List<Community> communities = collection.getCommunities();
        if(CollectionUtils.isNotEmpty(communities)){
            return communities.iterator().next();
        }else{
            return null;
        }
    }
}