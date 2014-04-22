package org.dspace.content;

import org.dspace.content.dao.SubscriptionDAO;
import org.dspace.content.service.SubscriptionService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 22/04/14
 * Time: 14:20
 */
public class SubscriptionServiceImpl implements SubscriptionService {

    @Autowired(required = true)
    protected SubscriptionDAO subscriptionDAO;

    @Override
    public Subscription create(Context context, Collection collection, EPerson ePerson) throws SQLException {
        Subscription subscription = subscriptionDAO.create(context, new Subscription());
        subscription.setCollection(collection);
        subscription.setePerson(ePerson);
        return subscription;

    }

    @Override
    public void deleteByCollection(Context context, Collection collection) throws SQLException {
        subscriptionDAO.deleteByCollection(context,  collection);

    }

    @Override
    public void delete(Context context, Subscription subscription) throws SQLException {
        subscriptionDAO.delete(context, subscription);
    }
}
