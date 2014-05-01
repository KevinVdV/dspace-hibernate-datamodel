package org.dspace.content;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.dao.SupervisedItemDAO;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.SupervisedItemService;
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

    /**
     * Get all workspace items which are being supervised
     *
     * @param context the context this object exists in
     *
     * @return array of SupervisedItems
     */
    public List<SupervisedItem> getAll(Context context) throws SQLException
    {
        return supervisedItemDAO.findAll(context, SupervisedItem.class);
    }


    /**
     * Gets all the groups that are supervising a particular workspace item
     *
     * @param c the context this object exists in
     * @param wi the ID of the workspace item
     *
     * @return the supervising groups in an array
     */
    public Group[] getSupervisorGroups(Context c, int wi) throws SQLException
    {
        //TODO: IMPLEMENT
        return null;

    }

    /**
     * Gets all the groups that are supervising a this workspace item
     *
     *
     * @return the supervising groups in an array
     */
    // FIXME: We should arrange this code to use the above getSupervisorGroups
    // method by building the relevant info before passing the request.
    public Group[] getSupervisorGroups() throws SQLException
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
    public List<SupervisedItem> findByEPerson(Context context, EPerson ep) throws SQLException
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
    public void remove(Context context, SupervisedItem supervisedItem, Group group) throws SQLException, AuthorizeException
    {
        // get the workspace item and the group from the request values
        supervisedItem.removeGroup(group);
        update(context, supervisedItem);

        // get the item and have it remove the policies for the group
        Item item = supervisedItem.getItem();
        itemService.removeGroupPolicies(context, item, group);
    }

    public void update(Context context, SupervisedItem supervisedItem) throws SQLException {
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
     * @param group   the ID of the group which will supervise
     * @param wsItemID  the ID of the workspace item to be supervised
     * @param policy    String containing the policy type to be used
     */
    public void add(Context context, Group group, WorkspaceItem workspaceItem, int policy)
        throws SQLException, AuthorizeException
    {
        // make a table row in the database table, and update with the relevant
        // details
        supervisedItemDAO.findByID(context, SupervisedItem.class ,workspaceItem.getID());
        /*
        TableRow row = DatabaseManager.row("epersongroup2workspaceitem");
        row.setColumn("workspace_item_id", wsItemID);
        row.setColumn("eperson_group_id", groupID);
        DatabaseManager.insert(context,row);

        // If a default policy type has been requested, apply the policies using
        // the DSpace API for doing so
        if (policy != POLICY_NONE)
        {
            Item item = wsItem.getItem();
            Group group = Group.find(context, groupID);

            // "Editor" implies READ, WRITE, ADD permissions
            // "Observer" implies READ permissions
            if (policy == POLICY_EDITOR)
            {
                ResourcePolicy r = ResourcePolicy.create(context);
                r.setResource(item);
                r.setGroup(group);
                r.setAction(Constants.READ);
                r.update();

                r = ResourcePolicy.create(context);
                r.setResource(item);
                r.setGroup(group);
                r.setAction(Constants.WRITE);
                r.update();

                r = ResourcePolicy.create(context);
                r.setResource(item);
                r.setGroup(group);
                r.setAction(Constants.ADD);
                r.update();

            }
            else if (policy == POLICY_OBSERVER)
            {
                ResourcePolicy r = ResourcePolicy.create(context);
                r.setResource(item);
                r.setGroup(group);
                r.setAction(Constants.READ);
                r.update();
            }
        }
        */
    }

}
