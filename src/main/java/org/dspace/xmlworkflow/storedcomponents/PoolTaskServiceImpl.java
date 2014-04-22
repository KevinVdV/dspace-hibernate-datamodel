package org.dspace.xmlworkflow.storedcomponents;

import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.eperson.service.GroupService;
import org.dspace.xmlworkflow.storedcomponents.dao.PoolTaskDAO;
import org.dspace.xmlworkflow.storedcomponents.service.InProgressUserService;
import org.dspace.xmlworkflow.storedcomponents.service.PoolTaskService;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 21/04/14
 * Time: 15:55
 * To change this template use File | Settings | File Templates.
 */
public class PoolTaskServiceImpl implements PoolTaskService {

    @Autowired(required = true)
    protected PoolTaskDAO poolTaskDAO;

    @Autowired(required = true)
    protected GroupService groupService;
    @Autowired(required = true)
    protected InProgressUserService inProgressUserService;

    public PoolTaskServiceImpl()
    {
    }

    public PoolTask find(Context context, int id) throws SQLException {
        return poolTaskDAO.findByID(context, PoolTask.class, id);
    }

    public List<PoolTask> findByEPerson(Context context, EPerson ePerson) throws SQLException
    {
        List<PoolTask> result = poolTaskDAO.findByEPerson(context, ePerson);
        //Get all PoolTasks for groups of which this eperson is a member
        List<Group> groups = groupService.allMemberGroups(context, ePerson);
        result.addAll(findByGroups(context, ePerson, groups));
        return result;
    }

    public List<PoolTask> findByGroups(Context context, EPerson ePerson, List<Group> groups) throws SQLException {
        List<PoolTask> result = new ArrayList<PoolTask>();
        for (Group group : groups) {
            List<PoolTask> groupTasks = poolTaskDAO.findByGroup(context, group);
            for (PoolTask poolTask : groupTasks) {
                XmlWorkflowItem workflowItem = poolTask.getWorkflowItem();
                if(inProgressUserService.findByWorkflowItemAndEPerson(context, workflowItem, ePerson) == null){
                    result.add(poolTask);
                }
            }
        }
        return result;
    }

    public List<PoolTask> findByWorkflowItem(Context context, XmlWorkflowItem workflowItem) throws SQLException {
        return poolTaskDAO.findByWorkflowItem(context, workflowItem);
    }

    public PoolTask findByWorkflowItemAndEPerson(Context context, XmlWorkflowItem workflowItem, EPerson ePerson) throws SQLException
    {
        PoolTask poolTask = poolTaskDAO.findByWorkflowItemAndEPerson(context, workflowItem, ePerson);

        //If there is a pooltask for this eperson, return it
        if(poolTask != null)
            return poolTask;
        else{
            //If the user has a is processing or has finished the step for a workflowitem, there is no need to look for pooltasks for one of his
            //groups because the user already has the task claimed
            if(inProgressUserService.findByWorkflowItemAndEPerson(context, workflowItem, ePerson)!=null){
                return null;
            }
            else{
                //If the user does not have a claimedtask yet, see whether one of the groups of the user has pooltasks
                //for this workflow item
                List<Group> groups = groupService.allMemberGroups(context, ePerson);
                for (Group group : groups) {
                    poolTask = poolTaskDAO.findByWorkflowItemAndGroup(context, group, workflowItem);
                    if(poolTask != null)
                    {
                        return poolTask;
                    }

                }
            }
        }
        return null;
    }
    public PoolTask create(Context context) throws SQLException {
        return poolTaskDAO.create(context, new PoolTask());
    }

    public void deleteByWorkflowItem(Context context, XmlWorkflowItem workflowItem) throws SQLException {
        List<PoolTask> tasks = findByWorkflowItem(context, workflowItem);
        //Use an iterator to remove the tasks !
        Iterator<PoolTask> iterator = tasks.iterator();
        while (iterator.hasNext()) {
            PoolTask poolTask = iterator.next();
            delete(context, poolTask);
        }
    }

    public void delete(Context context, PoolTask poolTask) throws SQLException
    {
        poolTaskDAO.delete(context, poolTask);
    }


    public void update(Context context, PoolTask poolTask) throws SQLException {
        poolTaskDAO.save(context, poolTask);
    }
}
