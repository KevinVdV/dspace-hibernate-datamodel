package org.dspace.xmlworkflow.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.xmlworkflow.WorkflowConfigurationException;
import org.dspace.xmlworkflow.state.Step;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;

import java.io.IOException;
import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 23/05/14
 * Time: 11:19
 */
public interface WorkflowRequirementsService {

    public static final String WORKFLOW_SCHEMA = "workflow";

    public void addClaimedUser(Context c, XmlWorkflowItem wfi, Step step, EPerson user) throws SQLException, AuthorizeException, IOException;

    public void removeClaimedUser(Context c, XmlWorkflowItem wfi, EPerson user, String stepID) throws SQLException, IOException, WorkflowConfigurationException, AuthorizeException;

    public void addFinishedUser(Context c, XmlWorkflowItem wfi, EPerson user) throws AuthorizeException, SQLException;

    public void clearInProgressUsers(Context c, XmlWorkflowItem wfi) throws AuthorizeException, SQLException;
}
