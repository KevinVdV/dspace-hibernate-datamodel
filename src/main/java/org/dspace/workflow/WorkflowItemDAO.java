package org.dspace.workflow;

import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;
import org.dspace.eperson.EPerson;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 7/04/14
 * Time: 16:05
 */
public interface WorkflowItemDAO extends GenericDAO<WorkflowItem> {

    public WorkflowItem findByItem(Context context, Item i) throws SQLException;

    public List<WorkflowItem> findByEPerson(Context context, EPerson ep) throws SQLException;

    public List<WorkflowItem> findByCollection(Context context, Collection c) throws SQLException;

    public List<WorkflowItem> findByPooledTasks(Context context, EPerson ePerson) throws SQLException;
}
