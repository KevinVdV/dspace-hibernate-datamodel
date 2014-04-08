package org.dspace.workflow;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.ItemManager;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.eperson.EPerson;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 7/04/14
 * Time: 15:52
 */
public class WorkflowItemManagerImpl implements WorkflowItemManager
{
    /** log4j category */
    protected static Logger log = Logger.getLogger(WorkflowItem.class);

    @Autowired(required = true)
    protected WorkflowItemDAO workflowItemDAO;

    @Autowired(required = true)
    protected ItemManager itemManager;
    @Autowired(required = true)
    protected TaskListItemManager taskListItemManager;

    /** EPerson owning the current state */

    /**
     * Construct a workspace item corresponding to the given database row
     *
     */
    public WorkflowItemManagerImpl()
    {
    }

    public WorkflowItem create(Context context, Item item, Collection collection) throws SQLException, AuthorizeException {
        WorkflowItem workflowItem = workflowItemDAO.create(context, new WorkflowItem());
        workflowItem.setItem(item);
        workflowItem.setCollection(collection);
        update(context, workflowItem);
        return workflowItem;
    }

    /**
     * Get a workflow item from the database. The item, collection and submitter
     * are loaded into memory.
     *
     * @param context
     *            DSpace context object
     * @param id
     *            ID of the workspace item
     *
     * @return the workflow item, or null if the ID is invalid.
     */
    public WorkflowItem find(Context context, int id)
            throws SQLException
    {

        WorkflowItem workflowItem = workflowItemDAO.findByID(context, WorkflowItem.class, id);

        if (workflowItem == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug(LogManager.getHeader(context, "find_workflow_item",
                        "not_found,workflow_id=" + id));
            }
        }
        else
        {
            if (log.isDebugEnabled())
            {
                log.debug(LogManager.getHeader(context, "find_workflow_item",
                        "workflow_id=" + id));
            }
        }
        return workflowItem;
    }

    /**
     * return all workflowitems
     *
     * @param c  active context
     * @return WorkflowItem [] of all workflows in system
     */
    public List<WorkflowItem> findAll(Context c) throws SQLException
    {
        return workflowItemDAO.findAll(c, WorkflowItem.class);
    }

    /**
     * Get all workflow items that were original submissions by a particular
     * e-person. These are ordered by workflow ID, since this should likely keep
     * them in the order in which they were created.
     *
     * @param context
     *            the context object
     * @param ep
     *            the eperson
     *
     * @return the corresponding workflow items
     */
    public List<WorkflowItem> findByEPerson(Context context, EPerson ep) throws SQLException
    {
        return workflowItemDAO.findByEPerson(context, ep);
    }

    /**
     * getPooledTasks() returns a List of WorkflowItems an EPerson could claim
     * (as a reviewer, etc.) for display on a user's MyDSpace page.
     *
     * @param e
     *            The Eperson we want to fetch the pooled tasks for.
     */
    public List<WorkflowItem> findByPooledTasks(Context context, EPerson ePerson) throws SQLException
    {
        return workflowItemDAO.findByPooledTasks(context, ePerson);
    }

    /**
     * Get all workflow items for a particular collection.
     *
     * @param context
     *            the context object
     * @param c
     *            the collection
     *
     * @return array of the corresponding workflow items
     */
    public List<WorkflowItem> findByCollection(Context context, Collection c) throws SQLException
    {
        return workflowItemDAO.findByCollection(context, c);

    }


    /**
     * Check to see if a particular item is currently under Workflow.
     * If so, its WorkflowItem is returned.  If not, null is returned
     *
     * @param context
     *            the context object
     * @param i
     *            the item
     *
     * @return workflow item corresponding to the item, or null
     */
    public WorkflowItem findByItem(Context context, Item i) throws SQLException
    {
        return workflowItemDAO.findByItem(context, i);
    }

    /**
     * Update the workflow item, including the unarchived item.
     */
    public void update(Context context, WorkflowItem workflowItem) throws SQLException, AuthorizeException
    {
        // FIXME check auth
        log.info(LogManager.getHeader(context, "update_workflow_item",
                "workflow_item_id=" + workflowItem.getID()));


        // Update the item
        itemManager.update(context, workflowItem.getItem());

        // Update ourselves
        workflowItemDAO.save(context, workflowItem);
    }

    /**
     * delete the WorkflowItem, retaining the Item
     */
    public void deleteWrapper(Context context, WorkflowItem workflowItem) throws SQLException, AuthorizeException
    {
        // delete any pending tasks
        taskListItemManager.deleteByWorkflowItem(context, workflowItem);

        // FIXME - auth?
        workflowItemDAO.delete(context, workflowItem);
    }

    public EPerson getSubmitter(WorkflowItem workflowItem) throws SQLException
    {
        return workflowItem.getItem().getSubmitter();
    }
}