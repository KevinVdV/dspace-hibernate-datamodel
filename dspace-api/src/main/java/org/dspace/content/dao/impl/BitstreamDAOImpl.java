package org.dspace.content.dao.impl;

import org.dspace.checker.MostRecentChecksum;
import org.dspace.content.Bitstream;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;
import org.dspace.content.dao.BitstreamDAO;
import org.dspace.core.Context;
import org.dspace.core.AbstractHibernateDAO;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 17/03/14
 * Time: 11:42
 */
public class BitstreamDAOImpl extends AbstractHibernateDAO<Bitstream> implements BitstreamDAO {

    @Override
    public List<Bitstream> findDeletedBitstreams(Context context) throws SQLException {
        Criteria criteria = createCriteria(context, Bitstream.class);
        criteria.add(Restrictions.eq("deleted", true));

        return list(criteria);

    }

    @Override
    public List<Bitstream> findDuplicateInternalIdentifier(Context context, Bitstream bitstream) throws SQLException {
        Criteria criteria = createCriteria(context, Bitstream.class);
        criteria.add(Restrictions.and(
                Restrictions.eq("internalId", bitstream.getInternalId()),
                Restrictions.not(Restrictions.eq("id", bitstream.getID()))
        ));

        return list(criteria);
    }

    @Override
    public List<Bitstream> findBitstreamsWithNoRecentChecksum(Context context) throws SQLException {
//        "select bitstream.deleted, bitstream.store_number, bitstream.size_bytes, "
//                    + "bitstreamformatregistry.short_description, bitstream.bitstream_id,  "
//                    + "bitstream.user_format_description, bitstream.internal_id, "
//                    + "bitstream.source, bitstream.checksum_algorithm, bitstream.checksum, "
//                    + "bitstream.name, bitstream.description "
//                    + "from bitstream left outer join bitstreamformatregistry on "
//                    + "bitstream.bitstream_format_id = bitstreamformatregistry.bitstream_format_id "
//                    + "where not exists( select 'x' from most_recent_checksum "
//                    + "where most_recent_checksum.bitstream_id = bitstream.bitstream_id )"
        Criteria criteria = createCriteria(context, Bitstream.class)
            .add(Subqueries.propertyNotIn("id", DetachedCriteria.forClass(MostRecentChecksum.class)));

        @SuppressWarnings("unchecked")
        List<Bitstream> result = (List<Bitstream>) criteria.list();
        return result;
    }

    @Override
    public Iterator<Bitstream> findByCommunity(Context context, Community community) throws SQLException {
        Query query = createQuery(context, "select b from Bitstream b " +
                "join b.bundles bitBundles " +
                "join bitBundles.bundle bundle " +
                "join bundle.items item " +
                "join item.collections itemColl " +
                "join itemColl.communities community " +
                "WHERE :community IN community");

        query.setParameter("community", community);

        return iterate(query);
    }

    @Override
    public Iterator<Bitstream> findByCollection(Context context, Collection collection) throws SQLException {
        Query query = createQuery(context, "select b from Bitstream b " +
                "join b.bundles bitBundles " +
                "join bitBundles.bundle bundle " +
                "join bundle.items item " +
                "join item.collections c " +
                "WHERE :collection IN c");

        query.setParameter("collection", collection);

        return iterate(query);
    }

    @Override
    public Iterator<Bitstream> findByItem(Context context, Item item) throws SQLException {
        Query query = createQuery(context, "select b from Bitstream b " +
                "join b.bundles bitBundles " +
                "join bitBundles.bundle bundle " +
                "join bundle.items item " +
                "WHERE :item IN item");

        query.setParameter("item", item);

        return iterate(query);
    }
}
