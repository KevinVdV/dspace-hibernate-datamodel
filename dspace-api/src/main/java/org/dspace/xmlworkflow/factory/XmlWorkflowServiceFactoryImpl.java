package org.dspace.xmlworkflow.factory;

import org.dspace.workflow.WorkflowItemService;
import org.dspace.workflow.WorkflowService;
import org.dspace.xmlworkflow.service.WorkflowRequirementsService;
import org.dspace.xmlworkflow.service.XmlWorkflowService;
import org.dspace.xmlworkflow.storedcomponents.service.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 23/05/14
 * Time: 13:34
 */
public class XmlWorkflowServiceFactoryImpl extends XmlWorkflowServiceFactory {

    @Autowired(required = true)
    private XmlWorkflowFactory workflowFactory;
    @Autowired(required = true)
    private WorkflowRequirementsService workflowRequirementsService;
    @Autowired(required = true)
    private XmlWorkflowService xmlWorkflowService;
    @Autowired(required = true)
    private ClaimedTaskService claimedTaskService;
    @Autowired(required = true)
    private CollectionRoleService collectionRoleService;
    @Autowired(required = true)
    private InProgressUserService inProgressUserService;
    @Autowired(required = true)
    private PoolTaskService poolTaskService;
    @Autowired(required = true)
    private WorkflowItemRoleService workflowItemRoleService;
    @Autowired(required = true)
    private XmlWorkflowItemService xmlWorkflowItemService;

    @Override
    public XmlWorkflowFactory getWorkflowFactory() {
        return workflowFactory;
    }

    @Override
    public WorkflowRequirementsService getWorkflowRequirementsService() {
        return workflowRequirementsService;
    }

    @Override
    public XmlWorkflowService getXmlWorkflowService() {
        return xmlWorkflowService;
    }

    @Override
    public ClaimedTaskService getClaimedTaskService() {
        return claimedTaskService;
    }

    @Override
    public CollectionRoleService getCollectionRoleService() {
        return collectionRoleService;
    }

    @Override
    public InProgressUserService getInProgressUserService() {
        return inProgressUserService;
    }

    @Override
    public PoolTaskService getPoolTaskService() {
        return poolTaskService;
    }

    @Override
    public WorkflowItemRoleService getWorkflowItemRoleService() {
        return workflowItemRoleService;
    }

    @Override
    public XmlWorkflowItemService getXmlWorkflowItemService() {
        return xmlWorkflowItemService;
    }

    @Override
    public WorkflowService getWorkflowService() {
        return getXmlWorkflowService();
    }

    @Override
    public WorkflowItemService getWorkflowItemService() {
        return getXmlWorkflowItemService();
    }
}
