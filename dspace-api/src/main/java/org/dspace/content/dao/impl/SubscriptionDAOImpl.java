package org.dspace.content.dao.impl;

import org.dspace.content.Collection;
import org.dspace.content.Subscription;
import org.dspace.content.dao.SubscriptionDAO;
import org.dspace.core.Context;
import org.dspace.dao.AbstractHibernateDAO;
import org.dspace.eperson.EPerson;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 22/04/14
 * Time: 14:24
 */
public class SubscriptionDAOImpl extends AbstractHibernateDAO<Subscription> implements SubscriptionDAO {

    @Override
    public List<Subscription> findByEPerson(Context context, EPerson eperson) throws SQLException {
        Criteria criteria = createCriteria(context, Subscription.class);
        criteria.add(
                Restrictions.and(
                        Restrictions.eq("ePerson", eperson)
                )
        );
        return list(criteria);

    }

    @Override
    public Subscription findByCollectionAndEPerson(Context context, EPerson eperson, Collection collection) throws SQLException {
        Criteria criteria = createCriteria(context, Subscription.class);
        criteria.add(
                Restrictions.and(
                        Restrictions.eq("ePerson", eperson),
                        Restrictions.eq("collection", collection)
                )
        );
        return uniqueResult(criteria);
    }


    @Override
    public void deleteByCollection(Context context, Collection collection) throws SQLException {
        String hqlQuery = "delete from Subscription where collection=:collection";
        Query query = createQuery(context, hqlQuery);
        query.setParameter("collection", collection);
        query.executeUpdate();
    }

    @Override
    public void deleteByEPerson(Context context, EPerson eperson) throws SQLException {
        String hqlQuery = "delete from Subscription where ePerson=:ePerson";
        Query query = createQuery(context, hqlQuery);
        query.setParameter("ePerson", eperson);
        query.executeUpdate();
    }

    @Override
    public void deleteByCollectionAndEPerson(Context context, Collection collection, EPerson eperson) throws SQLException {
        String hqlQuery = "delete from Subscription where collection=:collection AND ePerson=:ePerson";
        Query query = createQuery(context, hqlQuery);
        query.setParameter("collection", collection);
        query.setParameter("ePerson", eperson);
        query.executeUpdate();
    }
}
