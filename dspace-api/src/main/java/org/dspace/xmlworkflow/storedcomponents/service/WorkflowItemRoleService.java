package org.dspace.xmlworkflow.storedcomponents.service;

import org.dspace.core.Context;
import org.dspace.service.DSpaceCRUDService;
import org.dspace.xmlworkflow.storedcomponents.WorkflowItemRole;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;

import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 21/04/14
 * Time: 10:51
 * To change this template use File | Settings | File Templates.
 */
public interface WorkflowItemRoleService extends DSpaceCRUDService<WorkflowItemRole>
{
    public List<WorkflowItemRole> findByWorkflowItemAndRole(Context context, XmlWorkflowItem workflowItem, String role) throws SQLException;

    public List<WorkflowItemRole> findByWorkflowItem(Context context, XmlWorkflowItem workflowItem) throws SQLException;

    public void deleteForWorkflowItem(Context context, XmlWorkflowItem xmlWorkflowItem) throws SQLException;
}
