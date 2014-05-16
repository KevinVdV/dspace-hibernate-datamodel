package org.dspace.harvest;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.harvest.dao.HarvestedCollectionDAO;
import org.dspace.harvest.service.HarvestedCollectionService;
import org.dspace.harvest.service.HarvestedItemService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 23/04/14
 * Time: 12:56
 */
public class HarvestedCollectionServiceImpl implements HarvestedCollectionService{

    @Autowired(required = true)
    protected HarvestedCollectionDAO harvestedCollectionDAO;

    // TODO: make sure this guy knows to lock people out if the status is not zero.
    // i.e. someone editing a collection's setting from the admin menu should have
    // to stop an ongoing harvest before they can edit the settings.


    public HarvestedCollectionServiceImpl() {
    }


    /**
     * Find the harvest settings corresponding to this collection
     *
     * @return a HarvestInstance object corresponding to this collection's settings, null if not found.
     */
    @Override
    public HarvestedCollection findByCollection(Context context, Collection collection) throws SQLException {
        return harvestedCollectionDAO.findByCollection(context, collection);
    }

    /**
     * Create a new harvest instance row for a specified collection.
     *
     * @return a new HarvestInstance object
     */
    @Override
    public HarvestedCollection create(Context context, Collection collection) throws SQLException {
        HarvestedCollection harvestedCollection = harvestedCollectionDAO.create(context, new HarvestedCollection());
        harvestedCollection.setCollection(collection);
        harvestedCollection.setHarvestType(HarvestedCollectionService.TYPE_NONE);
        update(context, harvestedCollection);
        return harvestedCollection;
    }

    /**
     * Returns whether the specified collection is harvestable, i.e. whether its harvesting
     * options are set up correctly. This is distinct from "ready", since this collection may
     * be in process of being harvested.
     */
    @Override
    public boolean isHarvestable(Context c, Collection collection) throws SQLException {
        HarvestedCollection hc = findByCollection(c, collection);
        return hc != null && hc.getHarvestType() > 0 && hc.getOaiSource() != null && hc.getOaiSetId() != null &&
                hc.getHarvestStatus() != HarvestedCollectionService.STATUS_UNKNOWN_ERROR;
    }

    /**
     * Returns whether this harvest instance is actually harvestable, i.e. whether its settings
     * options are set up correctly. This is distinct from "ready", since this collection may
     * be in process of being harvested.
     */
    @Override
    public boolean isHarvestable(HarvestedCollection harvestedCollection) throws SQLException {
        if (harvestedCollection.getHarvestType() > 0 && harvestedCollection.getOaiSource() != null && harvestedCollection.getOaiSetId() != null &&
                harvestedCollection.getHarvestStatus() != HarvestedCollectionService.STATUS_UNKNOWN_ERROR) {
            return true;
        }

        return false;
    }

    /**
     * Returns whether the specified collection is ready for immediate harvest.
     */
    @Override
    public boolean isReady(Context context, Collection collection) throws SQLException {
        HarvestedCollection harvestedCollection = findByCollection(context, collection);
        return isReady(harvestedCollection);
    }

    @Override
    public boolean isReady(HarvestedCollection harvestedCollection) throws SQLException {
        if (isHarvestable(harvestedCollection) && (harvestedCollection.getHarvestStatus() == HarvestedCollectionService.STATUS_READY || harvestedCollection.getHarvestStatus() == HarvestedCollectionService.STATUS_OAI_ERROR)) {
            return true;
        }

        return false;
    }


    /**
     * Find all collections that are set up for harvesting
     * <p/>
     * return: list of collection id's
     *
     * @throws SQLException
     */
    @Override
    public List<HarvestedCollection> findAll(Context context) throws SQLException {
        return harvestedCollectionDAO.findAll(context, HarvestedCollection.class);
    }

    /**
     * Find all collections that are ready for harvesting
     * <p/>
     * return: list of collection id's
     *
     * @throws SQLException
     */
    @Override
    public List<HarvestedCollection> findReady(Context context) throws SQLException {
        int harvestInterval = ConfigurationManager.getIntProperty("oai", "harvester.harvestFrequency");
        if (harvestInterval == 0) {
            harvestInterval = 720;
        }

        int expirationInterval = ConfigurationManager.getIntProperty("oai", "harvester.threadTimeout");
        if (expirationInterval == 0) {
            expirationInterval = 24;
        }

        Date startTime;
        Date expirationTime;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, -1 * harvestInterval);
        startTime = calendar.getTime();

        calendar.setTime(startTime);
        calendar.add(Calendar.HOUR, -2 * expirationInterval);
        expirationTime = calendar.getTime();

       	/* Select all collections whose last_harvest is before our start time, whose harvest_type *is not* 0 and whose status *is* 0 (available) or 3 (OAI Error). */
        int[] statuses = new int[]{HarvestedCollectionService.STATUS_READY, HarvestedCollectionService.STATUS_OAI_ERROR};
        return harvestedCollectionDAO.findByLastHarvestedAndHarvestTypeAndHarvestStatusesAndHarvestTime(context, startTime, HarvestedCollectionService.TYPE_NONE, statuses, HarvestedCollectionService.STATUS_BUSY, expirationTime);

    }

    /**
     * Find all collections with the specified status flag.
     *
     * @param context the dspace context
     * @param status see HarvestInstance.STATUS_...
     * @throws SQLException
     */
    @Override
    public List<HarvestedCollection> findByStatus(Context context, int status) throws SQLException {
        return harvestedCollectionDAO.findByStatus(context, status);
    }


    /**
     * Find the collection that was harvested the longest time ago.
     *
     * @throws SQLException
     */
    @Override
    public HarvestedCollection findOldestHarvest(Context context) throws SQLException {
        return harvestedCollectionDAO.findByStatusAndMinimalTypeOrderByLastHarvestedAsc(context, HarvestedCollectionService.STATUS_READY, HarvestedCollectionService.TYPE_NONE, 1);
    }

    /**
     * Find the collection that was harvested most recently.
     *
     * @throws SQLException
     */
    @Override
    public HarvestedCollection findNewestHarvest(Context context) throws SQLException {
        return harvestedCollectionDAO.findByStatusAndMinimalTypeOrderByLastHarvestedDesc(context, HarvestedCollectionService.STATUS_READY, HarvestedCollectionService.TYPE_NONE, 1);
    }


    /**
     * A function to set all harvesting-related parameters at once
     */
    @Override
    public void setHarvestParams(HarvestedCollection harvestedCollection, int type, String oaiSource, String oaiSetId, String mdConfigId) {
        harvestedCollection.setHarvestType(type);
        harvestedCollection.setOaiSource(oaiSource);
        harvestedCollection.setOaiSetId(oaiSetId);
        harvestedCollection.setMetadataConfigId(mdConfigId);
    }

    @Override
    public void delete(Context context, HarvestedCollection harvestedCollection) throws SQLException {
        harvestedCollectionDAO.delete(context, harvestedCollection);
    }

    @Override
    public void update(Context context, HarvestedCollection harvestedCollection) throws SQLException {
        harvestedCollectionDAO.save(context, harvestedCollection);

    }
}
