package org.dspace.eperson;

import org.dspace.core.Context;
import org.dspace.dao.AbstractDSpaceObjectDao;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 31/03/14
 * Time: 12:03
 */
public class GroupDAOImpl extends AbstractDSpaceObjectDao<Group> implements GroupDAO {

    public Group findByName(Context context, String name) throws SQLException
    {
        Criteria criteria = context.getDBConnection().createCriteria(Group.class);
        criteria.add(Restrictions.eq("name", name));

        return (Group) criteria.list();
    }

    public List<Group> findAll(Context context, String sortColumn) throws SQLException
    {
        Criteria criteria = context.getDBConnection().createCriteria(Group.class);
        criteria.addOrder(Order.asc(sortColumn));

        @SuppressWarnings("unchecked")
        List result = criteria.list();
        return result;
    }

    public List<Group> findByEPerson(Context context, EPerson ePerson) throws SQLException {
        Query query = context.getDBConnection().createQuery("from Group where (from EPerson e where e.id = :eperson_id) in elements(epeople)");
        query.setParameter("eperson_id", ePerson.getID());
        @SuppressWarnings("unchecked")
        List result = query.list();
        return result;
    }

    public List<Group> search(Context context, String query, int offset, int limit) throws SQLException {
        String queryParam = "%"+query.toLowerCase()+"%";
        Criteria criteria = context.getDBConnection().createCriteria(Group.class);
        Disjunction disjunction = addSearchCriteria(queryParam);
        criteria.add(disjunction);

        if(0 <= offset)
        {
            criteria.setFirstResult(offset);
        }
        if(0 <= limit)
        {
            criteria.setMaxResults(limit);
        }
        @SuppressWarnings("unchecked")
        List result = criteria.list();
        return result;
    }

    public int searchResultCount(Context context, String query) throws SQLException {
        String queryParam = "%"+query.toLowerCase()+"%";
        Criteria criteria = context.getDBConnection().createCriteria(Group.class);
        Disjunction disjunction = addSearchCriteria(queryParam);
        criteria.add(disjunction);

        return ((Long) criteria.setProjection(Projections.rowCount()).uniqueResult()).intValue();
    }

    protected Disjunction addSearchCriteria(String queryParam) {
        Disjunction disjunction = Restrictions.disjunction();
        disjunction.add(Restrictions.ilike("eperson_group_id", queryParam));
        disjunction.add(Restrictions.ilike("name", queryParam));
        return disjunction;
    }
}
