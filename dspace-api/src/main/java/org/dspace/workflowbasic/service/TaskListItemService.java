package org.dspace.workflowbasic.service;

import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.workflowbasic.BasicWorkflowItem;
import org.dspace.workflowbasic.TaskListItem;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 8/04/14
 * Time: 09:13
 */
public interface TaskListItemService {

    public TaskListItem create(Context context, BasicWorkflowItem workflowItem, EPerson ePerson) throws SQLException;

    public void deleteByWorkflowItem(Context context, BasicWorkflowItem workflowItem) throws SQLException;

    public void update(Context context, TaskListItem taskListItem) throws SQLException;

    public List<TaskListItem> findByEPerson(Context context, EPerson ePerson) throws SQLException;
}
