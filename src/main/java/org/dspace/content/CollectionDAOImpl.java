package org.dspace.content;

import org.dspace.core.Context;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 14/03/14
 * Time: 15:47
 */
public class CollectionDAOImpl extends CollectionDAO {

    /**
     * Get all collections in the system. These are alphabetically sorted by
     * collection name.
     *
     * @param context
     *            DSpace context object
     *
     * @return the collections in the system
     * @throws java.sql.SQLException
     */
    public List<Collection> findAll(Context context, String order) throws SQLException
    {
        return findAll(context, order, null, null);
    }


    @Override
    public List<Collection> findAll(Context context, String order, Integer limit, Integer offset) throws SQLException {
        Criteria criteria = getCriteria(context);
        criteria.addOrder(Order.asc(order));
        if(limit != null)
        {
            criteria.setFirstResult(limit);
        }
        if(offset != null){
            criteria.setMaxResults(offset);
        }
        @SuppressWarnings("unchecked")
        List<Collection> collections = criteria.list();
        return collections;
    }

    public Iterator<Item> getItems(Context context, Collection collection, boolean inArchive) throws SQLException
    {
        return getItems(context, collection, inArchive, null, null);
    }

    public Iterator<Item> getItems(Context context, Collection collection, boolean inArchive, Integer limit, Integer offset) throws SQLException{
        Query query = context.getDBConnection()
                .createQuery("select i from Item i join i.collections c WHERE :collection IN c.id AND i.inArchive=:in_archive");
        query.setParameter("collection", collection.getID());
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
        Iterator<Item> iterator = query.iterate();
        return iterator;
    }

    @Override
    public Iterator<Item> getAllItems(Context context, Collection collection) throws SQLException {
        Query query = context.getDBConnection().createQuery("select i from Item i join i.collections c WHERE :collection IN c.id");
        query.setParameter("collection", collection.getID());

        @SuppressWarnings("unchecked")
        Iterator<Item> iterator = query.iterate();
        return iterator;
    }

    @Override
    public Collection findByTemplateItem(Context context, Item item) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(Collection.class);
        criteria.add(Restrictions.eq("template_item_id", item.getID()));
        return (Collection) criteria.uniqueResult();
    }


    protected Criteria getCriteria(Context context) throws SQLException {
        return context.getDBConnection().createCriteria(Collection.class);
    }

}
