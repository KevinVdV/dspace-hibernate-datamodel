/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.authorize.*;
import org.dspace.core.*;
import org.dspace.eperson.Group;
import org.dspace.eperson.GroupDAO;
import org.dspace.event.Event;
import org.dspace.handle.HandleManager;
import org.dspace.hibernate.HibernateQueryUtil;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;

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
public class CollectionDAO extends DSpaceObjectDAO<Collection>
{
    /** log4j category */
    private static Logger log = Logger.getLogger(Collection.class);

    /** The logo bitstream */
    //TODO: HIBERNATE: iMPLMENT WHEN BITSTREAM BECOMES AVAILABLE
//    private Bitstream logo;

    /** The item template */
        //TODO: HIBERNATE: iMPLMENT WHEN ITEM BECOMES AVAILABLE
//    private Item template;

    /**
     * Construct a collection with the given table row
     *
     * @throws SQLException
     */
    public CollectionDAO() throws SQLException
    {
        clearDetails();
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
        Collection collection = (Collection) context.getDBConnection().get(Collection.class, id);
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

    /**
     * Create a new collection, with a new ID. This method is not public, and
     * does not check authorisation.
     *
     * @param context
     *            DSpace context object
     *
     * @return the newly created collection
     * @throws SQLException
     * @throws AuthorizeException
     */
    Collection create(Context context) throws SQLException,
            AuthorizeException
    {
        return create(context, null);
    }

    /**
     * Create a new collection, with a new ID. This method is not public, and
     * does not check authorisation.
     *
     * @param context
     *            DSpace context object
     *
     * @param handle the pre-determined Handle to assign to the new community
     * @return the newly created collection
     * @throws SQLException
     * @throws AuthorizeException
     */
    Collection create(Context context, String handle) throws SQLException,
            AuthorizeException
    {
        Collection newCollection = new Collection();
        try
        {
            newCollection.handle = (handle == null) ?
                       HandleManager.createHandle(context, newCollection) :
                       HandleManager.createHandle(context, newCollection, handle);
        }
        catch(IllegalStateException ie)
        {
            //If an IllegalStateException is thrown, then an existing object is already using this handle
            //pass exception on up the chain
            throw ie;
        }

        // create the default authorization policy for collections
        // of 'anonymous' READ
        Group anonymousGroup = new GroupDAO().find(context, 0);

        ResourcePolicyDAO resourcePolicyDAO = new ResourcePolicyDAO();
        ResourcePolicy myPolicy = resourcePolicyDAO.create(context);
        resourcePolicyDAO.setResource(myPolicy, newCollection);
        myPolicy.setAction(Constants.READ);
        resourcePolicyDAO.setGroup(myPolicy, anonymousGroup);
        resourcePolicyDAO.update(context, myPolicy);

        // now create the default policies for submitted items
        myPolicy = resourcePolicyDAO.create(context);
        resourcePolicyDAO.setResource(myPolicy, newCollection);
        myPolicy.setAction(Constants.DEFAULT_ITEM_READ);
        resourcePolicyDAO.setGroup(myPolicy, anonymousGroup);
        resourcePolicyDAO.update(context, myPolicy);

        myPolicy = resourcePolicyDAO.create(context);
        resourcePolicyDAO.setResource(myPolicy, newCollection);
        myPolicy.setAction(Constants.DEFAULT_BITSTREAM_READ);
        resourcePolicyDAO.setGroup(myPolicy, anonymousGroup);
        resourcePolicyDAO.update(context, myPolicy);

        context.addEvent(new Event(Event.CREATE, Constants.COLLECTION, newCollection.getID(), newCollection.handle));

        log.info(LogManager.getHeader(context, "create_collection",
                "collection_id=" + newCollection.getID())
                + ",handle=" + newCollection.handle);

        HibernateQueryUtil.update(context, newCollection);
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
    public Collection[] findAll(Context context) throws SQLException
    {
        Criteria criteria = context.getDBConnection().createCriteria(Collection.class);
        criteria.addOrder(Order.asc("name"));
        List<Collection> collections = criteria.list();
        return collections.toArray(new Collection[collections.size()]);
    }

    /**
     * Get all collections in the system. Adds support for limit and offset.
     * @param context
     * @param limit
     * @param offset
     * @return
     * @throws SQLException
     */
    public Collection[] findAll(Context context, Integer limit, Integer offset) throws SQLException
    {
        Criteria criteria = context.getDBConnection().createCriteria(Collection.class);
        criteria.addOrder(Order.asc("name"));
        criteria.setFirstResult(offset);
        criteria.setMaxResults(limit);
        List<Collection> collections = criteria.list();
        return collections.toArray(new Collection[collections.size()]);

    }


    /**
     * Get the in_archive items in this collection. The order is indeterminate.
     *
     * @return an iterator over the items in the collection.
     * @throws SQLException
     */
    //TODO: Implement when item becomes available
    /*
    public ItemIterator getItems() throws SQLException
    {
        String myQuery = "SELECT item.* FROM item, collection2item WHERE "
                + "item.item_id=collection2item.item_id AND "
                + "collection2item.collection_id= ? "
                + "AND item.in_archive='1'";

        TableRowIterator rows = DatabaseManager.queryTable(ourContext, "item",
                myQuery,getID());

        return new ItemIterator(ourContext, rows);
    }
     */

    /**
     * Get the in_archive items in this collection. The order is indeterminate.
     * Provides the ability to use limit and offset, for efficient paging.
     * @param limit Max number of results in set
     * @param offset Number of results to jump ahead by. 100 = 100th result is first, not 100th page.
     * @return an iterator over the items in the collection.
     * @throws SQLException
     */
    //TODO: Implement when item becomes available
    /*
    public ItemIterator getItems(Integer limit, Integer offset) throws SQLException
    {
        String myQuery = "SELECT item.* FROM item, collection2item WHERE "
                + "item.item_id=collection2item.item_id AND "
                + "collection2item.collection_id= ? "
                + "AND item.in_archive='1' limit ? offset ?";

        TableRowIterator rows = DatabaseManager.queryTable(ourContext, "item",
                myQuery,getID(), limit, offset);

        return new ItemIterator(ourContext, rows);
    }
    */

    /**
     * Get all the items in this collection. The order is indeterminate.
     *
     * @return an iterator over the items in the collection.
     * @throws SQLException
     */
        //TODO: Implement when item becomes available
    /*
    public ItemIterator getAllItems() throws SQLException
    {
        String myQuery = "SELECT item.* FROM item, collection2item WHERE "
                + "item.item_id=collection2item.item_id AND "
                + "collection2item.collection_id= ? ";

        TableRowIterator rows = DatabaseManager.queryTable(ourContext, "item",
                myQuery,getID());

        return new ItemIterator(ourContext, rows);
    }
    */

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
        collection.setName(value);
        addDetails(field);
    }

    /**
     * Get the logo for the collection. <code>null</code> is returned if the
     * collection does not have a logo.
     *
     * @return the logo of the collection, or <code>null</code>
     */
    //TODO: Implement once bitstream becomes available
    /*
    public Bitstream getLogo()
    {
        return logo;
    }
    */
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
        //TODO: Implement once bitstream becomes available
/*
    public Bitstream setLogo(InputStream is) throws AuthorizeException,
            IOException, SQLException
    {
        // Check authorisation
        // authorized to remove the logo when DELETE rights
        // authorized when canEdit
        if (!((is == null) && AuthorizeManager.authorizeActionBoolean(
                ourContext, this, Constants.DELETE)))
        {
            canEdit(true);
        }

        // First, delete any existing logo
        if (!collectionRow.isColumnNull("logo_bitstream_id"))
        {
            logo.delete();
        }

        if (is == null)
        {
            collectionRow.setColumnNull("logo_bitstream_id");
            logo = null;

            log.info(LogManager.getHeader(ourContext, "remove_logo",
                    "collection_id=" + getID()));
        }
        else
        {
            Bitstream newLogo = Bitstream.create(ourContext, is);
            collectionRow.setColumn("logo_bitstream_id", newLogo.getID());
            logo = newLogo;

            // now create policy for logo bitstream
            // to match our READ policy
            List<ResourcePolicy> policies = AuthorizeManager.getPoliciesActionFilter(ourContext, this, Constants.READ);
            AuthorizeManager.addPolicies(ourContext, policies, newLogo);

            log.info(LogManager.getHeader(ourContext, "set_logo",
                    "collection_id=" + getID() + "logo_bitstream_id="
                            + newLogo.getID()));
        }

        modified = true;
        return logo;
    }
    */

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
//TODO: Hibernate fix when authorizeUtil becomes available
//        AuthorizeUtil.authorizeManageWorkflowsGroup(context, this);

        if (getWorkflowGroup(collection, step) == null)
        {
            //turn off authorization so that Collection Admins can create Collection Workflow Groups
            context.turnOffAuthorisationSystem();
            GroupDAO groupDAO = new GroupDAO();
            Group g = groupDAO.create(context);
            context.restoreAuthSystemState();

            g.setName("COLLECTION_" + collection.getID() + "_WORKFLOW_STEP_" + step);
            groupDAO.update(context, g);
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
    public Group getWorkflowGroup(Collection collection, int step) throws IllegalAccessException {
        switch (step)
        {
            case 1:
                return collection.getWorkflowStep1();
            case 2:
                return collection.getWorkflowStep2();
            case 3:
                return collection.getWorkflowStep3();
            default:
                throw new IllegalAccessException("Illegal step count: " + step);
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
        //TODO: Hibernate fix when authorizeUtil becomes available
//        AuthorizeUtil.authorizeManageSubmittersGroup(context, this);

        Group submitters = collection.getSubmitters();
        if (submitters == null)
        {
            //turn off authorization so that Collection Admins can create Collection Submitters
            context.turnOffAuthorisationSystem();
            GroupDAO groupDAO = new GroupDAO();
            submitters = groupDAO.create(context);
            context.restoreAuthSystemState();

            submitters.setName("COLLECTION_" + collection.getID() + "_SUBMIT");
            groupDAO.update(context, submitters);

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
        //TODO: Hibernate fix when authorizeUtil becomes available
//        AuthorizeUtil.authorizeManageSubmittersGroup(context, collection);

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
        //TODO: Hibernate fix when authorizeUtil becomes available
//        AuthorizeUtil.authorizeManageAdminGroup(context, this);

        Group admins = collection.getSubmitters();
        if (admins == null)
        {
            //turn off authorization so that Community Admins can create Collection Admins
            context.turnOffAuthorisationSystem();
            GroupDAO groupDAO = new GroupDAO();
            admins = groupDAO.create(context);
            context.restoreAuthSystemState();

            admins.setName("COLLECTION_" + collection.getID() + "_ADMIN");
            groupDAO.update(context, admins);
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
        //TODO: Hibernate fix when authorizeUtil becomes available
//        AuthorizeUtil.authorizeRemoveAdminGroup(context, this);

        Group admins = collection.getAdmins();
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
        String license = collection.getLicense();

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
        return collection.getLicense();
    }

    /**
     * Find out if the collection has a custom license
     *
     * @return <code>true</code> if the collection has a custom license
     */
    public boolean hasCustomLicense(Collection collection)
    {
        String license = collection.getLicense();

        return StringUtils.isNotBlank(license);
    }

    /**
     * Get the template item for this collection. <code>null</code> is
     * returned if the collection does not have a template. Submission
     * mechanisms may copy this template to provide a convenient starting point
     * for a submission.
     *
     * @return the item template, or <code>null</code>
     */
    /*
    //TODO: HIBERNATE: Implement when item becomes available
    public Item getTemplateItem() throws SQLException
    {
        return template;
    }
     */

    /**
     * Create an empty template item for this collection. If one already exists,
     * no action is taken. Caution: Make sure you call <code>update</code> on
     * the collection after doing this, or the item will have been created but
     * the collection record will not refer to it.
     *
     * @throws SQLException
     * @throws AuthorizeException
     */
        //TODO: HIBERNATE: Implement when item becomes available
/*
    public void createTemplateItem() throws SQLException, AuthorizeException
    {
        // Check authorisation
        AuthorizeUtil.authorizeManageTemplateItem(ourContext, this);

        if (template == null)
        {
            template = Item.create(ourContext);
            collectionRow.setColumn("template_item_id", template.getID());

            log.info(LogManager.getHeader(ourContext, "create_template_item",
                    "collection_id=" + getID() + ",template_item_id="
                            + template.getID()));
        }
        modified = true;
    }

*/
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
            //TODO: HIBERNATE: Implement when item becomes available
/*

    public void removeTemplateItem() throws SQLException, AuthorizeException,
            IOException
    {
        // Check authorisation
        AuthorizeUtil.authorizeManageTemplateItem(ourContext, this);

        collectionRow.setColumnNull("template_item_id");
        DatabaseManager.update(ourContext, collectionRow);

        if (template != null)
        {
            log.info(LogManager.getHeader(ourContext, "remove_template_item",
                    "collection_id=" + getID() + ",template_item_id="
                            + template.getID()));
            // temporarily turn off auth system, we have already checked the permission on the top of the method
            // check it again will fail because we have already broken the relation between the collection and the item
            ourContext.turnOffAuthorisationSystem();
            template.delete();
            ourContext.restoreAuthSystemState();
            template = null;
        }

        ourContext.addEvent(new Event(Event.MODIFY, Constants.COLLECTION, getID(), "remove_template_item"));
    }
    */

    public Collection findByTemplateItem(Context context, Item item) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(Collection.class);
        criteria.add(Restrictions.eq("template_item_id", item.getID()));
        return (Collection) criteria.uniqueResult();
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
        item.addCollection(collection);

        HibernateQueryUtil.refresh(context, collection);


        context.addEvent(new Event(Event.ADD, Constants.COLLECTION, collection.getID(), Constants.ITEM, item.getID(), item.getHandle()));
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
            new ItemDAO().delete(context, item);
        }
        log.info(LogManager.getHeader(context, "remove_item",
                "collection_id=" + collection.getID() + ",item_id=" + item.getID()));

        context.addEvent(new Event(Event.REMOVE, Constants.COLLECTION, collection.getID(), Constants.ITEM, item.getID(), item.getHandle()));
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

        HibernateQueryUtil.update(context, collection);
        if (collection.isModified())
        {
            context.addEvent(new Event(Event.MODIFY, Constants.COLLECTION, collection.getID(), null));
            collection.clearModified();
        }
        if (collection.isModifiedMetadata())
        {
            context.addEvent(new Event(Event.MODIFY_METADATA, Constants.COLLECTION, collection.getID(), getDetails()));
            collection.clearModifiedMetadata();
            clearDetails();
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
        Community[] parents = getCommunities();

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
    void delete(Context context, Collection collection) throws SQLException, AuthorizeException, IOException
    {
        log.info(LogManager.getHeader(context, "delete_collection",
                "collection_id=" + collection.getID()));

        context.addEvent(new Event(Event.DELETE, Constants.COLLECTION, collection.getID(), getHandle()));

        // remove subscriptions - hmm, should this be in Subscription.java?
        //TODO: HIBERNATE DESCRIPTION
//        DatabaseManager.updateQuery(context,
//                "DELETE FROM subscription WHERE collection_id= ? ",
//                getID());

        // Remove Template Item
        //TODO: HIBERNATE, IMPLEMENT WHEN ITEMS BECOME AVAILABLE
        /*
        removeTemplateItem();

        // Remove items
        ItemIterator items = getAllItems();

        try
        {
        	while (items.hasNext())
        	{
        		Item item = items.next();
        		IndexBrowse ib = new IndexBrowse(ourContext);

        		if (item.isOwningCollection(this))
        		{
        			// the collection to be deleted is the owning collection, thus remove
        			// the item from all collections it belongs to
        			Collection[] collections = item.getCollections();
        			for (int i=0; i< collections.length; i++)
        			{
        				//notify Browse of removing item.
        				ib.itemRemoved(item);
        				// Browse.itemRemoved(ourContext, itemId);
        				collections[i].removeItem(item);
        			}

        		}
        		// the item was only mapped to this collection, so just remove it
        		else
        		{
        			//notify Browse of removing item mapping.
        			ib.indexItem(item);
        			// Browse.itemChanged(ourContext, item);
        			removeItem(item);
        		}
        	}
        }
        catch (BrowseException e)
        {
        	log.error("caught exception: ", e);
        	throw new IOException(e.getMessage(), e);
        }
        finally
        {
            if (items != null)
            {
                items.close();
            }
        }
        */
        // Delete bitstream logo
        //TODO: HIBERNATE, IMPLEMENT WHEN BITSTREAMS BECOME AVAILABLE
//        setLogo(null);

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
        HandleManager.unbindHandle(context, collection);

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
        GroupDAO groupDAO = new GroupDAO();
        Group g = null;

        g = collection.getWorkflowStep1();

        if (g != null)
        {
            groupDAO.delete(context, g);
        }

        g = collection.getWorkflowStep2();

        if (g != null)
        {
            groupDAO.delete(context, g);
        }

        g = collection.getWorkflowStep3();

        if (g != null)
        {
            groupDAO.delete(context, g);
        }

        // Remove default administrators group
        g = collection.getAdmins();

        if (g != null)
        {
            groupDAO.delete(context, g);
        }

        // Remove default submitters group
        g = collection.getSubmitters();

        if (g != null)
        {
            groupDAO.delete(context, g);
        }
    }

    /**
     * Get the communities this collection appears in
     *
     * @return array of <code>Community</code> objects
     * @throws SQLException
     */
    public Community[] getCommunities(Collection collection) throws SQLException
    {
        // Get the bundle table rows
        //At the moment only a single community is allowed to be parent of a collection
        return new Community[]{collection.getOwningCommunity()};
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
    public Collection[] findAuthorized(Context context, Community comm,
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
            myCollections = Arrays.asList(findAll(context));
        }

        // now build a list of collections you have authorization for
        for (Collection myCollection : myCollections) {
            if (AuthorizeManager.authorizeActionBoolean(context,
                    myCollection, actionID)) {
                myResults.add(myCollection);
            }
        }
        return myResults.toArray(new Collection[myResults.size()]);
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

    public DSpaceObject getAdminObject(Collection collection, int action) throws SQLException
    {
        DSpaceObject adminObject = null;
        Community community = null;
        Community[] communities = getCommunities();
        if (communities != null && communities.length > 0)
        {
            community = communities[0];
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
    public DSpaceObject getParentObject(Collection collection) throws SQLException
    {
        return collection.getOwningCommunity();
    }

    @Override
    public void updateLastModified()
    {
        //Also fire a modified event since the collection HAS been modified
        ourContext.addEvent(new Event(Event.MODIFY, Constants.COLLECTION, getID(), null));
    }
}