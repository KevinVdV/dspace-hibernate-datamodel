package org.dspace.eperson.dao.impl;

import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.eperson.dao.EPersonDAO;
import org.hibernate.Criteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 31/03/14
 * Time: 11:16
 */
public class EPersonDAOImpl extends AbstractHibernateDAO<EPerson> implements EPersonDAO {

    @Override
    public EPerson findByEmail(Context context, String email) throws SQLException
    {
        // All email addresses are stored as lowercase, so ensure that the email address is lowercased for the lookup
        Criteria criteria = createCriteria(context, EPerson.class);
        criteria.add(Restrictions.eq("email", email.toLowerCase()));
        return uniqueResult(criteria);
    }


    @Override
    public EPerson findByNetid(Context context, String netid) throws SQLException
    {
        Criteria criteria = createCriteria(context, EPerson.class);
        criteria.add(Restrictions.eq("netid", netid));
        return uniqueResult(criteria);
    }

    @Override
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

    @Override
    public int searchResultCount(Context context, String query) throws SQLException
    {
        String queryParam = "%"+query.toLowerCase()+"%";
        Criteria criteria = createCriteria(context, EPerson.class);
        Disjunction disjunction = addSearchCriteria(queryParam);
        criteria.add(disjunction);

        return count(criteria);
    }

    @Override
    public List<EPerson> findAll(Context context, String sortField) throws SQLException {
        Criteria criteria = createCriteria(context, EPerson.class);
        criteria.addOrder(Order.asc(sortField));
        return list(criteria);

    }

    @Override
    public List<EPerson> findByGroups(Context context, Set<Group> groups) throws SQLException {
        Criteria criteria = createCriteria(context, EPerson.class);
        criteria.createAlias("groups", "g");
        Disjunction orRestriction = Restrictions.or();
        for(Group group : groups)
        {
            orRestriction.add(Restrictions.eq("g.id", group.getID()));
        }
        criteria.add(orRestriction);
        return list(criteria);
    }


    protected Disjunction addSearchCriteria(String queryParam) {
        Disjunction disjunction = Restrictions.disjunction();
        disjunction.add(Restrictions.like("eperson_id", queryParam));
        disjunction.add(Restrictions.like("firstname", queryParam));
        disjunction.add(Restrictions.like("lastname", queryParam));
        disjunction.add(Restrictions.like("email", queryParam));
        return disjunction;
    }
}
