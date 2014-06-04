package org.dspace.core;

import org.hibernate.Session;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 3/06/14
 * Time: 08:47
 */
public class HibernateDBConnection implements DBConnection<Session> {

    @Override
    public Session getSession() throws SQLException {
        HibernateUtil.beginTransaction();
        return HibernateUtil.getSession();
    }

    @Override
    public boolean isTransActionAlive() {
        return HibernateUtil.isTransActionAlive();
    }

    @Override
    public boolean isSessionAlive() {
        return HibernateUtil.isSessionAlive();
    }

    @Override
    public void rollback() throws SQLException {
        HibernateUtil.rollbackTransaction();

    }

    @Override
    public void closeDBConnection() throws SQLException {
        HibernateUtil.closeSession();
    }

    @Override
    public void commit() throws SQLException {
        HibernateUtil.commitTransaction();
    }
}
