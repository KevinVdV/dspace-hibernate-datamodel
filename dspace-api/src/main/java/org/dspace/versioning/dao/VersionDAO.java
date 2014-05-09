package org.dspace.versioning.dao;

import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;
import org.dspace.versioning.Version;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 8/05/14
 * Time: 09:56
 */
public interface VersionDAO extends GenericDAO<Version>
{
    public Version findByItem(Context context, Item item) throws SQLException;
}
