package org.dspace.xmlworkflow.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.WorkspaceItem;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.workflow.WorkflowService;
import org.dspace.xmlworkflow.RoleMembers;
import org.dspace.xmlworkflow.state.Step;
import org.dspace.xmlworkflow.state.actions.WorkflowActionConfig;
import org.dspace.xmlworkflow.storedcomponents.ClaimedTask;
import org.dspace.xmlworkflow.storedcomponents.PoolTask;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;

import javax.mail.MessagingException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 23/05/14
 * Time: 11:49
 */
public interface XmlWorkflowService extends WorkflowService<XmlWorkflowItem> {

    public void alertUsersOnTaskActivation(Context c, XmlWorkflowItem wfi, String emailTemplate, List<EPerson> epa, String ...arguments) throws IOException, SQLException, MessagingException;

    public void deleteAllTasks(Context c, XmlWorkflowItem wi) throws SQLException, AuthorizeException;

    public void deleteAllPooledTasks(Context c, XmlWorkflowItem wi) throws SQLException, AuthorizeException;

    public void deletePooledTask(Context c, XmlWorkflowItem wi, PoolTask task) throws SQLException, AuthorizeException;

    public void deleteClaimedTask(Context c, XmlWorkflowItem wi, ClaimedTask task) throws SQLException, AuthorizeException;

    public void createPoolTasks(Context context, XmlWorkflowItem wi, RoleMembers assignees, Step step, WorkflowActionConfig action) throws SQLException, AuthorizeException;

    public void createOwnedTask(Context c, XmlWorkflowItem wi, Step step, WorkflowActionConfig action, EPerson e) throws SQLException, AuthorizeException;

    public WorkspaceItem abort(Context c, XmlWorkflowItem wi, EPerson e) throws AuthorizeException, SQLException, IOException;

    public String getEPersonName(EPerson e) throws SQLException;
}