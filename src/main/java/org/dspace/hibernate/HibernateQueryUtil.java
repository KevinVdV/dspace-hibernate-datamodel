package org.dspace.hibernate;

import org.dspace.core.Context;
import org.dspace.eperson.EPersonEntity;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by kevin on 08/02/14.
 */
public class HibernateQueryUtil {

    public static Object findByUnique(Context context, String tableName, String column, Object value) throws SQLException {
        Query query = context.getDBConnection().createQuery("from " + tableName + " where " + column + " = :" + column);
        query.setParameter(column, value);
        query.setCacheable(true);
        return query.uniqueResult();
    }

    public static void update(Context context, Object object) throws SQLException {
        context.getDBConnection().save(object);
    }

    public static void delete(Context context, Object object) throws SQLException {
        context.getDBConnection().delete(object);
    }

    public static List<Object> searchQuery(Context context, Class resultClass, Map<String, String> parameters, Map<String, String> order, int offset, int limit) throws SQLException {
        Criteria criteria = getSearchCriteria(context, resultClass, parameters, order);
        criteria.setFirstResult(offset);
        criteria.setMaxResults(limit);
        return criteria.list();
    }

    public static Integer searchQueryCount(Context context, Class resultClass, Map<String, String> parameters, Map<String, String> order) throws SQLException {
        Criteria criteria = getSearchCriteria(context, resultClass, parameters, order);
        return ((Long) criteria.setProjection(Projections.rowCount()).uniqueResult()).intValue();
    }

    public static List<Object> getAll(Context context, Class object, Map<String, String> sort) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(object);
        addOrder(sort, criteria);
        return criteria.list();
    }

    private static Criteria getSearchCriteria(Context context, Class resultClass, Map<String, String> parameters, Map<String, String> order) throws SQLException {
        Session session = context.getDBConnection();
        Criteria criteria = session.createCriteria(resultClass.getClass());
        Disjunction disjunction = Restrictions.disjunction();
        for(String column : parameters.keySet()){
            String value = parameters.get(column);
            disjunction.add(Restrictions.ilike(column, value));
        }
        criteria.add(disjunction);
        addOrder(order, criteria);
        return criteria;
    }

    protected static void addOrder(Map<String, String> order, Criteria criteria) {
        if(order != null)
        {
            for(String column : order.keySet())
            {
                if("asc".equals(order.get(column))){
                    criteria.addOrder(Order.asc(column));
                }else{
                    criteria.addOrder(Order.desc(column));
                }
            }
        }
    }
}
