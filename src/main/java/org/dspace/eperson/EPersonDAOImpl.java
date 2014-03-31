package org.dspace.eperson;

import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.dao.AbstractDSpaceObjectDao;
import org.dspace.dao.AbstractHibernateDAO;
import org.dspace.hibernate.HibernateQueryUtil;
import org.hibernate.Criteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 31/03/14
 * Time: 11:16
 */
public class EPersonDAOImpl extends AbstractDSpaceObjectDao<EPerson> implements EPersonDAO {

    public EPerson findByEmail(Context context, String email) throws SQLException
    {
        // All email addresses are stored as lowercase, so ensure that the email address is lowercased for the lookup
        Criteria criteria = context.getDBConnection().createCriteria(EPerson.class);
        criteria.add(Restrictions.eq("email", email.toLowerCase()));
        return (EPerson) criteria.uniqueResult();
    }


    public EPerson findByNetid(Context context, String netid) throws SQLException
    {
        Criteria criteria = context.getDBConnection().createCriteria(EPerson.class);
        criteria.add(Restrictions.eq("netid", netid));
        return (EPerson) criteria.uniqueResult();
    }

    public List<EPerson> search(Context context, String query, int offset, int limit) throws SQLException
    {
        String queryParam = "%"+query.toLowerCase()+"%";
        Criteria criteria = context.getDBConnection().createCriteria(EPerson.class);
        Disjunction disjunction = addSearchCriteria(queryParam);
        criteria.add(disjunction);
        criteria.addOrder(Order.asc("lastname"));
        criteria.addOrder(Order.asc("firstname"));

        if(0 <= offset)
        {
            criteria.setFirstResult(offset);
        }
        if(0 <= limit)
        {
            criteria.setMaxResults(limit);
        }
        return criteria.list();
    }

    public int searchResultCount(Context context, String query) throws SQLException
    {
        String queryParam = "%"+query.toLowerCase()+"%";
        Criteria criteria = context.getDBConnection().createCriteria(EPerson.class);
        Disjunction disjunction = addSearchCriteria(queryParam);
        criteria.add(disjunction);

        return ((Long) criteria.setProjection(Projections.rowCount()).uniqueResult()).intValue();
    }

    public List<EPerson> findAll(Context context, String sortField) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(EPerson.class);
        criteria.addOrder(Order.asc(sortField));
        List result = criteria.list();
        return result;

    }


    protected Disjunction addSearchCriteria(String queryParam) {
        Disjunction disjunction = Restrictions.disjunction();
        disjunction.add(Restrictions.ilike("eperson_id", queryParam));
        disjunction.add(Restrictions.ilike("firstname", queryParam));
        disjunction.add(Restrictions.ilike("lastname", queryParam));
        disjunction.add(Restrictions.ilike("email", queryParam));
        return disjunction;
    }
}
