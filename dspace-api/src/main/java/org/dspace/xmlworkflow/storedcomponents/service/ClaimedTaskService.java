package org.dspace.xmlworkflow.storedcomponents.service;

import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.service.DSpaceCRUDService;
import org.dspace.xmlworkflow.storedcomponents.ClaimedTask;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;

import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 21/04/14
 * Time: 09:46
 * To change this template use File | Settings | File Templates.
 */
public interface ClaimedTaskService extends DSpaceCRUDService<ClaimedTask>{

    public List<ClaimedTask> findByWorkflowItem(Context context, XmlWorkflowItem workflowItem) throws SQLException;

    public ClaimedTask findByWorkflowItemAndEPerson(Context context, XmlWorkflowItem workflowItem, EPerson ePerson) throws SQLException;

    public List<ClaimedTask> findByEperson(Context context, EPerson ePerson) throws SQLException;

    public List<ClaimedTask> findByWorkflowItemAndStepId(Context c, XmlWorkflowItem workflowItem, String stepID) throws SQLException;

    public ClaimedTask findByEPersonAndWorkflowItemAndStepIdAndActionId(Context c, EPerson ePerson, XmlWorkflowItem workflowItem, String stepID, String actionID) throws SQLException;

    public List<ClaimedTask> findByWorkflowItemAndStepIdAndActionId(Context c, XmlWorkflowItem workflowItem, String stepID, String actionID) throws SQLException;

    public List<ClaimedTask> findByStep(Context context, String stepID) throws SQLException;

    public void deleteByWorkflowItem(Context context, XmlWorkflowItem workflowItem) throws SQLException;
}
