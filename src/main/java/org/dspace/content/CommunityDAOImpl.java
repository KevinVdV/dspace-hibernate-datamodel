package org.dspace.content;

import org.dspace.core.Context;
import org.dspace.dao.AbstractHibernateDAO;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 14/03/14
 * Time: 14:31
 */
public class CommunityDAOImpl extends AbstractHibernateDAO<Community> implements CommunityDAO {



    /**
     * Get a list of all communities in the system. These are alphabetically
     * sorted by community name.
     *
     * @param context
     *            DSpace context object
     *
     * @return the communities in the system
     */
    public List<Community> findAll(Context context, String order) throws SQLException
    {
        Criteria criteria = context.getDBConnection().createCriteria(Community.class);
        criteria.addOrder(Order.asc(order));
        @SuppressWarnings("unchecked")
        List<Community> result = criteria.list();
        return result;
    }


}
