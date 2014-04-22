package org.dspace.content.dao.impl;

import org.dspace.content.Collection;
import org.dspace.content.Subscription;
import org.dspace.content.dao.SubscriptionDAO;
import org.dspace.core.Context;
import org.dspace.dao.AbstractHibernateDAO;
import org.hibernate.Query;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 22/04/14
 * Time: 14:24
 */
public class SubscriptionDAOImpl extends AbstractHibernateDAO<Subscription> implements SubscriptionDAO {

    @Override
    public void deleteByCollection(Context context, Collection collection) throws SQLException {
        String hqlQuery = "delete from Subscription where collection=:collection";
        Query query = createQuery(context, hqlQuery);
        query.setParameter("collection", collection);
        query.executeUpdate();
    }
}
