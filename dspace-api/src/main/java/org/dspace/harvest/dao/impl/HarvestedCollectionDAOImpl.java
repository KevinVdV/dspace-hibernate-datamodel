package org.dspace.harvest.dao.impl;

import org.dspace.content.Collection;
import org.dspace.core.Context;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.harvest.HarvestedCollection;
import org.dspace.harvest.dao.HarvestedCollectionDAO;
import org.hibernate.Criteria;
import org.hibernate.criterion.*;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 23/04/14
 * Time: 13:39
 */
public class HarvestedCollectionDAOImpl extends AbstractHibernateDAO<HarvestedCollection> implements HarvestedCollectionDAO{


    @Override
    public HarvestedCollection findByStatusAndMinimalTypeOrderByLastHarvestedDesc(Context context, int status, int type, int limit) throws SQLException {
//      Old query: "select collection_id from harvested_collection where harvest_type > ? and harvest_status = ? order by last_harvested desc limit 1";
        Criteria criteria = getByStatusAndMinimalTypeCriteria(context, status, type, limit);
        criteria.addOrder(Order.desc("lastHarvested"));
        return uniqueResult(criteria);
    }

    @Override
    public HarvestedCollection findByStatusAndMinimalTypeOrderByLastHarvestedAsc(Context context, int status, int type, int limit) throws SQLException {
//        Old query: "select collection_id from harvested_collection where harvest_type > ? and harvest_status = ? order by last_harvested asc limit 1";
        Criteria criteria = getByStatusAndMinimalTypeCriteria(context, status, type, limit);
        criteria.addOrder(Order.asc("lastHarvested"));
        return uniqueResult(criteria);
    }

    @Override
    public List<HarvestedCollection> findByStatus(Context context, int status) throws SQLException {
        Criteria criteria = createCriteria(context, HarvestedCollection.class);
        criteria.add(Restrictions.eq("harvestStatus", status));
        return list(criteria);
    }

    @Override
    public HarvestedCollection findByCollection(Context context, Collection collection) throws SQLException {
        Criteria criteria = createCriteria(context, HarvestedCollection.class);
        criteria.add(Restrictions.eq("collection", collection));
        return uniqueResult(criteria);

    }

    @Override
    public List<HarvestedCollection> findByLastHarvestedAndHarvestTypeAndHarvestStatusesAndHarvestTime(Context context, Date startTime, int minimalType, int[] statuses, int expirationStatus, Date expirationTime) throws SQLException {
//      Old query: "SELECT * FROM harvested_collection WHERE
// (last_harvested < ? or last_harvested is null) and harvest_type > ? and (harvest_status = ? or harvest_status = ? or (harvest_status=? and harvest_start_time < ?)) ORDER BY last_harvested",
//                new java.sql.Timestamp(startTime.getTime()), 0, HarvestedCollection.STATUS_READY, HarvestedCollection.STATUS_OAI_ERROR, HarvestedCollection.STATUS_BUSY, new java.sql.Timestamp(expirationTime.getTime()));
        Criteria criteria = createCriteria(context, HarvestedCollection.class);
        LogicalExpression lastHarvestedRestriction = Restrictions.or(
                Restrictions.lt("lastHarvested", startTime),
                Restrictions.isNull("lastHarvested")
        );
        Disjunction statusRestriction = Restrictions.or();
        for (int status : statuses) {
            statusRestriction.add(Restrictions.eq("harvestStatus", status));
        }
        statusRestriction.add(
                Restrictions.and(
                        Restrictions.eq("harvestStatus", expirationStatus),
                        Restrictions.gt("harvestStartTime", expirationTime)
                )
        );

        criteria.add(
                Restrictions.and(
                        lastHarvestedRestriction,
                        Restrictions.gt("harvestType", minimalType),
                        statusRestriction

                )
        );
        criteria.addOrder(Order.asc("lastHarvested"));
        return list(criteria);

    }

    protected Criteria getByStatusAndMinimalTypeCriteria(Context context, int status, int type, int limit) throws SQLException {
        Criteria criteria = createCriteria(context, HarvestedCollection.class);
        criteria.add(
                Restrictions.and(
                        Restrictions.gt("harvestType", type),
                        Restrictions.eq("harvestStatus", status)
                )
        );
        if(limit != -1)
        {
            criteria.setMaxResults(1);
        }
        return criteria;
    }

}
