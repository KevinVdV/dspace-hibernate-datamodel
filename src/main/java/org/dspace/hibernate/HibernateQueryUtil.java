package org.dspace.hibernate;

import org.dspace.core.Context;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;

import javax.persistence.Table;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by kevin on 08/02/14.
 */
public class HibernateQueryUtil {

    public static <T> T findByUnique(Context context, Class<? extends T> clazz, String column, Object value) throws SQLException {
        Query query = context.getDBConnection().createQuery("from " + clazz.getSimpleName() + " where " + column + " = :" + column);
        query.setParameter(column, value);
        query.setCacheable(true);
        return (T) query.uniqueResult();
    }

    public static void update(Context context, Object object) throws SQLException {
        context.getDBConnection().save(object);
    }

    public static void refresh(Context context, Object object) throws SQLException {
        context.getDBConnection().refresh(object);
    }

    public static void delete(Context context, Object object) throws SQLException {
        //Refresh before deleting to avoid issues where our object is out of date
        //TODO: IS THIS OK ? CAUSES ISSUES WHEN OBJECTS NOT WRITTEN TO DB
//        context.getDBConnection().refresh(object);
        context.getDBConnection().delete(object);
    }

    public static<T> List<T> searchQuery(Context context, Class<T> resultClass, Map<String, String> parameters, Map<String, String> order, int offset, int limit) throws SQLException {
        Criteria criteria = getSearchCriteria(context, resultClass, parameters, order);
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

    public static<T> List<T> searchQuery(Context context, Class<T> resultClass, Map<String, String> parameters, Map<String, String> order) throws SQLException {
        Criteria criteria = getSearchCriteria(context, resultClass, parameters, order);
        return criteria.list();
    }

    public static Integer searchQueryCount(Context context, Class resultClass, Map<String, String> parameters) throws SQLException {
        Criteria criteria = getSearchCriteria(context, resultClass, parameters, null);
        return ((Long) criteria.setProjection(Projections.rowCount()).uniqueResult()).intValue();
    }

    public static<T> List<T> getAll(Context context, Class<T> object, Map<String, String> sort) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(object);
        addOrder(sort, criteria);
        return criteria.list();
    }

    private static Criteria getSearchCriteria(Context context, Class resultClass, Map<String, String> parameters, Map<String, String> order) throws SQLException {
        Session session = context.getDBConnection();
        Criteria criteria = session.createCriteria(resultClass);
        Disjunction disjunction = Restrictions.disjunction();
        if(parameters != null)
        {
            for(String column : parameters.keySet()){
                String value = parameters.get(column);
                disjunction.add(Restrictions.ilike(column, value));
            }
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
