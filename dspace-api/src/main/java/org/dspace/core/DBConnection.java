package org.dspace.core;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 3/06/14
 * Time: 08:38
 */
public interface DBConnection<T> {

    public T getSession() throws SQLException;

    public boolean isTransActionAlive();

    public boolean isSessionAlive();

    public void rollback() throws SQLException;

    public void closeDBConnection() throws SQLException;

    public void commit() throws SQLException;
}
