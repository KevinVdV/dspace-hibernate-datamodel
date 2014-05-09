package org.dspace.content.dao.impl;

import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.WorkspaceItem;
import org.dspace.content.dao.WorkspaceItemDAO;
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
        Query query = createQuery(context, "from WorkspaceItem ws where ws.item.submitter = :submitter order by workspaceItemId");
        query.setParameter("submitter", ep);
        return list(query);
    }

    public List<WorkspaceItem> findByCollection(Context context, Collection c) throws SQLException
    {
        Criteria criteria = createCriteria(context, WorkspaceItem.class);
        criteria.add(Restrictions.eq("collection", c));
        return list(criteria);
    }

    public WorkspaceItem findByItem(Context context, Item i) throws SQLException
    {
        Criteria criteria = createCriteria(context, WorkspaceItem.class);
        criteria.add(Restrictions.eq("item", i));
        // Look for the unique workspaceitem entry where 'item_id' references this item
        return uniqueResult(criteria);
    }

    public List<WorkspaceItem> findAll(Context context) throws SQLException
    {
        Criteria criteria = createCriteria(context, WorkspaceItem.class);
        criteria.addOrder(Order.asc("item"));
        return list(criteria);
    }

}
