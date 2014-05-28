package org.dspace.xmlworkflow.storedcomponents.dao;

import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;
import org.dspace.eperson.EPerson;
import org.dspace.xmlworkflow.storedcomponents.WorkflowItemRole;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;

import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 21/04/14
 * Time: 10:55
 * To change this template use File | Settings | File Templates.
 */
public interface WorkflowItemRoleDAO extends GenericDAO<WorkflowItemRole> {

    public List<WorkflowItemRole> findByWorkflowItemAndRole(Context context, XmlWorkflowItem workflowItem, String role) throws SQLException;

    public List<WorkflowItemRole> findByWorkflowItem(Context context, XmlWorkflowItem workflowItem) throws SQLException;

    public List<WorkflowItemRole> findByEPerson(Context context, EPerson ePerson) throws SQLException;
}
