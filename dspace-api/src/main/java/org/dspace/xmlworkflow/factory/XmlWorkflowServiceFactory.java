package org.dspace.xmlworkflow.factory;

import org.dspace.utils.DSpace;
import org.dspace.workflow.factory.WorkflowServiceFactory;
import org.dspace.xmlworkflow.service.WorkflowRequirementsService;
import org.dspace.xmlworkflow.service.XmlWorkflowService;
import org.dspace.xmlworkflow.storedcomponents.service.*;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 23/05/14
 * Time: 13:33
 */
public abstract class XmlWorkflowServiceFactory extends WorkflowServiceFactory {

    public abstract XmlWorkflowFactory getWorkflowFactory();

    public abstract WorkflowRequirementsService getWorkflowRequirementsService();

    public abstract XmlWorkflowService getXmlWorkflowService();

    public abstract ClaimedTaskService getClaimedTaskService();

    public abstract CollectionRoleService getCollectionRoleService();

    public abstract InProgressUserService getInProgressUserService();

    public abstract PoolTaskService getPoolTaskService();

    public abstract WorkflowItemRoleService getWorkflowItemRoleService();

    public abstract XmlWorkflowItemService getXmlWorkflowItemService();

    public static XmlWorkflowServiceFactory getInstance()
    {
        return new DSpace().getServiceManager().getServiceByName("xmlWorkflowServiceFactory", XmlWorkflowServiceFactory.class);
    }
}
