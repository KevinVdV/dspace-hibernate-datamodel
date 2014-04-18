package org.dspace.workflow.dao;

import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;
import org.dspace.workflow.TaskListItem;
import org.dspace.workflow.WorkflowItem;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 8/04/14
 * Time: 09:10
 */
public interface TaskListItemDAO extends GenericDAO<TaskListItem> {

    public void deleteByWorkflowItem(Context context, WorkflowItem workflowItem) throws SQLException;
}
