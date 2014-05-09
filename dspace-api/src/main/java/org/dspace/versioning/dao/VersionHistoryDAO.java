package org.dspace.versioning.dao;

import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;
import org.dspace.versioning.VersionHistory;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 8/05/14
 * Time: 11:12
 */
public interface VersionHistoryDAO extends GenericDAO<VersionHistory> {

    public VersionHistory findByItem(Context context, Item item) throws SQLException;
}
