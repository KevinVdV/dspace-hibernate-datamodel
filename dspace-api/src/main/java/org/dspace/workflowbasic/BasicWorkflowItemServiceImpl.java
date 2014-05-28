package org.dspace.workflowbasic;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.eperson.EPerson;
import org.dspace.workflowbasic.dao.BasicWorkflowItemDAO;
import org.dspace.workflowbasic.service.BasicWorkflowItemService;
import org.dspace.workflowbasic.service.TaskListItemService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 7/04/14
 * Time: 15:52
 */
public class BasicWorkflowItemServiceImpl implements BasicWorkflowItemService
{
    /** log4j category */
    protected static Logger log = Logger.getLogger(BasicWorkflowItem.class);

    @Autowired(required = true)
    protected BasicWorkflowItemDAO workflowItemDAO;

    @Autowired(required = true)
    protected ItemService itemService;
    @Autowired(required = true)
    protected TaskListItemService taskListItemService;

    /** EPerson owning the current state */

    /**
     * Construct a workspace item corresponding to the given database row
     *
     */
    public BasicWorkflowItemServiceImpl()
    {
    }

    @Override
    public BasicWorkflowItem create(Context context, Item item, Collection collection) throws SQLException, AuthorizeException {
        BasicWorkflowItem workflowItem = workflowItemDAO.create(context, new BasicWorkflowItem());
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
    @Override
    public BasicWorkflowItem find(Context context, int id)
            throws SQLException
    {

        BasicWorkflowItem workflowItem = workflowItemDAO.findByID(context, BasicWorkflowItem.class, id);

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
    @Override
    public List<BasicWorkflowItem> findAll(Context c) throws SQLException
    {
        return workflowItemDAO.findAll(c, BasicWorkflowItem.class);
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
    @Override
    public List<BasicWorkflowItem> findBySubmitter(Context context, EPerson ep) throws SQLException
    {
        return workflowItemDAO.findBySubmitter(context, ep);
    }

    /**
     * getPooledTasks() returns a List of WorkflowItems an EPerson could claim
     * (as a reviewer, etc.) for display on a user's MyDSpace page.
     *
     * @param e
     *            The Eperson we want to fetch the pooled tasks for.
     */
    @Override
    public List<BasicWorkflowItem> findByPooledTasks(Context context, EPerson ePerson) throws SQLException
    {
        return workflowItemDAO.findByPooledTasks(context, ePerson);
    }

    @Override
    public List<BasicWorkflowItem> findByOwner(Context context, EPerson ePerson) throws SQLException {
        return workflowItemDAO.findByOwner(context, ePerson);
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
    @Override
    public List<BasicWorkflowItem> findByCollection(Context context, Collection c) throws SQLException
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
    @Override
    public BasicWorkflowItem findByItem(Context context, Item i) throws SQLException
    {
        return workflowItemDAO.findByItem(context, i);
    }

    @Override
    public void deleteByCollection(Context context, Collection collection) throws SQLException, IOException, AuthorizeException {
        List<BasicWorkflowItem> workflowItems = findByCollection(context, collection);
        Iterator<BasicWorkflowItem> iterator = workflowItems.iterator();
        while (iterator.hasNext()) {
            BasicWorkflowItem workflowItem = iterator.next();
            iterator.remove();
            delete(context, workflowItem);
        }
    }

    /**
     * Update the workflow item, including the unarchived item.
     */
    @Override
    public void update(Context context, BasicWorkflowItem workflowItem) throws SQLException, AuthorizeException
    {
        // FIXME check auth
        log.info(LogManager.getHeader(context, "update_workflow_item",
                "workflow_item_id=" + workflowItem.getID()));


        // Update the item
        itemService.update(context, workflowItem.getItem());

        // Update ourselves
        workflowItemDAO.save(context, workflowItem);
    }

    /**
     * delete the WorkflowItem, retaining the Item
     */
    @Override
    public void deleteWrapper(Context context, BasicWorkflowItem workflowItem) throws SQLException, AuthorizeException
    {
        // delete any pending tasks
        taskListItemService.deleteByWorkflowItem(context, workflowItem);

        // FIXME - auth?
        workflowItemDAO.delete(context, workflowItem);
    }

    @Override
    public void delete(Context context, BasicWorkflowItem workflowItem) throws SQLException, AuthorizeException, IOException {
        Item item = workflowItem.getItem();
        deleteWrapper(context, workflowItem);
        itemService.delete(context, item);
    }
}