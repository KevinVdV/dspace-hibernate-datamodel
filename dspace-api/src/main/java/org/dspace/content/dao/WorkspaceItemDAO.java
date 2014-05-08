package org.dspace.content.dao;

import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.WorkspaceItem;
import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;
import org.dspace.eperson.EPerson;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 31/03/14
 * Time: 10:47
 */
public interface WorkspaceItemDAO extends GenericDAO<WorkspaceItem> {

    public List<WorkspaceItem> findByEPerson(Context context, EPerson ep) throws SQLException;

    public List<WorkspaceItem> findByCollection(Context context, Collection c) throws SQLException;

    public WorkspaceItem findByItem(Context context, Item i) throws SQLException;

    public List<WorkspaceItem> findAll(Context context) throws SQLException;
}
