package org.dspace.xmlworkflow.storedcomponents;

import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.xmlworkflow.storedcomponents.dao.WorkflowItemRoleDAO;
import org.dspace.xmlworkflow.storedcomponents.service.WorkflowItemRoleService;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 21/04/14
 * Time: 10:51
 * To change this template use File | Settings | File Templates.
 */
public class WorkflowItemRoleServiceImpl implements WorkflowItemRoleService {

    @Autowired(required = true)
    private WorkflowItemRoleDAO workflowItemRoleDAO;

    public WorkflowItemRoleServiceImpl()
    {
    }

    @Override
    public WorkflowItemRole find(Context context, int id) throws SQLException
    {
        return workflowItemRoleDAO.findByID(context, WorkflowItemRole.class, id);
    }

    @Override
    public List<WorkflowItemRole> findByWorkflowItemAndRole(Context context, XmlWorkflowItem workflowItem, String role) throws SQLException {
        return workflowItemRoleDAO.findByWorkflowItemAndRole(context, workflowItem, role);
    }

    @Override
    public List<WorkflowItemRole> findByWorkflowItem(Context context, XmlWorkflowItem workflowItem) throws SQLException {
        return workflowItemRoleDAO.findByWorkflowItem(context, workflowItem);
    }

    @Override
    public List<WorkflowItemRole> findByEPerson(Context context, EPerson ePerson) throws SQLException {
        return workflowItemRoleDAO.findByEPerson(context, ePerson);
    }

    @Override
    public void deleteForWorkflowItem(Context context, XmlWorkflowItem xmlWorkflowItem) throws SQLException {
        Iterator<WorkflowItemRole> workflowItemRoles = findByWorkflowItem(context, xmlWorkflowItem).iterator();
        while (workflowItemRoles.hasNext()) {
            WorkflowItemRole workflowItemRole = workflowItemRoles.next();
            workflowItemRoles.remove();
            delete(context, workflowItemRole);
        }
    }

    @Override
    public WorkflowItemRole create(Context context) throws SQLException {
        return workflowItemRoleDAO.create(context, new WorkflowItemRole());
    }

    @Override
    public void delete(Context context, WorkflowItemRole workflowItemRole) throws SQLException
    {
        workflowItemRoleDAO.delete(context, workflowItemRole);
    }

    @Override
    public void update(Context context, WorkflowItemRole workflowItemRole) throws SQLException
    {
        workflowItemRoleDAO.save(context, workflowItemRole);
    }
}
