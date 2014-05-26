/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.xmlworkflow;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.xmlworkflow.factory.XmlWorkflowFactory;
import org.dspace.xmlworkflow.service.WorkflowRequirementsService;
import org.dspace.xmlworkflow.service.XmlWorkflowService;
import org.dspace.xmlworkflow.state.Step;
import org.dspace.xmlworkflow.state.Workflow;
import org.dspace.xmlworkflow.storedcomponents.*;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;
import org.dspace.xmlworkflow.storedcomponents.service.InProgressUserService;
import org.dspace.xmlworkflow.storedcomponents.service.PoolTaskService;
import org.dspace.xmlworkflow.storedcomponents.service.XmlWorkflowItemService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * A class that contains utililty methods related to the workflow
 * The adding/removing from claimed users and ensuring that
 * if multiple users have to perform these steps that a count is kept
 * so that no more then the allowed user count are allowed to perform their actions
 *
 * @author Bram De Schouwer (bram.deschouwer at dot com)
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 */
public class WorkflowRequirementsServiceImpl implements WorkflowRequirementsService {

    @Autowired(required = true)
    protected PoolTaskService poolTaskService;
    @Autowired(required = true)
    protected InProgressUserService inProgressUserService;
    @Autowired(required = true)
    protected XmlWorkflowItemService xmlWorkflowItemService;
    @Autowired(required = true)
    protected XmlWorkflowService xmlWorkflowService;
    @Autowired(required = true)
    protected XmlWorkflowFactory workflowFactory;
    @Autowired(required = true)
    protected ItemService itemService;


    /**
     * Adds a claimed user in the metadata
     * if enough users have claimed this task (claimed or finished) to meet the required number
     * the pooled tasks will be deleted
     * @param c the dspace context
     * @param wfi the workflow item
     * @param step the step for which we are accepting
     * @param user the current user
     * @throws SQLException ...
     * @throws AuthorizeException ...
     * @throws IOException ...
     */
    @Override
    public void addClaimedUser(Context c, XmlWorkflowItem wfi, Step step, EPerson user) throws SQLException, AuthorizeException, IOException {

        //Make sure we delete the pooled task for our current user if the task is not a group pooltask
        PoolTask task = poolTaskService.findByWorkflowItemAndEPerson(c, wfi, user);
        if(task != null && task.getePerson() != null){
            xmlWorkflowService.deletePooledTask(c, wfi, task);
        }

        InProgressUser ipu = inProgressUserService.create(c);
        ipu.setWorkflowItem(wfi);
        ipu.setePerson(user);
        ipu.setFinished(false);
        inProgressUserService.update(c, ipu);
        int totalUsers = inProgressUserService.getNumberOfInProgressUsers(c, wfi) + inProgressUserService.getNumberOfFinishedUsers(c, wfi);

        if(totalUsers == step.getRequiredUsers()){
            //If enough users have claimed/finished this step then remove the tasks
            xmlWorkflowService.deleteAllPooledTasks(c, wfi);
        }
        xmlWorkflowItemService.update(c, wfi);
    }

    @Override
    public void removeClaimedUser(Context c, XmlWorkflowItem wfi, EPerson user, String stepID) throws SQLException, IOException, WorkflowConfigurationException, AuthorizeException {
        //Check if we had reached our max number @ this moment
        int totalUsers = inProgressUserService.getNumberOfInProgressUsers(c, wfi) + inProgressUserService.getNumberOfFinishedUsers(c, wfi);

        //Then remove the current user from the inProgressUsers
        inProgressUserService.delete(c, inProgressUserService.findByWorkflowItemAndEPerson(c, wfi, user));

        Workflow workflow = workflowFactory.getWorkflow(c, wfi.getCollection());
        Step step = workflow.getStep(stepID);

//        WorkflowManager.deleteOwnedTask(c, user, wfi, step, step.getActionConfig());
        //We had reached our total user, so recreate tasks for the user who don't have one
        if(totalUsers == step.getRequiredUsers()){

            //Create a list of the users we are to ignore
            List<InProgressUser> toIgnore = inProgressUserService.findByWorkflowItem(c, wfi);

            //Remove the users to ignore
            RoleMembers roleMembers = step.getRole().getMembers(c, wfi);
            //Create a list out all the users we are to pool a task for
            for (InProgressUser ipu: toIgnore) {
                roleMembers.removeEperson(ipu.getePerson().getID());
            }
            step.getUserSelectionMethod().getProcessingAction().regenerateTasks(c, wfi, roleMembers);

        }else{
            //If the user previously had a personal PoolTask, this must be regenerated. Therefore we call the regeneration method
            //with only one EPerson
            RoleMembers role = step.getRole().getMembers(c, wfi);
            List<EPerson> epersons = role.getEPersons();
            for(EPerson eperson: epersons){
                if(eperson.getID() == user.getID()){
                    RoleMembers memberToRegenerateTasksFor = new RoleMembers();
                    memberToRegenerateTasksFor.addEPerson(user);
                    step.getUserSelectionMethod().getProcessingAction().regenerateTasks(c, wfi, memberToRegenerateTasksFor);
                    break;
                }
            }
        }
        //Update our item
        itemService.update(c, wfi.getItem());
    }

    /**
     * Adds a finished user in the metadata
     * this method will also remove the user from the inprogress metadata
     * @param c the dspace context
     * @param wfi the workflow item
     * @param user the current user
     * @throws AuthorizeException ...
     * @throws SQLException ...
     */
    @Override
    public void addFinishedUser(Context c, XmlWorkflowItem wfi, EPerson user) throws AuthorizeException, SQLException {
        InProgressUser ipu = inProgressUserService.findByWorkflowItemAndEPerson(c, wfi, user);
        ipu.setFinished(true);
        inProgressUserService.update(c, ipu);
    }


    @Override
    public void clearInProgressUsers(Context c, XmlWorkflowItem wfi) throws AuthorizeException, SQLException {
        List<InProgressUser> ipus = inProgressUserService.findByWorkflowItem(c, wfi);
        for(InProgressUser ipu: ipus){
            inProgressUserService.delete(c, ipu);
        }
    }

}
