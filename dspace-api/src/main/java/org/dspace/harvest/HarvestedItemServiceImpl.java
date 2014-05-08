package org.dspace.harvest;

import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.harvest.dao.HarvestedItemDAO;
import org.dspace.harvest.service.HarvestedItemService;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 23/04/14
 * Time: 12:17
 */
public class HarvestedItemServiceImpl implements HarvestedItemService {

    @Autowired(required = true)
    protected HarvestedItemDAO harvestedItemDAO;

    @Override
    public HarvestedItem create(Context context, Item item, String itemOAIid) throws SQLException {
        HarvestedItem harvestedItem = harvestedItemDAO.create(context, new HarvestedItem());
        harvestedItem.setItem(item);
        harvestedItem.setOaiId(itemOAIid);
        update(context, harvestedItem);
        return harvestedItem;
    }

    @Override
    public HarvestedItem find(Context context, Item item) throws SQLException {
        return harvestedItemDAO.findByItem(context, item);
    }

    @Override
    public HarvestedItem findByOAIId(Context context, String itemOaiID, Collection collection) throws SQLException {
        return harvestedItemDAO.findByOAIId(context, itemOaiID, collection);
    }

    @Override
    public void update(Context context, HarvestedItem harvestedItem) throws SQLException {
        harvestedItemDAO.save(context, harvestedItem);
    }

    @Override
    public void delete(Context context, HarvestedItem harvestedItem) throws SQLException {
        harvestedItemDAO.delete(context, harvestedItem);

    }
}
