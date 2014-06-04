package org.dspace.workflowbasic.dao.impl;

import org.dspace.core.Context;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.eperson.EPerson;
import org.dspace.workflowbasic.BasicWorkflowItem;
import org.dspace.workflowbasic.TaskListItem;
import org.dspace.workflowbasic.dao.TaskListItemDAO;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 8/04/14
 * Time: 09:11
 */
public class TaskListItemDAOImpl extends AbstractHibernateDAO<TaskListItem> implements TaskListItemDAO
{

    @Override
    public void deleteByWorkflowItem(Context context, BasicWorkflowItem workflowItem) throws SQLException {
        String queryString = "delete from TaskListItem where workflowItem = :workflowItem";
        Query query = createQuery(context, queryString);
        query.setParameter("workflowItem", workflowItem);
        query.executeUpdate();
    }

    @Override
    public List<TaskListItem> findByEPerson(Context context, EPerson ePerson) throws SQLException {
        Criteria criteria = createCriteria(context, TaskListItem.class);
        criteria.add(Restrictions.eq("ePerson", ePerson));
        return list(criteria);
    }
}
