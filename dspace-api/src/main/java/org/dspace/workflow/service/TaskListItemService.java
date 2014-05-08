package org.dspace.workflow.service;

import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.workflow.TaskListItem;
import org.dspace.workflow.WorkflowItem;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 8/04/14
 * Time: 09:13
 */
public interface TaskListItemService {

    public TaskListItem create(Context context, WorkflowItem workflowItem, EPerson ePerson) throws SQLException;

    public void deleteByWorkflowItem(Context context, WorkflowItem workflowItem) throws SQLException;

    public void update(Context context, TaskListItem taskListItem) throws SQLException;
}
