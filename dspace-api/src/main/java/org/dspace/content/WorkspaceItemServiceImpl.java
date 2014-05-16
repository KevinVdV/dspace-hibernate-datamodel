/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.content.dao.WorkspaceItemDAO;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.WorkspaceItemService;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class representing an item in the process of being submitted by a user
 *
 * @author Robert Tansley
 * @version $Revision$
 */
public class WorkspaceItemServiceImpl implements WorkspaceItemService
{
    /** log4j logger */
    protected static Logger log = Logger.getLogger(WorkspaceItem.class);

    @Autowired(required = true)
    protected WorkspaceItemDAO workspaceItemDAO;

    @Autowired(required = true)
    protected CollectionService collectionService;
    @Autowired(required = true)
    protected ItemService itemService;

    /**
     * Construct a workspace item corresponding to the given database row
     *
     */
    public WorkspaceItemServiceImpl()
    {
    }

    /**
     * Get a workspace item from the database. The item, collection and
     * submitter are loaded into memory.
     *
     * @param context
     *            DSpace context object
     * @param id
     *            ID of the workspace item
     *
     * @return the workspace item, or null if the ID is invalid.
     */
    @Override
    public WorkspaceItem find(Context context, int id) throws SQLException
    {
        // First check the cache
        WorkspaceItem workspaceItem = workspaceItemDAO.findByID(context, WorkspaceItem.class, id);

        if (workspaceItem == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug(LogManager.getHeader(context, "find_workspace_item",
                        "not_found,workspace_item_id=" + id));
            }
        }
        else
        {
            if (log.isDebugEnabled())
            {
                log.debug(LogManager.getHeader(context, "find_workspace_item",
                        "workspace_item_id=" + id));
            }
        }
        return workspaceItem;
    }

    /**
     * Create a new workspace item, with a new ID. An Item is also created. The
     * submitter is the current user in the context.
     *
     * @param c
     *            DSpace context object
     * @param coll
     *            Collection being submitted to
     * @param template
     *            if <code>true</code>, the workspace item starts as a copy
     *            of the collection's template item
     *
     * @return the newly created workspace item
     */
    @Override
    public WorkspaceItem create(Context c, Collection coll, boolean template) throws AuthorizeException, SQLException,
            IOException {
        // Check the user has permission to ADD to the collection
        AuthorizeManager.authorizeAction(c, coll, Constants.ADD);

        WorkspaceItem workspaceItem = workspaceItemDAO.create(c, new WorkspaceItem());

        // Create an item
        Item i = itemService.create(c, workspaceItem);
        i.setSubmitter(c.getCurrentUser());

        // Now create the policies for the submitter and workflow
        // users to modify item and contents
        // contents = bitstreams, bundles
        // FIXME: icky hardcoded workflow steps
        Group step1group = collectionService.getWorkflowGroup(coll, 1);
        Group step2group = collectionService.getWorkflowGroup(coll, 2);
        Group step3group = collectionService.getWorkflowGroup(coll, 3);

        EPerson e = c.getCurrentUser();

        // read permission
        AuthorizeManager.addPolicy(c, i, Constants.READ, e, ResourcePolicy.TYPE_SUBMISSION);


        if (ConfigurationManager.getProperty("workflow", "workflow.framework").equals("originalworkflow")) {
            if (step1group != null)
            {
                AuthorizeManager.addPolicy(c, i, Constants.READ, step1group, ResourcePolicy.TYPE_WORKFLOW);
            }

            if (step2group != null)
            {
                AuthorizeManager.addPolicy(c, i, Constants.READ, step2group, ResourcePolicy.TYPE_WORKFLOW);
            }

            if (step3group != null)
            {
                AuthorizeManager.addPolicy(c, i, Constants.READ, step3group, ResourcePolicy.TYPE_WORKFLOW);
            }
        }


        // write permission
        AuthorizeManager.addPolicy(c, i, Constants.WRITE, e, ResourcePolicy.TYPE_SUBMISSION);

        if (ConfigurationManager.getProperty("workflow", "workflow.framework").equals("originalworkflow")) {
            if (step1group != null)
            {
                AuthorizeManager.addPolicy(c, i, Constants.WRITE, step1group, ResourcePolicy.TYPE_WORKFLOW);
            }

            if (step2group != null)
            {
                AuthorizeManager.addPolicy(c, i, Constants.WRITE, step2group, ResourcePolicy.TYPE_WORKFLOW);
            }

            if (step3group != null)
            {
                AuthorizeManager.addPolicy(c, i, Constants.WRITE, step3group, ResourcePolicy.TYPE_WORKFLOW);
            }
        }

        // add permission
        AuthorizeManager.addPolicy(c, i, Constants.ADD, e, ResourcePolicy.TYPE_SUBMISSION);

        if (ConfigurationManager.getProperty("workflow", "workflow.framework").equals("originalworkflow")) {
            if (step1group != null)
            {
                AuthorizeManager.addPolicy(c, i, Constants.ADD, step1group, ResourcePolicy.TYPE_WORKFLOW);
            }

            if (step2group != null)
            {
                AuthorizeManager.addPolicy(c, i, Constants.ADD, step2group, ResourcePolicy.TYPE_WORKFLOW);
            }

            if (step3group != null)
            {
                AuthorizeManager.addPolicy(c, i, Constants.ADD, step3group, ResourcePolicy.TYPE_WORKFLOW);
            }
        }

        // remove contents permission
        AuthorizeManager.addPolicy(c, i, Constants.REMOVE, e, ResourcePolicy.TYPE_SUBMISSION);

        if (ConfigurationManager.getProperty("workflow", "workflow.framework").equals("originalworkflow")) {
            if (step1group != null)
            {
                AuthorizeManager.addPolicy(c, i, Constants.REMOVE, step1group, ResourcePolicy.TYPE_WORKFLOW);
            }

            if (step2group != null)
            {
                AuthorizeManager.addPolicy(c, i, Constants.REMOVE, step2group, ResourcePolicy.TYPE_WORKFLOW);
            }

            if (step3group != null)
            {
                AuthorizeManager.addPolicy(c, i, Constants.REMOVE, step3group, ResourcePolicy.TYPE_WORKFLOW);
            }
        }

        // Copy template if appropriate
        Item templateItem = coll.getTemplateItem();

        if (template && (templateItem != null))
        {
            List<MetadataValue> md = itemService.getMetadata(templateItem, Item.ANY, Item.ANY, Item.ANY, Item.ANY);

            for (MetadataValue aMd : md) {
                MetadataField metadataField = aMd.getMetadataField();
                MetadataSchema metadataSchema = metadataField.getMetadataSchema();
                itemService.addMetadata(c, i, metadataSchema.getName(), metadataField.getElement(), metadataField.getQualifier(), aMd.getLanguage(),
                        aMd.getValue());
            }
        }

        itemService.update(c, i);
        workspaceItem.setItem(i);
        workspaceItem.setCollection(coll);

        log.info(LogManager.getHeader(c, "create_workspace_item",
                "workspace_item_id=" + workspaceItem.getID()
                        + "item_id=" + i.getID() + "collection_id="
                        + coll.getID()));

        update(c, workspaceItem);
        return workspaceItem;
    }

    @Override
    public WorkspaceItem create(Context c, Collection coll, InProgressSubmission workflowItem) throws AuthorizeException, SQLException, IOException {
        if(workflowItem instanceof WorkspaceItem)
        {
            throw new IllegalStateException("Cannot create workspace item for a workspace item !");
        }
        WorkspaceItem workspaceItem = workspaceItemDAO.create(c, new WorkspaceItem());
        workspaceItem.setItem(workflowItem.getItem());
        workspaceItem.setCollection(coll);
        update(c, workspaceItem);
        return workspaceItem;
    }

    /**
     * Get all workspace items for a particular e-person. These are ordered by
     * workspace item ID, since this should likely keep them in the order in
     * which they were created.
     *
     * @param context
     *            the context object
     * @param ep
     *            the eperson
     *
     * @return the corresponding workspace items
     */
    @Override
    public List<WorkspaceItem> findByEPerson(Context context, EPerson ep) throws SQLException
    {
        return workspaceItemDAO.findByEPerson(context, ep);

    }

    /**
     * Get all workspace items for a particular collection.
     *
     * @param context
     *            the context object
     * @param c
     *            the collection
     *
     * @return the corresponding workspace items
     */
    @Override
    public List<WorkspaceItem> findByCollection(Context context, Collection c) throws SQLException
    {
        return workspaceItemDAO.findByCollection(context, c);
    }

    /**
     * Check to see if a particular item is currently still in a user's Workspace.
     * If so, its WorkspaceItem is returned.  If not, null is returned
     *
     * @param context
     *            the context object
     * @param i
     *            the item
     *
     * @return workflow item corresponding to the item, or null
     */
    @Override
    public WorkspaceItem findByItem(Context context, Item i)
            throws SQLException
    {
        return workspaceItemDAO.findByItem(context, i);
    }


    /**
     * Get all workspace items in the whole system
     *
     * @param   context     the context object
     *
     * @return      all workspace items
     */
    @Override
    public List<WorkspaceItem> findAll(Context context) throws SQLException
    {
        return workspaceItemDAO.findAll(context);
    }

    /**
     * Update the workspace item, including the unarchived item.
     */
    @Override
    public void update(Context context, WorkspaceItem workspaceItem) throws SQLException, AuthorizeException
    {
        // Authorisation is checked by the item.update() method below

        log.info(LogManager.getHeader(context, "update_workspace_item",
                "workspace_item_id=" + workspaceItem.getID()));

        // Update the item
        itemService.update(context, workspaceItem.getItem());

        // Update ourselves
        workspaceItemDAO.save(context, workspaceItem);
    }

    public EPerson getSubmitter(WorkspaceItem workspaceItem) throws SQLException {
        return workspaceItem.getItem().getSubmitter();
    }


    /**
     * Delete the workspace item. The entry in workspaceitem, the unarchived
     * item and its contents are all removed (multiple inclusion
     * notwithstanding.)
     */
    @Override
    public void delete(Context context, WorkspaceItem workspaceItem) throws SQLException, AuthorizeException,
            IOException
    {
        /*
         * Authorisation is a special case. The submitter won't have REMOVE
         * permission on the collection, so our policy is this: Only the
         * original submitter or an administrator can delete a workspace item.

         */
        if (!AuthorizeManager.isAdmin(context)
                && ((context.getCurrentUser() == null) || (context
                .getCurrentUser().getID() != workspaceItem.getItem().getSubmitter()
                .getID())))
        {
            // Not an admit, not the submitter
            throw new AuthorizeException("Must be an administrator or the "
                    + "original submitter to delete a workspace item");
        }

        log.info(LogManager.getHeader(context, "delete_workspace_item",
                "workspace_item_id=" + workspaceItem.getID() + "item_id=" + workspaceItem.getItem().getID()
                        + "collection_id=" + workspaceItem.getCollection().getID()));

       // Need to delete the epersongroup2workspaceitem row first since it refers
        // to workspaceitem ID
//        TODO: HIBERNATE: Implement when supervisor comes into play
//        deleteEpersonGroup2WorkspaceItem();

        Item item = workspaceItem.getItem();
        // Need to delete the workspaceitem row first since it refers
        // to item ID
        workspaceItemDAO.delete(context, workspaceItem);

        // Delete item
        itemService.delete(context, item);
    }

    @Override
    public void deleteByCollection(Context context, Collection collection) throws SQLException, IOException, AuthorizeException {
        List<WorkspaceItem> workspaceItems = findByCollection(context, collection);
        Iterator<WorkspaceItem> iterator = workspaceItems.iterator();
        while (iterator.hasNext()) {
            WorkspaceItem workspaceItem = iterator.next();
            //Remove it before deleting our object to avoid exceptions
            iterator.remove();
            delete(context, workspaceItem);
        }
    }

    /*
    TODO: HIBERNATE: Implement when supervisor comes into play
    private void deleteEpersonGroup2WorkspaceItem() throws SQLException
    {

        String removeSQL="DELETE FROM epersongroup2workspaceitem WHERE workspace_item_id = ?";
        DatabaseManager.updateQuery(ourContext, removeSQL,getID());

    }*/

    @Override
    public void deleteWrapper(Context context, WorkspaceItem workspaceItem) throws SQLException, AuthorizeException
    {
        // Check authorisation. We check permissions on the enclosed item.
        AuthorizeManager.authorizeAction(context, workspaceItem.getItem(), Constants.WRITE);

        log.info(LogManager.getHeader(context, "delete_workspace_item",
                "workspace_item_id=" + workspaceItem.getID() + "item_id=" + workspaceItem.getItem().getID()
                        + "collection_id=" + workspaceItem.getCollection().getID()));

        //        deleteSubmitPermissions();

        // Need to delete the workspaceitem row first since it refers
        // to item ID
        workspaceItemDAO.delete(context, workspaceItem);
    }
}