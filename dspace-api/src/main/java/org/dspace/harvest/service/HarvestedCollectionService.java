package org.dspace.harvest.service;

import org.dspace.content.Collection;
import org.dspace.core.Context;
import org.dspace.harvest.HarvestedCollection;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 23/04/14
 * Time: 13:04
 */
public interface HarvestedCollectionService {
    public static final int TYPE_NONE = 0;
    public static final int TYPE_DMD = 1;
    public static final int TYPE_DMDREF = 2;
    public static final int TYPE_FULL = 3;

    public static final int STATUS_READY = 0;
    public static final int STATUS_BUSY = 1;
    public static final int STATUS_QUEUED = 2;
    public static final int STATUS_OAI_ERROR = 3;
    public static final int STATUS_UNKNOWN_ERROR = -1;

    public HarvestedCollection findByCollection(Context context, Collection collection) throws SQLException;

    public HarvestedCollection create(Context context, Collection collection) throws SQLException;

    public boolean isHarvestable(Context c, Collection collection) throws SQLException;

    public boolean isHarvestable(HarvestedCollection harvestedCollection) throws SQLException;

    public boolean isReady(Context context, Collection collection) throws SQLException;

    public boolean isReady(HarvestedCollection harvestedCollection) throws SQLException;

    public List<HarvestedCollection> findAll(Context context) throws SQLException;

    public List<HarvestedCollection> findReady(Context context) throws SQLException;

    public List<HarvestedCollection> findByStatus(Context context, int status) throws SQLException;

    public HarvestedCollection findOldestHarvest(Context context) throws SQLException;

    public HarvestedCollection findNewestHarvest(Context context) throws SQLException;

    public void setHarvestParams(HarvestedCollection harvestedCollection, int type, String oaiSource, String oaiSetId, String mdConfigId);

    public void delete(Context context, HarvestedCollection harvestedCollection) throws SQLException;

    public void update(Context context, HarvestedCollection harvestedCollection) throws SQLException;
}
