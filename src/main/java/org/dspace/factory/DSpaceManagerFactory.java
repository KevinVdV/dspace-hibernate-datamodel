package org.dspace.factory;

import org.dspace.authorize.ResourcePolicyManager;
import org.dspace.content.*;
import org.dspace.core.Constants;
import org.dspace.eperson.EPersonManager;
import org.dspace.eperson.GroupManager;
import org.dspace.utils.DSpace;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 01/04/14
 * Time: 08:05
 * To change this template use File | Settings | File Templates.
 */
public abstract class DSpaceManagerFactory {

    public abstract BitstreamFormatManager getBitstreamFormatManager();

    public abstract BitstreamManager getBitstreamManager();

    public abstract BundleManager getBundleManager();

    public abstract CollectionManager getCollectionManager();

    public abstract CommunityManager getCommunityManager();

    public abstract EPersonManager getEPersonManager();

    public abstract GroupManager getGroupManager();

    public abstract ItemManager getItemManager();

    public abstract MetadataFieldManager getMetadataFieldManager();

    public abstract MetadataSchemaManager getMetadataSchemaManager();

    public abstract MetadataValueManager getMetadataValueManager();

    public abstract WorkspaceItemManager getWorkspaceItemManager();

    public abstract ResourcePolicyManager getResourcePolicyManager();

    public InProgressSubmissionManager getInProgressSubmissionManager(InProgressSubmission inProgressSubmission)
    {
        if(inProgressSubmission instanceof WorkspaceItem)
        {
            return getWorkspaceItemManager();
        }
        //TODO: Implement workflowItem checks
        throw new UnsupportedOperationException();
    }
    public DSpaceObjectManager getDSpaceObjectManager(DSpaceObject dso)
    {
        return getDSpaceObjectManager(dso.getType());
    }

    public DSpaceObjectManager getDSpaceObjectManager(int type){
        switch (type)
        {
            case Constants.BITSTREAM:
                return getBitstreamManager();
            case Constants.BUNDLE:
                return getBundleManager();
            case Constants.ITEM:
                return getItemManager();
            case Constants.COLLECTION:
                return getCollectionManager();
            case Constants.COMMUNITY:
                return getCommunityManager();
            case Constants.GROUP:
                return getGroupManager();
            case Constants.EPERSON:
                return getEPersonManager();
            default:
                throw new UnsupportedOperationException();
        }
    }

    public static DSpaceManagerFactory getInstance(){
        return new DSpace().getServiceManager().getServiceByName("managerFactory", DSpaceManagerFactory.class);
    }
}
