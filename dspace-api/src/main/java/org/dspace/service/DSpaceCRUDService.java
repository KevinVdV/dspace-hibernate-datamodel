package org.dspace.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;

import java.sql.SQLException;

/**
 * Interface containing the simple CRUD methods so we don't have to add them over and again to every service which
 * requires these methods
 *
 * User: kevin (kevin at atmire.com)
 * Date: 22/04/14
 * Time: 11:58
 */
public interface DSpaceCRUDService<T> {

    public T create(Context context) throws SQLException, AuthorizeException;

    public T find(Context context, int id) throws SQLException;

    public void update(Context context, T t) throws SQLException, AuthorizeException;

    public void delete(Context context, T t) throws SQLException, AuthorizeException;
}
