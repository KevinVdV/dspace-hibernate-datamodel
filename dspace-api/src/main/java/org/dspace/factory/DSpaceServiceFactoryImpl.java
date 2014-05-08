package org.dspace.factory;

import org.dspace.app.util.service.WebAppService;
import org.dspace.authorize.service.ResourcePolicyService;
import org.dspace.checker.service.ChecksumHistoryService;
import org.dspace.checker.service.ChecksumResultService;
import org.dspace.checker.service.MostRecentChecksumService;
import org.dspace.content.*;
import org.dspace.content.service.*;
import org.dspace.eperson.service.EPersonService;
import org.dspace.eperson.service.GroupService;
import org.dspace.eperson.service.RegistrationDataService;
import org.dspace.handle.service.HandleService;
import org.dspace.identifier.DOIService;
import org.dspace.workflow.service.TaskListItemService;
import org.dspace.workflow.service.WorkflowItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 01/04/14
 * Time: 08:05
 * To change this template use File | Settings | File Templates.
 */
@Component
public class DSpaceServiceFactoryImpl extends DSpaceServiceFactory {

    @Autowired(required = true)
    private BitstreamFormatService bitstreamFormatService;
    @Autowired(required = true)
    private BitstreamService bitstreamService;
    @Autowired(required = true)
    private BundleService bundleService;
    @Autowired(required = true)
    private ItemService itemService;
    @Autowired(required = true)
    private CollectionService collectionService;
    @Autowired(required = true)
    private CommunityService communityService;
    @Autowired(required = true)
    private GroupService groupService;
    @Autowired(required = true)
    private EPersonService epersonService;
    @Autowired(required = true)
    private MetadataSchemaService metadataSchemaService;
    @Autowired(required = true)
    private MetadataFieldService metadataFieldService;
    @Autowired(required = true)
    private MetadataValueService metadataValueService;
    @Autowired(required = true)
    private WorkspaceItemService workspaceItemService;
    @Autowired(required = true)
    private WorkflowItemService workflowItemService;
    @Autowired(required = true)
    private ResourcePolicyService resourcePolicyService;
    @Autowired(required = true)
    private TaskListItemService taskListItemService;
    @Autowired(required = true)
    private HandleService handleService;
    @Autowired(required = true)
    private SubscriptionService subscriptionService;
    @Autowired(required = true)
    private RegistrationDataService registrationDataService;
    @Autowired(required = true)
    private MostRecentChecksumService mostRecentChecksumService;
    @Autowired(required = true)
    private ChecksumHistoryService checksumHistoryService;
    @Autowired(required = true)
    private ChecksumResultService checksumResultService;
    @Autowired(required = true)
    private InstallItemService installItemService;
    @Autowired(required = true)
    private WebAppService webAppService;
    @Autowired(required = true)
    private SupervisedItemService supervisedItemService;
    @Autowired(required = true)
    private DOIService doiService;


    public BitstreamFormatService getBitstreamFormatService()
    {
        return bitstreamFormatService;
    }

    public BitstreamService getBitstreamService()
    {
        return bitstreamService;
    }

    public BundleService getBundleService()
    {
        return bundleService;
    }

    public CollectionService getCollectionService()
    {
        return collectionService;
    }

    public CommunityService getCommunityService()
    {
        return communityService;
    }

    public EPersonService getEPersonService()
    {
        return epersonService;
    }

    public GroupService getGroupService()
    {
        return groupService;
    }

    public ItemService getItemService()
    {
        return itemService;
    }

    public MetadataSchemaService getMetadataSchemaService()
    {
        return metadataSchemaService;
    }

    public MetadataFieldService getMetadataFieldService()
    {
        return metadataFieldService;
    }

    public MetadataValueService getMetadataValueService()
    {
        return metadataValueService;
    }

    public WorkspaceItemService getWorkspaceItemService()
    {
        return workspaceItemService;
    }

    public WorkflowItemService getWorkflowItemService() {
        return workflowItemService;
    }

    public ResourcePolicyService getResourcePolicyService() {
        return resourcePolicyService;
    }

    public TaskListItemService getTaskListItemService() {
        return taskListItemService;
    }

    public HandleService getHandleService() {
        return handleService;
    }

    @Override
    public SubscriptionService getSubscriptionService() {
        return subscriptionService;
    }

    @Override
    public RegistrationDataService getRegistrationDataService() {
        return registrationDataService;
    }

    @Override
    public MostRecentChecksumService getMostRecentChecksumService() {
        return mostRecentChecksumService;
    }

    @Override
    public ChecksumHistoryService getChecksumHistoryService() {
        return checksumHistoryService;
    }

    @Override
    public ChecksumResultService getChecksumResultService() {
        return checksumResultService;
    }

    @Override
    public InstallItemService getInstallItemService() {
        return installItemService;
    }

    @Override
    public WebAppService getWebAppService() {
        return webAppService;
    }

    @Override
    public SupervisedItemService getSupervisedItemService() {
        return supervisedItemService;
    }

    @Override
    public DOIService getDOIService() {
        return doiService;
    }
}
