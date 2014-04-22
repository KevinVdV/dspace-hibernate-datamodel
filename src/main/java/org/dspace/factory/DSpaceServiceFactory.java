package org.dspace.factory;

import org.dspace.authorize.service.ResourcePolicyService;
import org.dspace.content.*;
import org.dspace.content.service.*;
import org.dspace.core.Constants;
import org.dspace.eperson.service.EPersonService;
import org.dspace.eperson.service.GroupService;
import org.dspace.handle.service.HandleService;
import org.dspace.utils.DSpace;
import org.dspace.workflow.WorkflowItem;
import org.dspace.workflow.service.TaskListItemService;
import org.dspace.workflow.service.WorkflowItemService;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 01/04/14
 * Time: 08:05
 * To change this template use File | Settings | File Templates.
 */
public abstract class DSpaceServiceFactory {
    public abstract BitstreamFormatService getBitstreamFormatService();

    public abstract BitstreamService getBitstreamService();

    public abstract BundleService getBundleService();

    public abstract CollectionService getCollectionService();

    public abstract CommunityService getCommunityService();

    public abstract EPersonService getEPersonService();

    public abstract GroupService getGroupService();

    public abstract ItemService getItemService();

    public abstract MetadataFieldService getMetadataFieldService();

    public abstract MetadataSchemaService getMetadataSchemaService();

    public abstract MetadataValueService getMetadataValueService();

    public abstract WorkspaceItemService getWorkspaceItemService();

    public abstract WorkflowItemService getWorkflowItemService();

    public abstract ResourcePolicyService getResourcePolicyService();

    public abstract TaskListItemService getTaskListItemService();

    public abstract HandleService getHandleService();

    public abstract SubscriptionService getSubscriptionService();

    public InProgressSubmissionService getInProgressSubmissionService(InProgressSubmission inProgressSubmission)
    {
        if(inProgressSubmission instanceof WorkspaceItem)
        {
            return getWorkspaceItemService();
        }else
        if(inProgressSubmission instanceof WorkflowItem)
        {
            return getWorkflowItemService();
        }

        //TODO: Implement workflowItem checks
        throw new UnsupportedOperationException();
    }
    public DSpaceObjectService<DSpaceObject> getDSpaceObjectManager(DSpaceObject dso)
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
                return getGroupService();
            case Constants.EPERSON:
                return getEPersonService();
            default:
                throw new UnsupportedOperationException();
        }
    }

    public static DSpaceServiceFactory getInstance(){
        return new DSpace().getServiceManager().getServiceByName("serviceFactory", DSpaceServiceFactory.class);
    }
}
