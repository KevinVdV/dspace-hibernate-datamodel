package org.dspace.workflowbasic.factory;

import org.dspace.workflow.WorkflowItemService;
import org.dspace.workflow.WorkflowService;
import org.dspace.workflowbasic.service.BasicWorkflowItemService;
import org.dspace.workflowbasic.service.BasicWorkflowService;
import org.dspace.workflowbasic.service.TaskListItemService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 26/05/14
 * Time: 13:20
 */
public class BasicWorkflowServiceFactoryImpl extends BasicWorkflowServiceFactory {

    @Autowired(required = true)
    private BasicWorkflowService basicWorkflowService;
    @Autowired(required = true)
    private BasicWorkflowItemService basicWorkflowItemService;
    @Autowired(required = true)
    private TaskListItemService taskListItemService;


    @Override
    public BasicWorkflowService getBasicWorkflowService() {
        return basicWorkflowService;
    }

    @Override
    public BasicWorkflowItemService getBasicWorkflowItemService() {
        return basicWorkflowItemService;
    }

    @Override
    public TaskListItemService getTaskListItemService() {
        return taskListItemService;
    }

    @Override
    public WorkflowService getWorkflowService() {
        return getBasicWorkflowService();
    }

    @Override
    public WorkflowItemService getWorkflowItemService() {
        return getBasicWorkflowItemService();
    }
}
