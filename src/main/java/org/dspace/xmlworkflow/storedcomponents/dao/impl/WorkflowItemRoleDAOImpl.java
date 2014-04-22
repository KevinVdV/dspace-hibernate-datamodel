package org.dspace.xmlworkflow.storedcomponents.dao.impl;

import org.dspace.core.Context;
import org.dspace.dao.AbstractHibernateDAO;
import org.dspace.xmlworkflow.storedcomponents.WorkflowItemRole;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;
import org.dspace.xmlworkflow.storedcomponents.dao.WorkflowItemRoleDAO;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 21/04/14
 * Time: 11:07
 * To change this template use File | Settings | File Templates.
 */
public class WorkflowItemRoleDAOImpl extends AbstractHibernateDAO<WorkflowItemRole> implements WorkflowItemRoleDAO {
    @Override
    public WorkflowItemRole findByWorkflowItemAndRole(Context context, XmlWorkflowItem workflowItem, String role) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(WorkflowItemRoleDAO.class);
        criteria.add(Restrictions.and(
                Restrictions.eq("workflowItem", workflowItem),
                Restrictions.eq("role", role)
            )
        );

        @SuppressWarnings("unchecked")
        WorkflowItemRole result = (WorkflowItemRole) criteria.uniqueResult();
        return result;
    }

    @Override
    public List<WorkflowItemRole> findByWorkflowItem(Context context, XmlWorkflowItem workflowItem) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(WorkflowItemRoleDAO.class);
        criteria.add(Restrictions.eq("workflowItem", workflowItem));

        @SuppressWarnings("unchecked")
        List<WorkflowItemRole> result = (List<WorkflowItemRole>) criteria.list();
        return result;
    }
}