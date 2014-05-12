package org.dspace.content.dao.impl;

import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.content.dao.ItemDAO;
import org.dspace.core.Context;
import org.dspace.dao.AbstractHibernateDAO;
import org.dspace.eperson.EPerson;
import org.hibernate.Query;

import java.sql.SQLException;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 16/03/14
 * Time: 09:12
 * To change this template use File | Settings | File Templates.
 */
public class ItemDAOImpl extends AbstractHibernateDAO<Item> implements ItemDAO {

    public Iterator<Item> findAll(Context context, boolean archived) throws SQLException
    {
        Query query = createQuery(context, "FROM Item WHERE inArchive= :in_archive");
        query.setParameter("in_archive", archived);
        return iterate(query);
    }

    public Iterator<Item> findAll(Context context, boolean archived, boolean withdrawn) throws SQLException
    {
        Query query = createQuery(context, "FROM Item WHERE inArchive= :in_archive or withdrawn = :withdrawn");
        query.setParameter("in_archive", archived);
        query.setParameter("withdrawn", withdrawn);
        return iterate(query);
    }

    public Iterator<Item> findBySubmitter(Context context, EPerson eperson) throws SQLException {
        Query query = createQuery(context, "FROM Item WHERE inArchive= :in_archive and submitter= :submitter");
        query.setParameter("in_archive", true);
        query.setParameter("submitter", eperson);
        return iterate(query);
    }

    public Iterator<Item> findByMetadataField(Context context, MetadataField metadataField, String value, boolean inArchive) throws SQLException {
        String hqlQueryString = "select item from Item as item join item.metadata metadataValue WHERE item.inArchive=:in_archive AND metadataValue.metadataField = :metadata_field";
        if(value != null)
        {
            hqlQueryString += " AND metadataValue.value = :text_value";
        }
        Query query = createQuery(context, hqlQueryString);

        query.setParameter("in_archive", inArchive);
        query.setParameter("metadata_field", metadataField);
        if(value != null)
        {
            query.setParameter("text_value", value);
        }
        return iterate(query);
    }

    public Iterator<Item> findByAuthorityValue(Context context, MetadataField metadataField, String authority, boolean inArchive) throws SQLException {
        Query query = createQuery(context, "SELECT item FROM Item as item join item.metadata metadatavalue WHERE item.inArchive=:in_archive AND metadatavalue.metadataField = :metadata_field AND metadatavalue.authority = :authority");
        query.setParameter("in_archive", inArchive);
        query.setParameter("metadata_field", metadataField);
        query.setParameter("authority", authority);
        return iterate(query);
    }

    public Iterator<Item> findArchivedByCollection(Context context, Collection collection, Integer limit, Integer offset) throws SQLException{
        Query query = createQuery(context, "select i from Item i join i.collections c WHERE :collection IN c AND i.inArchive=:in_archive");
        query.setParameter("collection", collection);
        query.setParameter("in_archive", true);
        if(offset != null)
        {
            query.setFirstResult(offset);
        }
        if(limit != null)
        {
            query.setMaxResults(limit);
        }
        @SuppressWarnings("unchecked")
        Iterator<Item> iterator = iterate(query);
        return iterator;
    }

    public Iterator<Item> findAllByCollection(Context context, Collection collection) throws SQLException {
        Query query = createQuery(context, "select i from Item i join i.collections c WHERE :collection IN c");
        query.setParameter("collection", collection);

        @SuppressWarnings("unchecked")
        Iterator<Item> iterator = iterate(query);
        return iterator;
    }
}
