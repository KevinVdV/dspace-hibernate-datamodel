package org.dspace.harvest.service;

import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.harvest.HarvestedItem;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 23/04/14
 * Time: 12:11
 */
public interface HarvestedItemService {

    public HarvestedItem create(Context context, Item item, String itemOAIid) throws SQLException;

    /**
     * Find the harvest parameters corresponding to the specified DSpace item
     * @return a HarvestedItem object corresponding to this item, null if not found.
     */
    public HarvestedItem find(Context context, Item item) throws SQLException;

    /**
     * Retrieve a DSpace Item that corresponds to this particular combination of owning collection and OAI ID.
     *
     * FYI: This method has to be scoped to a collection. Otherwise, we could have collisions as more
     * than one collection might be importing the same item. That is OAI_ID's might be unique to the
     * provider but not to the harvester.
     *
     * @param context the dspace context
     * @param itemOaiID the string used by the OAI-PMH provider to identify the item
     * @param collection id of the local collection that the item should be found in
     * @return DSpace Item or null if no item was found
     */
    public HarvestedItem findByOAIId(Context context, String itemOaiID, Collection collection) throws SQLException;

    public void update(Context context, HarvestedItem harvestedItem) throws SQLException;

    public void delete(Context context, HarvestedItem harvestedItem) throws SQLException;
}
