package org.dspace.workflowbasic.dao.impl;

import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.eperson.EPerson;
import org.dspace.workflowbasic.BasicWorkflowItem;
import org.dspace.workflowbasic.dao.BasicWorkflowItemDAO;
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
public class BasicWorkflowItemDAOImpl extends AbstractHibernateDAO<BasicWorkflowItem> implements BasicWorkflowItemDAO {


    @Override
    public BasicWorkflowItem findByItem(Context context, Item i) throws SQLException {
        Criteria criteria = createCriteria(context, BasicWorkflowItem.class);
        criteria.add(Restrictions.eq("item", i));
        // Look for the unique WorkflowItem entry where 'item_id' references this item
        return uniqueResult(criteria);
    }

    @Override
    public List<BasicWorkflowItem> findBySubmitter(Context context, EPerson ep) throws SQLException
    {
        Criteria criteria = createCriteria(context, BasicWorkflowItem.class);
        criteria.add(Restrictions.eq("item.submitter", ep));
        return list(criteria);

    }

    @Override
    public List<BasicWorkflowItem> findByCollection(Context context, Collection c) throws SQLException
    {
        Criteria criteria = createCriteria(context, BasicWorkflowItem.class);
        criteria.add(Restrictions.eq("collection", c));
        return list(criteria);
    }

    @Override
    public List<BasicWorkflowItem> findByPooledTasks(Context context, EPerson ePerson) throws SQLException
    {
        String queryString = "select BasicWorkflowItem from BasicWorkflowItem as wf join TaskListItem.eperson tli where tli.eperson = :eperson";
        Query query = createQuery(context, queryString);
        query.setParameter("eperson", ePerson);
        return list(query);
    }

    @Override
    public List<BasicWorkflowItem> findByOwner(Context context, EPerson ePerson) throws SQLException {
        Criteria criteria = createCriteria(context, BasicWorkflowItem.class);
        criteria.add(Restrictions.eq("owner", ePerson));
        return list(criteria);
    }
}
