package org.dspace.workflow.factory;

import org.dspace.utils.DSpace;
import org.dspace.workflow.WorkflowItemService;
import org.dspace.workflow.WorkflowService;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 23/05/14
 * Time: 15:32
 */
public abstract class WorkflowServiceFactory {

    public abstract WorkflowService getWorkflowService();

    public abstract WorkflowItemService getWorkflowItemService();

    public static WorkflowServiceFactory getInstance()
    {
        return new DSpace().getServiceManager().getServiceByName("workflowFactory", WorkflowServiceFactory.class);
    }
}
