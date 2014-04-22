package org.dspace.xmlworkflow.storedcomponents;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.ItemService;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.eperson.EPerson;
import org.dspace.xmlworkflow.storedcomponents.dao.XmlWorkflowItemDAO;
import org.dspace.xmlworkflow.storedcomponents.service.ClaimedTaskService;
import org.dspace.xmlworkflow.storedcomponents.service.WorkflowItemRoleService;
import org.dspace.xmlworkflow.storedcomponents.service.XmlWorkflowItemService;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 21/04/14
 * Time: 09:49
 * To change this template use File | Settings | File Templates.
 */
public class XmlWorkflowItemServiceImpl implements XmlWorkflowItemService {

    @Autowired(required = true)
    protected WorkflowItemRoleService workflowItemRoleService;

    @Autowired(required = true)
    protected XmlWorkflowItemDAO xmlWorkflowItemDAO;

    @Autowired(required = true)
    protected ItemService itemService;
    @Autowired(required = true)
    protected PoolTaskService poolTaskService;
    @Autowired(required = true)
    protected ClaimedTaskService claimedTaskService;

    /*
     * The current step in the workflow system in which this workflow item is present
     */
    private static Logger log = Logger.getLogger(XmlWorkflowItem.class);

    public XmlWorkflowItemServiceImpl() {
    }

    public XmlWorkflowItem create(Context context) throws SQLException {
        return xmlWorkflowItemDAO.create(context, new XmlWorkflowItem());
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
    public XmlWorkflowItem find(Context context, int id) throws SQLException {
        XmlWorkflowItem workflowItem = xmlWorkflowItemDAO.findByID(context, XmlWorkflowItem.class, id);


        if (workflowItem == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug(LogManager.getHeader(context, "find_workflow_item",
                        "not_found,workflowitem_id=" + id));
            }
        }
        else
        {
            if (log.isDebugEnabled())
            {
                log.debug(LogManager.getHeader(context, "find_workflow_item",
                        "workflowitem_id=" + id));
            }
        }
        return workflowItem;
    }

    /**
     * return all workflowitems
     *
     * @param context  active context
     * @return WorkflowItem list of all workflow items in the system
     */
    public List<XmlWorkflowItem> findAll(Context context) throws SQLException
    {
        return xmlWorkflowItemDAO.findAll(context, XmlWorkflowItem.class);
    }

    /**
     * return all workflowitems for a certain page
     *
     * @param context  active context
     * @return WorkflowItem [] of all workflows in system
     */
    public List<XmlWorkflowItem> findAll(Context context, int page, int pagesize) throws SQLException {
        return findAllInCollection(context, page, pagesize, null);
    }

    /**
     * return all workflowitems for a certain page with a certain collection
     *
     * @param context  active context
     * @return WorkflowItem [] of all workflows in system
     */
    public List<XmlWorkflowItem> findAllInCollection(Context context, int page, int pagesize, Collection collection) throws SQLException {
        return xmlWorkflowItemDAO.findAllInCollection(context, page, pagesize, collection);
    }


    /**
     * return all workflowitems
     *
     * @param context  active context
     * @return WorkflowItem [] of all workflows in system
     */
    public int countAll(Context context) throws SQLException {
        return xmlWorkflowItemDAO.countAll(context);
    }

    /**
     * return all workflowitems
     *
     * @param context  active context
     * @return WorkflowItem [] of all workflows in system
     */
    public int countAllInCollection(Context context, Collection collection) throws SQLException
    {
        return xmlWorkflowItemDAO.countAllInCollection(context, collection);
    }


    /*
     * Returns all workflow items submitted by an eperson
     */
    public List<XmlWorkflowItem> findBySubmitter(Context context, EPerson ep) throws SQLException {
        return xmlWorkflowItemDAO.findBySubmitter(context, ep);
    }

    /**
     * Get all workflow items for a particular collection.
     *
     * @param context
     *            the context object
     * @param collection
     *            the collection
     *
     * @return array of the corresponding workflow items
     */
    public List<XmlWorkflowItem> findByCollection(Context context, Collection collection) throws SQLException
    {
        return xmlWorkflowItemDAO.findByCollection(context, collection);
    }

    /**
     * Check to see if a particular item is currently under Workflow.
     * If so, its WorkflowItem is returned.  If not, null is returned
     *
     * @param context
     *            the context object
     * @param item
     *            the item
     *
     * @return workflow item corresponding to the item, or null
     */
    public XmlWorkflowItem findByItem(Context context, Item item) throws SQLException{
        return xmlWorkflowItemDAO.findByItem(context, item);
    }

    /**
     * Update the workflow item, including the unarchived item.
     */
    public void update(Context context, XmlWorkflowItem workflowItem) throws SQLException, AuthorizeException {
        // FIXME check auth
        log.info(LogManager.getHeader(context, "update_workflow_item",
                "workflowitem_id=" + workflowItem.getID()));

        // Update the item
        itemService.update(context, workflowItem.getItem());

        xmlWorkflowItemDAO.save(context, workflowItem);
    }

    @Override
    public EPerson getSubmitter(XmlWorkflowItem inProgressSubmission) throws SQLException {
        return inProgressSubmission.getItem().getSubmitter();
    }

    /**
     * delete the WorkflowItem, retaining the Item
     */
    public void deleteWrapper(Context context, XmlWorkflowItem workflowItem) throws SQLException
    {
        List<WorkflowItemRole> roles = workflowItemRoleService.findByWorkflowItem(context, workflowItem);
        Iterator<WorkflowItemRole> workflowItemRoleIterator = roles.iterator();
        while (workflowItemRoleIterator.hasNext())
        {
            WorkflowItemRole workflowItemRole = workflowItemRoleIterator.next();
            workflowItemRoleService.delete(context, workflowItemRole);
        }

        XmlWorkflowManager.deleteAllTasks(context, this);

        // FIXME - auth?
        xmlWorkflowItemDAO.delete(context, workflowItem);
    }
}
