package org.dspace.eperson.dao.impl;

import org.apache.commons.lang.math.NumberUtils;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.eperson.dao.GroupDAO;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 31/03/14
 * Time: 12:03
 */
public class GroupDAOImpl extends AbstractHibernateDAO<Group> implements GroupDAO {

    @Override
    public Group findByName(Context context, String name) throws SQLException
    {
        Criteria criteria = createCriteria(context, Group.class);
        criteria.add(Restrictions.eq("name", name));

        return uniqueResult(criteria);
    }

    @Override
    public List<Group> findAll(Context context, String sortColumn) throws SQLException
    {
        Criteria criteria = createCriteria(context, Group.class);
        criteria.addOrder(Order.asc(sortColumn));

        return list(criteria);
    }

    @Override
    public List<Group> findByEPerson(Context context, EPerson ePerson) throws SQLException {
        Query query = createQuery(context, "from Group where (from EPerson e where e.id = :eperson_id) in elements(epeople)");
        query.setParameter("eperson_id", ePerson.getID());
        return list(query);
    }

    @Override
    public List<Group> search(Context context, String query, int offset, int limit) throws SQLException {
        Criteria criteria = createCriteria(context, Group.class);
        Disjunction disjunction = addSearchCriteria(query);
        criteria.add(disjunction);

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
    public int searchResultCount(Context context, String query) throws SQLException {
        Criteria criteria = createCriteria(context, Group.class);
        Disjunction disjunction = addSearchCriteria(query);
        criteria.add(disjunction);

        return count(criteria);
    }

    protected Disjunction addSearchCriteria(String queryParam) {
        Disjunction disjunction = Restrictions.disjunction();
        //Check if our query parameter is an identifier, if not do not attempt to search.
        //Hibernate will throw a ClassCastException when this param is added & is not an int.
        if(NumberUtils.toInt(queryParam, -1) != -1)
        {
            disjunction.add(Restrictions.like("id", NumberUtils.toInt(queryParam)));
        }
        disjunction.add(Restrictions.like("name", "%" + queryParam + "%"));
        return disjunction;
    }
}
