package org.dspace.workflowbasic.dao;

import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;
import org.dspace.eperson.EPerson;
import org.dspace.workflowbasic.BasicWorkflowItem;
import org.dspace.workflowbasic.TaskListItem;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 8/04/14
 * Time: 09:10
 */
public interface TaskListItemDAO extends GenericDAO<TaskListItem> {

    public void deleteByWorkflowItem(Context context, BasicWorkflowItem workflowItem) throws SQLException;

    public List<TaskListItem> findByEPerson(Context context, EPerson ePerson) throws SQLException;
}
