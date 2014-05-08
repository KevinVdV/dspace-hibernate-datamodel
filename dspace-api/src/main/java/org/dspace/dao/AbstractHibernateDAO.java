package org.dspace.dao;

import org.dspace.core.Context;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;

import java.sql.SQLException;
import java.util.Iterator;
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
        return list(createCriteria(context, clazz));
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

    public Criteria createCriteria(Context context, Class<T> persistentClass) throws SQLException {
        return context.getDBConnection().createCriteria(persistentClass);
    }

    //TODO: MAKE SURE EVERYBODY USES THIS METHOD !
    public Query createQuery(Context context, String query) throws SQLException {
        return context.getDBConnection().createQuery(query);
    }

    public List<T> list(Criteria criteria)
    {
        @SuppressWarnings("unchecked")
        List<T> result = (List<T>) criteria.list();
        return result;
    }

    public List<T> list(Query query)
    {
        @SuppressWarnings("unchecked")
        List<T> result = (List<T>) query.list();
        return result;
    }

    public T uniqueResult(Criteria criteria)
    {
        @SuppressWarnings("unchecked")
        T result = (T) criteria.uniqueResult();
        return result;
    }

    public Iterator<T> iterate(Query query)
    {
        @SuppressWarnings("unchecked")
        Iterator<T> result = (Iterator<T>) query.iterate();
        return result;
    }

    public int count(Criteria criteria)
    {
        return ((Long) criteria.setProjection(Projections.rowCount()).uniqueResult()).intValue();
    }
}
