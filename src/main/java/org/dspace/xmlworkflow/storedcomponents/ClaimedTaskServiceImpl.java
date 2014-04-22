/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.xmlworkflow.storedcomponents;

import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.xmlworkflow.storedcomponents.dao.ClaimedTaskDAO;
import org.dspace.xmlworkflow.storedcomponents.service.ClaimedTaskService;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Claimed task representing the database representation of an action claimed by an eperson
 *
 * @author Bram De Schouwer (bram.deschouwer at dot com)
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 */
public class ClaimedTaskServiceImpl implements ClaimedTaskService {
    /** Our context */
    @Autowired(required = true)
    protected ClaimedTaskDAO claimedTaskDAO;

    public ClaimedTaskServiceImpl()
    {
    }

    public ClaimedTask find(Context context, int id)
            throws SQLException {
        return claimedTaskDAO.findByID(context, ClaimedTask.class, id);
    }

    public List<ClaimedTask> findByWorkflowItem(Context context, XmlWorkflowItem workflowItem) throws SQLException {
        return claimedTaskDAO.findByWorkflowItem(context, workflowItem);
    }

    public ClaimedTask findByWorkflowItemAndEPerson(Context context, XmlWorkflowItem workflowItem, EPerson ePerson) throws SQLException {
        return claimedTaskDAO.findByWorkflowItemAndEPerson(context, workflowItem, ePerson);
    }

    public List<ClaimedTask> findByEperson(Context context, EPerson ePerson) throws SQLException {
        return claimedTaskDAO.findByEperson(context, ePerson);
    }

    public List<ClaimedTask> findByWorkflowItemAndStepId(Context c, XmlWorkflowItem workflowItem, String stepID) throws SQLException {
        return claimedTaskDAO.findByWorkflowItemAndStepId(c, workflowItem, stepID);

    }

    public ClaimedTask findByEPersonAndWorkflowItemAndStepIdAndActionId(Context c, EPerson ePerson, XmlWorkflowItem workflowItem, String stepID, String actionID) throws SQLException {
        return claimedTaskDAO.findByEPersonAndWorkflowItemAndStepIdAndActionId(c, ePerson, workflowItem, stepID, actionID);

    }
    public List<ClaimedTask> findByWorkflowItemAndStepIdAndActionId(Context c, XmlWorkflowItem workflowItem, String stepID, String actionID) throws SQLException {
        return claimedTaskDAO.findByWorkflowItemAndStepIdAndActionId(c, workflowItem, stepID, actionID);
    }

    public List<ClaimedTask> findByStep(Context context, String stepID) throws SQLException {
        return claimedTaskDAO.findByStep(context, stepID);
    }

    public ClaimedTask create(Context context) throws SQLException {

        return claimedTaskDAO.create(context, new ClaimedTask());
    }


    public void delete(Context context, ClaimedTask claimedTask) throws SQLException
    {
        claimedTaskDAO.delete(context, claimedTask);
    }


    public void update(Context context, ClaimedTask claimedTask) throws SQLException
    {
        claimedTaskDAO.save(context, claimedTask);
    }
}