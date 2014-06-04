package org.dspace.xmlworkflow.storedcomponents.dao.impl;

import org.dspace.core.Context;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.eperson.EPerson;
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
    public List<WorkflowItemRole> findByWorkflowItemAndRole(Context context, XmlWorkflowItem workflowItem, String role) throws SQLException {
        Criteria criteria = createCriteria(context, WorkflowItemRole.class);
        criteria.add(Restrictions.and(
                Restrictions.eq("workflowItem", workflowItem),
                Restrictions.eq("role", role)
            )
        );

        return list(criteria);
    }

    @Override
    public List<WorkflowItemRole> findByWorkflowItem(Context context, XmlWorkflowItem workflowItem) throws SQLException {
        Criteria criteria = createCriteria(context, WorkflowItemRole.class);
        criteria.add(Restrictions.eq("workflowItem", workflowItem));

        return list(criteria);
    }

    @Override
    public List<WorkflowItemRole> findByEPerson(Context context, EPerson ePerson) throws SQLException {
        Criteria criteria = createCriteria(context, WorkflowItemRole.class);
        criteria.add(Restrictions.eq("ePerson", ePerson));

        return list(criteria);
    }
}
