package org.dspace.content;

import org.dspace.core.Context;
import org.dspace.dao.AbstractHibernateDAO;
import org.dspace.eperson.EPerson;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 31/03/14
 * Time: 10:47
 */
public class WorkspaceItemDAOImpl extends AbstractHibernateDAO<WorkspaceItem> implements WorkspaceItemDAO {


    public List<WorkspaceItem> findByEPerson(Context context, EPerson ep) throws SQLException
    {
        Query query = context.getDBConnection().createQuery("from WorkspaceItem ws where ws.item.submitter = :submitter order by workspaceItemId");
        query.setParameter("submitter", ep);
        @SuppressWarnings("unchecked")
        List<WorkspaceItem> result = query.list();
        return result;
    }

    public List<WorkspaceItem> findByCollection(Context context, Collection c) throws SQLException
    {
        Criteria criteria = context.getDBConnection().createCriteria(WorkspaceItem.class);
        criteria.add(Restrictions.eq("collection_id", c.getID()));
        @SuppressWarnings("unchecked")
        List<WorkspaceItem> result = criteria.list();
        return result;
    }

    public WorkspaceItem findByItem(Context context, Item i) throws SQLException
    {
        Criteria criteria = context.getDBConnection().createCriteria(WorkspaceItem.class);
        criteria.add(Restrictions.eq("item_id", i.getID()));
        // Look for the unique workspaceitem entry where 'item_id' references this item
        return (WorkspaceItem) criteria.uniqueResult();
    }

    public List<WorkspaceItem> findAll(Context context) throws SQLException
    {
        Criteria criteria = context.getDBConnection().createCriteria(WorkspaceItem.class);
        criteria.addOrder(Order.asc("item_id"));
        @SuppressWarnings("unchecked")
        List<WorkspaceItem> result = criteria.list();
        return result;
    }

}
