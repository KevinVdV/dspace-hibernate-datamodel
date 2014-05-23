package org.dspace.content.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Subscription;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 22/04/14
 * Time: 14:11
 */
public interface SubscriptionService {

    public Subscription create(Context context, Collection collection, EPerson ePerson) throws SQLException, AuthorizeException;

    public void deleteByCollection(Context context, Collection collection) throws SQLException, AuthorizeException;

    public void delete(Context context, Subscription subscription) throws SQLException, AuthorizeException;

    public void delete(Context context, EPerson person) throws SQLException, AuthorizeException;
}
