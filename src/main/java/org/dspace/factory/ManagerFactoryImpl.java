package org.dspace.factory;

import org.dspace.content.*;
import org.dspace.eperson.*;
import org.dspace.kernel.ServiceManager;
import org.dspace.utils.DSpace;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 01/04/14
 * Time: 08:05
 * To change this template use File | Settings | File Templates.
 */
public class ManagerFactoryImpl extends AbstractDSpaceManagerFactory {

    //TODO: auto wire this in ?
    private static ServiceManager serviceManager = new DSpace().getServiceManager();

    public BitstreamFormatRepo getBitstreamFormatManager()
    {
        return serviceManager.getServiceByName(BitstreamFormatRepo.class.getName(), BitstreamFormatRepo.class);
    }

    public BitstreamRepo getBitstreamManager()
    {
        return serviceManager.getServiceByName(BitstreamRepo.class.getName(), BitstreamRepo.class);
    }

    public BundleRepo getBundleManager()
    {
        return serviceManager.getServiceByName(BundleRepo.class.getName(), BundleRepo.class);
    }

    public CollectionRepo getCollectionManager()
    {
        return serviceManager.getServiceByName(CollectionRepo.class.getName(), CollectionRepo.class);
    }

    public CommunityRepo getCommunityManager()
    {
        return serviceManager.getServiceByName(CommunityRepo.class.getName(), CommunityRepo.class);
    }

    public EPersonRepo getEPersonManager()
    {
        return serviceManager.getServiceByName(EPersonRepo.class.getName(), EPersonRepo.class);
    }

    public GroupRepo getGroupManager()
    {
        return serviceManager.getServiceByName(GroupRepo.class.getName(), GroupRepo.class);
    }

    public ItemRepo getItemManager()
    {
        return serviceManager.getServiceByName(ItemRepo.class.getName(), ItemRepo.class);
    }

    public MetadataFieldRepo getMetadataFieldManager()
    {
        return serviceManager.getServiceByName(MetadataFieldRepo.class.getName(), MetadataFieldRepo.class);
    }

    public MetadataSchemaRepo getMetadataSchemaManager()
    {
        return serviceManager.getServiceByName(MetadataSchemaRepo.class.getName(), MetadataSchemaRepo.class);
    }

    public MetadataValueRepo getMetadataValueManager()
    {
        return serviceManager.getServiceByName(MetadataValueRepo.class.getName(), MetadataValueRepo.class);

    }

    public WorkspaceItemRepo getWorkspaceItemManager()
    {
        return serviceManager.getServiceByName(WorkspaceItemRepo.class.getName(), WorkspaceItemRepo.class);
    }
}
