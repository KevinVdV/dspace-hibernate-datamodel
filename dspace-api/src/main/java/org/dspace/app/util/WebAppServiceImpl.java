package org.dspace.app.util;

import org.dspace.app.util.dao.WebAppDAO;
import org.dspace.app.util.service.WebAppService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 29/04/14
 * Time: 12:40
 */
public class WebAppServiceImpl implements WebAppService{

    @Autowired(required = true)
    protected WebAppDAO webAppDAO;


    public WebApp create(Context context, String appName, String url, Date started, int isUI) throws SQLException {
        WebApp webApp = webAppDAO.create(context, new WebApp());
        webApp.setAppName(appName);
        webApp.setUrl(url);
        webApp.setStarted(started);
        webApp.setIsui(isUI);
        webAppDAO.save(context, webApp);
        return webApp;
    }

    public void delete(Context context, WebApp webApp) throws SQLException {
        webAppDAO.delete(context, webApp);
    }

    public List<WebApp> findAll(Context context) throws SQLException {
        return webAppDAO.findAll(context, WebApp.class);
    }
}
