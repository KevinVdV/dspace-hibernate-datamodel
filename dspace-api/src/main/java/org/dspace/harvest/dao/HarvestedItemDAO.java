package org.dspace.harvest.dao;

import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;
import org.dspace.harvest.HarvestedItem;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 23/04/14
 * Time: 12:20
 */
public interface HarvestedItemDAO extends GenericDAO<HarvestedItem> {

    public HarvestedItem findByItem(Context context, Item item) throws SQLException;

    public HarvestedItem findByOAIId(Context context, String itemOaiID, Collection collection) throws SQLException;
}
