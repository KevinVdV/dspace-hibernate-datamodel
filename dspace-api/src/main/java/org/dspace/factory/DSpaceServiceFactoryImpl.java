package org.dspace.factory;

import org.dspace.app.util.service.WebAppService;
import org.dspace.authorize.service.ResourcePolicyService;
import org.dspace.checker.service.ChecksumHistoryService;
import org.dspace.checker.service.ChecksumResultService;
import org.dspace.checker.service.MostRecentChecksumService;
import org.dspace.content.service.*;
import org.dspace.eperson.service.EPersonService;
import org.dspace.eperson.service.GroupService;
import org.dspace.eperson.service.RegistrationDataService;
import org.dspace.handle.service.HandleService;
import org.dspace.identifier.DOIService;
import org.dspace.workflowbasic.service.BasicWorkflowItemService;
import org.dspace.workflowbasic.service.TaskListItemService;
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
    private MetadataSchemaService metadataSchemaService;
    @Autowired(required = true)
    private MetadataFieldService metadataFieldService;
    @Autowired(required = true)
    private MetadataValueService metadataValueService;
    @Autowired(required = true)
    private WorkspaceItemService workspaceItemService;
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


    @Override
    public BitstreamFormatService getBitstreamFormatService()
    {
        return bitstreamFormatService;
    }

    @Override
    public BitstreamService getBitstreamService()
    {
        return bitstreamService;
    }

    @Override
    public BundleService getBundleService()
    {
        return bundleService;
    }

    @Override
    public CollectionService getCollectionService()
    {
        return collectionService;
    }

    @Override
    public CommunityService getCommunityService()
    {
        return communityService;
    }

    @Override
    public ItemService getItemService()
    {
        return itemService;
    }

    @Override
    public MetadataSchemaService getMetadataSchemaService()
    {
        return metadataSchemaService;
    }

    @Override
    public MetadataFieldService getMetadataFieldService()
    {
        return metadataFieldService;
    }

    @Override
    public MetadataValueService getMetadataValueService()
    {
        return metadataValueService;
    }

    @Override
    public WorkspaceItemService getWorkspaceItemService()
    {
        return workspaceItemService;
    }

    @Override
    public HandleService getHandleService() {
        return handleService;
    }

    @Override
    public SubscriptionService getSubscriptionService() {
        return subscriptionService;
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
