package org.dspace.factory;

import org.dspace.content.*;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 01/04/14
 * Time: 08:05
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractDSpaceManagerFactory implements ManagerFactory {


    public InProgressSubmissionRepo getInProgressSubmissionManager(InProgressSubmission inProgressSubmission)
    {
        if(inProgressSubmission instanceof WorkspaceItem)
        {
            return getWorkspaceItemManager();
        }
        //TODO: Implement workflowItem checks
        throw new UnsupportedOperationException();
    }
    public DSpaceObjectRepo getDSpaceObjectManager(DSpaceObject dso)
    {
        if(dso instanceof Bitstream)
        {
            return getBitstreamManager();
        }else
        if(dso instanceof Bundle)
        {
            return getBundleManager();
        }else
        if(dso instanceof Item)
        {
            return getItemManager();
        }else
        if(dso instanceof Collection)
        {
            return getCollectionManager();
        }else
        if(dso instanceof Community)
        {
            return getCommunityManager();
        }else
        if(dso instanceof Group)
        {
            return getGroupManager();
        }else
        if(dso instanceof EPerson)
        {
            return getEPersonManager();
        }else{
            throw new UnsupportedOperationException();
        }
    }
}
