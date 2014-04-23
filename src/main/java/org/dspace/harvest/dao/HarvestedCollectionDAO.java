package org.dspace.harvest.dao;

import org.dspace.content.Collection;
import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;
import org.dspace.harvest.HarvestedCollection;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 23/04/14
 * Time: 12:59
 */
public interface HarvestedCollectionDAO extends GenericDAO<HarvestedCollection> {

    public HarvestedCollection findByStatusAndMinimalTypeOrderByLastHarvestedDesc(Context context, int status, int type, int limit) throws SQLException;

    public HarvestedCollection findByStatusAndMinimalTypeOrderByLastHarvestedAsc(Context context, int status, int type, int limit) throws SQLException;

    public List<HarvestedCollection> findByStatus(Context context, int status) throws SQLException;

    public HarvestedCollection findByCollection(Context context, Collection collection) throws SQLException;

    List<HarvestedCollection> findByLastHarvestedAndHarvestTypeAndHarvestStatusesAndHarvestTime(Context context, Date startTime, int minimalType, int[] statuses ,int expirationStatus, Date expirationTime) throws SQLException;
}
