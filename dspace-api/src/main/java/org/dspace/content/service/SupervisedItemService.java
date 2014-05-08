package org.dspace.content.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.WorkspaceItem;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 29/04/14
 * Time: 15:55
 */
public interface SupervisedItemService {

    /** value to use for no policy set */
    public static final int POLICY_NONE = 0;

    /** value to use for editor policies */
    public static final int POLICY_EDITOR = 1;

    /** value to use for observer policies */
    public static final int POLICY_OBSERVER = 2;

    public List<WorkspaceItem> getAll(Context context) throws SQLException;

    public Group[] getSupervisorGroups(Context c, int wi) throws SQLException;

    public Group[] getSupervisorGroups() throws SQLException;

    public List<WorkspaceItem> findByEPerson(Context context, EPerson ep) throws SQLException;

    public boolean isOrder(Context context, WorkspaceItem workspaceItem, Group group) throws SQLException;

    public void remove(Context context, WorkspaceItem supervisedItem, Group group) throws SQLException, AuthorizeException;

    public void add(Context context, Group group, WorkspaceItem workspaceItem, int policy) throws SQLException, AuthorizeException;
}