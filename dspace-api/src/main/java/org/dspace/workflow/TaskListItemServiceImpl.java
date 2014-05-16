package org.dspace.workflow;

import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.workflow.dao.TaskListItemDAO;
import org.dspace.workflow.service.TaskListItemService;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 8/04/14
 * Time: 09:13
 */
public class TaskListItemServiceImpl implements TaskListItemService {

    @Autowired(required = true)
    protected TaskListItemDAO taskListItemDAO;

    @Override
    public TaskListItem create(Context context, WorkflowItem workflowItem, EPerson ePerson) throws SQLException {
        TaskListItem taskListItem = taskListItemDAO.create(context, new TaskListItem());
        taskListItem.setWorkflowItem(workflowItem);
        taskListItem.setEPerson(ePerson);
        update(context, taskListItem);
        return taskListItem;
    }

    @Override
    public void deleteByWorkflowItem(Context context, WorkflowItem workflowItem) throws SQLException {
        taskListItemDAO.deleteByWorkflowItem(context, workflowItem);
    }

    @Override
    public void update(Context context, TaskListItem taskListItem) throws SQLException {
        taskListItemDAO.save(context, taskListItem);
    }
}
