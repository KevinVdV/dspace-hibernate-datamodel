package org.dspace.dao;

import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 14/03/14
 * Time: 13:45
 */
public interface GenericDAO<T>
{
    public T create(Context context, T t) throws SQLException;

    public void save(Context context, T t) throws SQLException;

    public void delete(Context context, T t) throws SQLException;

    public List<T> findAll(Context context, Class<T> clazz) throws SQLException;

    public T findUnique(Context context, String query) throws SQLException;

    public T findByID(Context context, Class clazz, int id) throws SQLException;

    public List<T> findMany(Context context, String query) throws SQLException;

}