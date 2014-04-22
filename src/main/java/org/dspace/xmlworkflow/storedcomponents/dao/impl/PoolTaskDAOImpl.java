package org.dspace.xmlworkflow.storedcomponents.dao.impl;

import org.dspace.core.Context;
import org.dspace.dao.AbstractHibernateDAO;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.xmlworkflow.storedcomponents.PoolTask;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;
import org.dspace.xmlworkflow.storedcomponents.dao.PoolTaskDAO;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 21/04/14
 * Time: 16:01
 * To change this template use File | Settings | File Templates.
 */
public class PoolTaskDAOImpl extends AbstractHibernateDAO<PoolTask> implements PoolTaskDAO {
    @Override
    public List<PoolTask> findByEPerson(Context context, EPerson ePerson) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(PoolTask.class);
        criteria.add(Restrictions.eq("ePerson", ePerson));

        @SuppressWarnings("unchecked")
        List<PoolTask> result = (List<PoolTask>) criteria.list();
        return result;
    }

    @Override
    public List<PoolTask> findByGroup(Context context, Group group) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(PoolTask.class);
        criteria.add(Restrictions.eq("group", group));

        @SuppressWarnings("unchecked")
        List<PoolTask> result = (List<PoolTask>) criteria.list();
        return result;
    }

    @Override
    public List<PoolTask> findByWorkflowItem(Context context, XmlWorkflowItem workflowItem) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(PoolTask.class);
        criteria.add(Restrictions.eq("workflowItem", workflowItem));

        @SuppressWarnings("unchecked")
        List<PoolTask> result = (List<PoolTask>) criteria.list();
        return result;
    }

    @Override
    public PoolTask findByWorkflowItemAndEPerson(Context context, XmlWorkflowItem workflowItem, EPerson ePerson) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(PoolTask.class);
        criteria.add(
                Restrictions.and(
                        Restrictions.eq("workflowItem", workflowItem),
                        Restrictions.eq("ePerson", ePerson)
                )
        );

        @SuppressWarnings("unchecked")
        PoolTask result = (PoolTask) criteria.uniqueResult();
        return result;
    }

    @Override
    public PoolTask findByWorkflowItemAndGroup(Context context, Group group, XmlWorkflowItem workflowItem) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(PoolTask.class);
        criteria.add(
                Restrictions.and(
                        Restrictions.eq("workflowItem", workflowItem),
                        Restrictions.eq("group", group)
                )
        );

        @SuppressWarnings("unchecked")
        PoolTask result = (PoolTask) criteria.uniqueResult();
        return result;
    }
}
