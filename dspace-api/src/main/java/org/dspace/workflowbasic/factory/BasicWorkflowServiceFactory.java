package org.dspace.workflowbasic.factory;

import org.dspace.utils.DSpace;
import org.dspace.workflow.factory.WorkflowServiceFactory;
import org.dspace.workflowbasic.service.BasicWorkflowItemService;
import org.dspace.workflowbasic.service.BasicWorkflowService;
import org.dspace.workflowbasic.service.TaskListItemService;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 23/05/14
 * Time: 14:21
 */
public abstract class BasicWorkflowServiceFactory extends WorkflowServiceFactory {

    public abstract BasicWorkflowService getBasicWorkflowService();

    public abstract BasicWorkflowItemService getBasicWorkflowItemService();

    public abstract TaskListItemService getTaskListItemService();

    public static BasicWorkflowServiceFactory getInstance()
    {
        return new DSpace().getServiceManager().getServiceByName("basicWorkflowServiceFactory", BasicWorkflowServiceFactory.class);
    }
}
