package org.dspace.workflow;

import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.dao.AbstractHibernateDAO;
import org.dspace.eperson.EPerson;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 7/04/14
 * Time: 16:10
 */
public class WorkflowItemDAOImpl extends AbstractHibernateDAO<WorkflowItem> implements WorkflowItemDAO {


    public WorkflowItem findByItem(Context context, Item i) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(WorkflowItem.class);
        criteria.add(Restrictions.eq("item", i));
        // Look for the unique WorkflowItem entry where 'item_id' references this item
        return (WorkflowItem) criteria.uniqueResult();
    }

    public List<WorkflowItem> findByEPerson(Context context, EPerson ep) throws SQLException
    {
        Criteria criteria = context.getDBConnection().createCriteria(WorkflowItem.class);
        criteria.add(Restrictions.eq("item.submitter", ep));
        @SuppressWarnings("unchecked")
        List<WorkflowItem> result = criteria.list();
        return result;

    }

    public List<WorkflowItem> findByCollection(Context context, Collection c) throws SQLException
    {
        Criteria criteria = context.getDBConnection().createCriteria(WorkflowItem.class);
        criteria.add(Restrictions.eq("collection", c));
        @SuppressWarnings("unchecked")
        List<WorkflowItem> result = criteria.list();
        return result;
    }

    public List<WorkflowItem> findByPooledTasks(Context context, EPerson ePerson) throws SQLException
    {
        String queryString = "select WorkflowItem from WorkflowItem as wf join TaskListItem.eperson tli where tli.eperson = :eperson";
        Query query = context.getDBConnection().createQuery(queryString);
        query.setParameter("eperson", ePerson);
        @SuppressWarnings("unchecked")
        List<WorkflowItem> result = query.list();
        return result;
    }


}
