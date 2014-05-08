package org.dspace.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.sql.SQLException;

/**
 * Created by kevin on 08/02/14.
 */
public class HibernateUtil {
    private static final SessionFactory sessionFactory;
    private static final ThreadLocal<Session> threadSession =
            new ThreadLocal<Session>();
    private static final ThreadLocal<Transaction> threadTransaction =
            new ThreadLocal<Transaction>();

    static {
        // Initialize SessionFactory...
        Configuration configuration = new Configuration();
        configuration.configure();
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        sessionFactory = configuration.buildSessionFactory(serviceRegistry);
    }

    public static Session getSession() throws SQLException {
        Session s = threadSession.get();
        // Open a new Session, if this thread has none yet
        try {
            if (s == null) {
                s = sessionFactory.openSession();
                threadSession.set(s);
            }
        } catch (HibernateException ex) {
            throw new SQLException(ex);
        }
        return s;
    }

    public static boolean isSessionAlive(){
        return threadSession.get() != null;
    }

    public static void closeSession() throws SQLException {
        try {
            Session s = threadSession.get();
            threadSession.set(null);
            if (s != null && s.isOpen()){
                s.close();
            }
        } catch (HibernateException ex) {
            throw new SQLException(ex);
        }
    }

    public static void beginTransaction() throws SQLException {
        Transaction tx = threadTransaction.get();
        try {
            if (tx == null) {
                tx = getSession().beginTransaction();
                threadTransaction.set(tx);
            }
        } catch (HibernateException ex) {
            throw new SQLException(ex);
        }
    }

    public static void commitTransaction() throws SQLException {
        Transaction tx = threadTransaction.get();
        try {
            if (tx != null && !tx.wasCommitted()
                    && !tx.wasRolledBack())
                tx.commit();
            threadTransaction.set(null);
        } catch (HibernateException ex) {
            rollbackTransaction();
            throw new SQLException(ex);
        }
    }

    public static void rollbackTransaction() throws SQLException {

        Transaction tx = threadTransaction.get();
        try {
            threadTransaction.set(null);
            if (tx != null && !tx.wasCommitted()
                    && !tx.wasRolledBack()) {
                tx.rollback();
            }
        } catch (HibernateException ex) {
            throw new SQLException(ex);
        } finally {
            closeSession();
        }
    }

    public static boolean isTransActionAlive(){
        Transaction tx = threadTransaction.get();
        return tx != null && tx.isActive();
    }

}

