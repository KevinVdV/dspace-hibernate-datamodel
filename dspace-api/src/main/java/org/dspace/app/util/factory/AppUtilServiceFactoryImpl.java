package org.dspace.app.util.factory;

import org.dspace.app.util.service.WebAppService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 2/06/14
 * Time: 16:04
 */
public class AppUtilServiceFactoryImpl extends AppUtilServiceFactory{
    @Autowired(required = true)
    private WebAppService webAppService;

    @Override
    public WebAppService getWebAppService() {
        return webAppService;
    }
}
