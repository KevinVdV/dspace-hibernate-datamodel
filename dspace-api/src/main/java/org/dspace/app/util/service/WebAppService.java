package org.dspace.app.util.service;

import org.dspace.app.util.WebApp;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 29/04/14
 * Time: 12:47
 */
public interface WebAppService {

    public WebApp create(Context context, String appName, String url, Date started, int isUI) throws SQLException;

    public List<WebApp> findAll(Context context) throws SQLException;

    public void delete(Context context, WebApp webApp) throws SQLException;
}
