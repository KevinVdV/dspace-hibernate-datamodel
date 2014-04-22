package org.dspace.eperson.dao.impl;

import org.dspace.core.Context;
import org.dspace.dao.AbstractDSpaceObjectDao;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.dao.EPersonDAO;
import org.hibernate.Criteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 31/03/14
 * Time: 11:16
 */
public class EPersonDAOImpl extends AbstractDSpaceObjectDao<EPerson> implements EPersonDAO {

    public EPerson findByEmail(Context context, String email) throws SQLException
    {
        // All email addresses are stored as lowercase, so ensure that the email address is lowercased for the lookup
        Criteria criteria = createCriteria(context, EPerson.class);
        criteria.add(Restrictions.eq("email", email.toLowerCase()));
        return uniqueResult(criteria);
    }


    public EPerson findByNetid(Context context, String netid) throws SQLException
    {
        Criteria criteria = createCriteria(context, EPerson.class);
        criteria.add(Restrictions.eq("netid", netid));
        return uniqueResult(criteria);
    }

    public List<EPerson> search(Context context, String query, int offset, int limit) throws SQLException
    {
        String queryParam = "%"+query.toLowerCase()+"%";
        Criteria criteria = createCriteria(context, EPerson.class);
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
        return list(criteria);
    }

    public int searchResultCount(Context context, String query) throws SQLException
    {
        String queryParam = "%"+query.toLowerCase()+"%";
        Criteria criteria = createCriteria(context, EPerson.class);
        Disjunction disjunction = addSearchCriteria(queryParam);
        criteria.add(disjunction);

        return count(criteria);
    }

    public List<EPerson> findAll(Context context, String sortField) throws SQLException {
        Criteria criteria = createCriteria(context, EPerson.class);
        criteria.addOrder(Order.asc(sortField));
        return list(criteria);

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
