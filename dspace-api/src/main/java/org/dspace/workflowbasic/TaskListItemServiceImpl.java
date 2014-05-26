package org.dspace.workflowbasic;

import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.workflowbasic.dao.TaskListItemDAO;
import org.dspace.workflowbasic.service.TaskListItemService;
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
    public TaskListItem create(Context context, BasicWorkflowItem workflowItem, EPerson ePerson) throws SQLException {
        TaskListItem taskListItem = taskListItemDAO.create(context, new TaskListItem());
        taskListItem.setWorkflowItem(workflowItem);
        taskListItem.setEPerson(ePerson);
        update(context, taskListItem);
        return taskListItem;
    }

    @Override
    public void deleteByWorkflowItem(Context context, BasicWorkflowItem workflowItem) throws SQLException {
        taskListItemDAO.deleteByWorkflowItem(context, workflowItem);
    }

    @Override
    public void update(Context context, TaskListItem taskListItem) throws SQLException {
        taskListItemDAO.save(context, taskListItem);
    }
}
