package org.dspace.content.factory;

import org.dspace.content.DSpaceObject;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.WorkspaceItem;
import org.dspace.content.service.*;
import org.dspace.core.Constants;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.utils.DSpace;
import org.dspace.workflow.factory.WorkflowServiceFactory;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 2/06/14
 * Time: 10:55
 */
public abstract class ContentServiceFactory {

    public abstract BitstreamFormatService getBitstreamFormatService();

    public abstract BitstreamService getBitstreamService();

    public abstract BundleService getBundleService();

    public abstract CollectionService getCollectionService();

    public abstract CommunityService getCommunityService();

    public abstract ItemService getItemService();

    public abstract MetadataFieldService getMetadataFieldService();

    public abstract MetadataSchemaService getMetadataSchemaService();

    public abstract MetadataValueService getMetadataValueService();

    public abstract WorkspaceItemService getWorkspaceItemService();

    public abstract SubscriptionService getSubscriptionService();

    public abstract InstallItemService getInstallItemService();

    public abstract SupervisedItemService getSupervisedItemService();

    public abstract SiteService getSiteService();

    public InProgressSubmissionService getInProgressSubmissionService(InProgressSubmission inProgressSubmission)
    {
        if(inProgressSubmission instanceof WorkspaceItem)
        {
            return getWorkspaceItemService();
        }
        else
        {
            return WorkflowServiceFactory.getInstance().getWorkflowItemService();
        }
    }
    public DSpaceObjectService<DSpaceObject> getDSpaceObjectService(DSpaceObject dso)
    {
        // No need to worry when supressing, as long as our "getDSpaceObjectManager" method is properly implemented
        // no casting issues should occur
        @SuppressWarnings("unchecked")
        DSpaceObjectService manager = getDSpaceObjectService(dso.getType());
        return manager;
    }

    public DSpaceObjectService getDSpaceObjectService(int type){
        switch (type)
        {
            case Constants.BITSTREAM:
                return getBitstreamService();
            case Constants.BUNDLE:
                return getBundleService();
            case Constants.ITEM:
                return getItemService();
            case Constants.COLLECTION:
                return getCollectionService();
            case Constants.COMMUNITY:
                return getCommunityService();
            case Constants.GROUP:
                return EPersonServiceFactory.getInstance().getGroupService();
            case Constants.EPERSON:
                return EPersonServiceFactory.getInstance().getEPersonService();
            default:
                throw new UnsupportedOperationException();
        }
    }

    public static ContentServiceFactory getInstance(){
        return new DSpace().getServiceManager().getServiceByName("contentServiceFactory", ContentServiceFactory.class);
    }

}
