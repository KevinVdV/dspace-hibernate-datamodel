package org.dspace.workflow;

import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 8/04/14
 * Time: 09:13
 */
public interface TaskListItemManager {

    public TaskListItem create(Context context, WorkflowItem workflowItem, EPerson ePerson) throws SQLException;

    public void deleteByWorkflowItem(Context context, WorkflowItem workflowItem) throws SQLException;

    public void update(Context context, TaskListItem taskListItem) throws SQLException;
}
