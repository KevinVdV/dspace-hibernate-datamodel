package org.dspace.core.factory;

import org.dspace.core.service.LicenseService;
import org.dspace.core.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 27/05/14
 * Time: 10:53
 */
public class CoreServiceFactoryImpl extends CoreServiceFactory {

    @Autowired(required=true)
    private LicenseService licenseService;

    @Autowired(required=true)
    private NewsService newsService;

    @Override
    public LicenseService getLicenseService() {
        return licenseService;
    }

    @Override
    public NewsService getNewsService() {
        return newsService;
    }
}
