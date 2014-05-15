package org.dspace.content.dao.impl;

import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.dao.CollectionDAO;
import org.dspace.core.Context;
import org.dspace.dao.AbstractHibernateDAO;
import org.dspace.eperson.Group;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 14/03/14
 * Time: 15:47
 */
public class CollectionDAOImpl extends AbstractHibernateDAO<Collection> implements CollectionDAO {

    /**
     * Get all collections in the system. These are alphabetically sorted by
     * collection name.
     *
     * @param context
     *            DSpace context object
     *
     * @return the collections in the system
     * @throws java.sql.SQLException
     */
    public List<Collection> findAll(Context context, String order) throws SQLException
    {
        return findAll(context, order, null, null);
    }

    public List<Collection> findAll(Context context, String order, Integer limit, Integer offset) throws SQLException {
        Criteria criteria = getCriteria(context);
        criteria.addOrder(Order.asc(order));
        if(limit != null)
        {
            criteria.setFirstResult(limit);
        }
        if(offset != null){
            criteria.setMaxResults(offset);
        }
        return list(criteria);
    }

    public Collection findByTemplateItem(Context context, Item item) throws SQLException {
        Criteria criteria = createCriteria(context, Collection.class);
        criteria.add(Restrictions.eq("template_item", item));
        return uniqueResult(criteria);
    }

    @Override
    public Collection findByGroup(Context context, Group group) throws SQLException {
        Criteria criteria = createCriteria(context, Collection.class);
        criteria.add(
                Restrictions.or(
                        Restrictions.eq("workflowStep1", group),
                        Restrictions.eq("workflowStep2", group),
                        Restrictions.eq("workflowStep3", group),
                        Restrictions.eq("submitters", group),
                        Restrictions.eq("admins", group)
                )
        );
        return uniqueResult(criteria);
    }


    protected Criteria getCriteria(Context context) throws SQLException {
        return createCriteria(context, Collection.class);
    }

}
