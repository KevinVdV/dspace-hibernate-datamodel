package org.dspace.content;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.dao.SubscriptionDAO;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.SubscriptionService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.eperson.EPerson;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 22/04/14
 * Time: 14:20
 */
public class SubscriptionServiceImpl implements SubscriptionService {

    /**
     * log4j logger
     */
    private static Logger log = Logger.getLogger(SubscriptionServiceImpl.class);

    @Autowired(required = true)
    protected SubscriptionDAO subscriptionDAO;

    @Autowired(required = true)
    protected CollectionService collectionService;

    @Override
    public Subscription create(Context context, Collection collection, EPerson ePerson) throws SQLException, AuthorizeException {
        if (AuthorizeManager.isAdmin(context)
                || ((context.getCurrentUser() != null) && (context
                .getCurrentUser().getID() == ePerson.getID()))) {
            if (isSubscribed(context, ePerson, collection)) {
                return findByCollectionAndEPerson(context, ePerson, collection);
            } else {
                Subscription subscription = subscriptionDAO.create(context, new Subscription());
                subscription.setCollection(collection);
                subscription.setePerson(ePerson);
                return subscription;
            }
        } else {
            throw new AuthorizeException(
                    "Only admin or e-person themselves can subscribe");
        }

    }

    /**
     * Unsubscribe an e-person to a collection. Passing in <code>null</code>
     * for the collection unsubscribes the e-person from all collections they
     * are subscribed to.
     *
     * @param context    DSpace context
     * @param eperson    EPerson to unsubscribe
     * @param collection Collection to unsubscribe from
     */
    protected void unsubscribe(Context context, EPerson eperson,
                            Collection collection) throws SQLException, AuthorizeException {
        // Check authorisation. Must be administrator, or the eperson.
        if (AuthorizeManager.isAdmin(context)
                || ((context.getCurrentUser() != null) && (context
                .getCurrentUser().getID() == eperson.getID()))) {
            if (collection == null) {
                // Unsubscribe from all
                subscriptionDAO.deleteByEPerson(context, eperson);

            } else {
                subscriptionDAO.deleteByCollectionAndEPerson(context, collection, eperson);

                log.info(LogManager.getHeader(context, "unsubscribe",
                        "eperson_id=" + eperson.getID() + ",collection_id="
                                + collection.getID()
                ));
            }
        } else {
            throw new AuthorizeException(
                    "Only admin or e-person themselves can unsubscribe");
        }
    }

    /**
     * Find out which collections an e-person is subscribed to
     *
     * @param context DSpace context
     * @param eperson EPerson
     * @return array of collections e-person is subscribed to
     */
    protected List<Subscription> getSubscriptions(Context context, EPerson eperson)
            throws SQLException {

        return subscriptionDAO.findByEPerson(context, eperson);
    }

    /**
     * Find out which collections the currently logged in e-person can subscribe to
     *
     * @param context DSpace context
     * @return array of collections the currently logged in e-person can subscribe to
     */
    protected List<Collection> getAvailableSubscriptions(Context context)
            throws SQLException {
        return getAvailableSubscriptions(context, null);
    }

    /**
     * Find out which collections an e-person can subscribe to
     *
     * @param context DSpace context
     * @param eperson EPerson
     * @return array of collections e-person can subscribe to
     */
    protected List<Collection> getAvailableSubscriptions(Context context, EPerson eperson)
            throws SQLException {

        if (eperson != null) {
            context.setCurrentUser(eperson);
        }
        return collectionService.findAuthorized(context, null, Constants.ADD);
    }

    /**
     * Is that e-person subscribed to that collection?
     *
     * @param context    DSpace context
     * @param eperson    find out if this e-person is subscribed
     * @param collection find out if subscribed to this collection
     * @return <code>true</code> if they are subscribed
     */
    protected boolean isSubscribed(Context context, EPerson eperson,
                                Collection collection) throws SQLException {
        return findByCollectionAndEPerson(context, eperson, collection) != null;
    }

    protected Subscription findByCollectionAndEPerson(Context context, EPerson eperson, Collection collection) throws SQLException {
        return subscriptionDAO.findByCollectionAndEPerson(context, eperson, collection);
    }


    @Override
    public void deleteByCollection(Context context, Collection collection) throws SQLException, AuthorizeException {
        if (AuthorizeManager.isAdmin(context)) {
            subscriptionDAO.deleteByCollection(context, collection);
        } else {
            throw new AuthorizeException("Only admin can delete subscriptions for a collection");
        }

    }

    @Override
    public void delete(Context context, Subscription subscription) throws SQLException, AuthorizeException {
        if (AuthorizeManager.isAdmin(context)
                || ((context.getCurrentUser() != null) && (context
                .getCurrentUser().getID() == subscription.getePerson().getID()))) {
            subscriptionDAO.delete(context, subscription);
        } else {
            throw new AuthorizeException("Only admin or e-person themselves can subscribe");
        }
    }
}
