package org.dspace.dao;

import org.dspace.core.Context;
import org.hibernate.Query;
import org.hibernate.criterion.Criterion;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 14/03/14
 * Time: 13:50
 */
public abstract class AbstractHibernateDAO<T> implements GenericDAO<T> {

    public T create(Context context, T t) throws SQLException {
        context.getDBConnection().save(t);
        return t;
    }

    public void save(Context context, T t) throws SQLException {
        context.getDBConnection().save(t);
    }

    public void delete(Context context, T t) throws SQLException {
        context.getDBConnection().delete(t);
    }

    public List<T> findAll(Context context, Class clazz) throws SQLException {
        @SuppressWarnings("unchecked")
        List<T> result = (List<T>) context.getDBConnection().createCriteria(clazz).list();
        return result;
    }

    public T findUnique(Context context, String query) throws SQLException {
        @SuppressWarnings("unchecked")
        T result = (T) context.getDBConnection().createQuery(query).uniqueResult();
        return result;
    }

    public T findByID(Context context, Class clazz, int id) throws SQLException {
        @SuppressWarnings("unchecked")
        T result = (T) context.getDBConnection().get(clazz, id);
        return result;
    }

    public List<T> findMany(Context context, String query) throws SQLException {
        @SuppressWarnings("unchecked")
        List<T> result = (List<T>) context.getDBConnection().createQuery(query).uniqueResult();
        return result;
    }
}
