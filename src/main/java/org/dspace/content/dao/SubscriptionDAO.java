package org.dspace.content.dao;

import org.dspace.content.Collection;
import org.dspace.content.Subscription;
import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;
import org.dspace.eperson.EPerson;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 22/04/14
 * Time: 14:19
 */
public interface SubscriptionDAO extends GenericDAO<Subscription> {

    public void deleteByCollection(Context context, Collection collection) throws SQLException;

    public List<Subscription> findByEPerson(Context context, EPerson eperson) throws SQLException;

    public Subscription findByCollectionAndEPerson(Context context, EPerson eperson, Collection collection) throws SQLException;

    public void deleteByEPerson(Context context, EPerson eperson) throws SQLException;

    public void deleteByCollectionAndEPerson(Context context, Collection collection, EPerson eperson) throws SQLException;
}
