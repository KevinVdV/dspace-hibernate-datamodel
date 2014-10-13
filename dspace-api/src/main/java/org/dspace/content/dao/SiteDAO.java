package org.dspace.content.dao;

import org.dspace.content.Site;
import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 18/07/14
 * Time: 16:07
 */
public interface SiteDAO extends GenericDAO<Site> {

    public Site findSite(Context context) throws SQLException;
}
