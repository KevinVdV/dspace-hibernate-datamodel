package org.dspace.factory;

import org.dspace.authorize.ResourcePolicyManager;
import org.dspace.content.*;
import org.dspace.eperson.*;
import org.dspace.workflow.TaskListItemManager;
import org.dspace.workflow.WorkflowItemManager;
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
public class ManagerFactoryImpl extends DSpaceManagerFactory {

    @Autowired(required = true)
    private BitstreamFormatManager bitstreamFormatManager;
    @Autowired(required = true)
    private BitstreamManager bitstreamManager;
    @Autowired(required = true)
    private BundleManager bundleManager;
    @Autowired(required = true)
    private ItemManager itemManager;
    @Autowired(required = true)
    private CollectionManager collectionManager;
    @Autowired(required = true)
    private CommunityManager communityManager;
    @Autowired(required = true)
    private GroupManager groupManager;
    @Autowired(required = true)
    private EPersonManager epersonManager;
    @Autowired(required = true)
    private MetadataSchemaManager metadataSchemaManager;
    @Autowired(required = true)
    private MetadataFieldManager metadataFieldManager;
    @Autowired(required = true)
    private MetadataValueManager metadataValueManager;
    @Autowired(required = true)
    private WorkspaceItemManager workspaceItemManager;
    @Autowired(required = true)
    private WorkflowItemManager workflowItemManager;
    @Autowired(required = true)
    private ResourcePolicyManager resourcePolicyManager;
    @Autowired(required = true)
    private TaskListItemManager taskListItemManager;


    public BitstreamFormatManager getBitstreamFormatManager()
    {
        return bitstreamFormatManager;
    }

    public BitstreamManager getBitstreamManager()
    {
        return bitstreamManager;
    }

    public BundleManager getBundleManager()
    {
        return bundleManager;
    }

    public CollectionManager getCollectionManager()
    {
        return collectionManager;
    }

    public CommunityManager getCommunityManager()
    {
        return communityManager;
    }

    public EPersonManager getEPersonManager()
    {
        return epersonManager;
    }

    public GroupManager getGroupManager()
    {
        return groupManager;
    }

    public ItemManager getItemManager()
    {
        return itemManager;
    }

    public MetadataSchemaManager getMetadataSchemaManager()
    {
        return metadataSchemaManager;
    }

    public MetadataFieldManager getMetadataFieldManager()
    {
        return metadataFieldManager;
    }

    public MetadataValueManager getMetadataValueManager()
    {
        return metadataValueManager;
    }

    public WorkspaceItemManager getWorkspaceItemManager()
    {
        return workspaceItemManager;
    }

    public WorkflowItemManager getWorkflowItemManager() {
        return workflowItemManager;
    }

    public ResourcePolicyManager getResourcePolicyManager() {
        return resourcePolicyManager;
    }

    @Override
    public TaskListItemManager getTaskListItemManager() {
        return taskListItemManager;
    }
}
