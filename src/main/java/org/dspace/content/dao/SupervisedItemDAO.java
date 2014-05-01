package org.dspace.content.dao;

import org.dspace.content.WorkspaceItem;
import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 29/04/14
 * Time: 15:30
 */
public interface SupervisedItemDAO extends GenericDAO<WorkspaceItem> {

    public List<WorkspaceItem> findByEPerson(Context context, EPerson ePerson) throws SQLException;

    public WorkspaceItem findByWorkspaceItemAndGroup(Context context, WorkspaceItem workspaceItem, Group group) throws SQLException;
}
