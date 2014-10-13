package org.dspace.content.service;

import org.dspace.content.Site;
import org.dspace.core.Context;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 18/07/14
 * Time: 16:06
 */
public interface SiteService {

    public Site createSite(Context context) throws SQLException;

    public Site findSite(Context context) throws SQLException;
}
