package org.dspace.content;

import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.authorize.service.ResourcePolicyService;
import org.dspace.content.dao.SupervisedItemDAO;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.SupervisedItemService;
import org.dspace.content.service.WorkspaceItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.eperson.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 29/04/14
 * Time: 15:24
 */
public class SupervisedItemServiceImpl implements SupervisedItemService {

    @Autowired(required = true)
    protected SupervisedItemDAO supervisedItemDAO;

    @Autowired(required = true)
    protected ItemService itemService;

    @Autowired(required = true)
    protected GroupService groupService;

    @Autowired(required = true)
    protected ResourcePolicyService resourcePolicyService;

    @Autowired(required = true)
    protected WorkspaceItemService workspaceItemService;

    /**
     * Get all workspace items which are being supervised
     *
     * @param context the context this object exists in
     *
     * @return array of SupervisedItems
     */
    @Override
    public List<WorkspaceItem> getAll(Context context) throws SQLException
    {
        return supervisedItemDAO.findAll(context, WorkspaceItem.class);
    }


    /**
     * Gets all the groups that are supervising a particular workspace item
     *
     * @param c the context this object exists in
     * @param workspaceItem the workspace item
     *
     * @return the supervising groups in an array
     */
    @Override
    public List<Group> getSupervisorGroups(Context context, WorkspaceItem workspaceItem) throws SQLException
    {
//        return supervisedItemDAO.findByWorkspaceItem(context, WorkspaceItem.class);
                     //TODO: IMPLEMENT
        return null;

    }

    /**
     * Gets all the groups that are supervising a workspace item
     *
     *
     * @return the supervising groups in an array
     */
    // FIXME: We should arrange this code to use the above getSupervisorGroups
    // method by building the relevant info before passing the request.
    @Override
    public List<Group> getSupervisorGroups() throws SQLException
    {
        //TODO: IMPLEMENT
        return null;

    }

    /**
     * Get items being supervised by given EPerson
     *
     * @param   ep          the eperson who's items to supervise we want
     * @param   context     the dspace context
     *
     * @return the items eperson is supervising in an array
     */
    @Override
    public List<WorkspaceItem> findByEPerson(Context context, EPerson ep) throws SQLException
    {
        return supervisedItemDAO.findByEPerson(context, ep);

    }


    /**
     * finds out if there is a supervision order that matches this set
     * of values
     *
     * @param context   the context this object exists in
     * @param workspaceItem  the workspace item to be supervised
     * @param group   the group to be doing the supervising
     *
     * @return boolean  true if there is an order that matches, false if not
     */
    @Override
    public boolean isOrder(Context context, WorkspaceItem workspaceItem, Group group) throws SQLException
    {
        return supervisedItemDAO.findByWorkspaceItemAndGroup(context, workspaceItem, group) != null;
    }

    /**
     * removes the requested group from the requested workspace item in terms
     * of supervision.  This also removes all the policies that group has
     * associated with the item
     *
     * @param context   the context this object exists in
     * @param workspaceItem  the workspace item
     * @param group  the group to be removed from the item
     */
    @Override
    public void remove(Context context, WorkspaceItem workspaceItem, Group group) throws SQLException, AuthorizeException
    {
        // get the workspace item and the group from the request values
        workspaceItem.removeSupervisorGroup(group);
        update(context, workspaceItem);

        // get the item and have it remove the policies for the group
        Item item = workspaceItem.getItem();
        itemService.removeGroupPolicies(context, item, group);
    }

    public void update(Context context, WorkspaceItem supervisedItem) throws SQLException {
        supervisedItemDAO.save(context, supervisedItem);
    }

    /**
     * removes redundant entries in the supervision orders database
     *
     * @param context   the context this object exists in
     */
    //TODO: CREATE THIS "horrible" query
    /*
    public static void removeRedundant(Context context)
        throws SQLException
    {
        // this horrid looking query tests to see if there are any groups or
        // workspace items which match up to the ones in the linking database
        // table.  If there aren't, we know that the link is out of date, and
        // it can be deleted.
        String query = "DELETE FROM epersongroup2workspaceitem " +
                       "WHERE NOT EXISTS ( " +
                       "SELECT 1 FROM workspaceitem WHERE workspace_item_id " +
                       "= epersongroup2workspaceitem.workspace_item_id " +
                       ") OR NOT EXISTS ( " +
                       "SELECT 1 FROM epersongroup WHERE eperson_group_id " +
                       "= epersongroup2workspaceitem.eperson_group_id " +
                       ")";

        DatabaseManager.updateQuery(context, query);
    }
    */

    /**
     * adds a supervision order to the database
     *
     * @param context   the context this object exists in
     * @param group   the group which will supervise
     * @param workspaceItem  the workspace item to be supervised
     * @param policy    String containing the policy type to be used
     */
    @Override
    public void add(Context context, Group group, WorkspaceItem workspaceItem, int policy)
        throws SQLException, AuthorizeException
    {
        workspaceItem.addSupervisorGroup(group);

        // If a default policy type has been requested, apply the policies using
        // the DSpace API for doing so
        if (policy != POLICY_NONE)
        {
            Item item = workspaceItem.getItem();
            // "Editor" implies READ, WRITE, ADD permissions
            // "Observer" implies READ permissions
            if (policy == POLICY_EDITOR)
            {
                ResourcePolicy r = resourcePolicyService.create(context);
                r.setdSpaceObject(item);
                r.setGroup(group);
                r.setAction(Constants.READ);
                resourcePolicyService.update(context, r);

                r = resourcePolicyService.create(context);
                r.setdSpaceObject(item);
                r.setGroup(group);
                r.setAction(Constants.WRITE);
                resourcePolicyService.update(context, r);

                r = resourcePolicyService.create(context);
                r.setdSpaceObject(item);
                r.setGroup(group);
                r.setAction(Constants.ADD);
                resourcePolicyService.update(context, r);

            }
            else if (policy == POLICY_OBSERVER)
            {
                ResourcePolicy r = resourcePolicyService.create(context);
                r.setdSpaceObject(item);
                r.setGroup(group);
                r.setAction(Constants.READ);
                resourcePolicyService.update(context, r);
            }
        }
    }

    @Override
    public void removeSupervisedGroup(Context context, Group group) throws SQLException, AuthorizeException {
        List<WorkspaceItem> supervisedItems = supervisedItemDAO.findByGroup(context, group);
        for (WorkspaceItem supervisedItem : supervisedItems) {
            supervisedItem.removeSupervisorGroup(group);
            workspaceItemService.update(context, supervisedItem);
        }
    }

}
