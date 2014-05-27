package org.dspace.core.factory;

import org.dspace.core.service.LicenseService;
import org.dspace.core.service.NewsService;
import org.dspace.utils.DSpace;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 27/05/14
 * Time: 10:53
 */
public abstract class CoreServiceFactory {

    public abstract LicenseService getLicenseService();

    public abstract NewsService getNewsService();

    public static CoreServiceFactory getInstance()
    {
        return new DSpace().getServiceManager().getServiceByName("coreServiceFactory", CoreServiceFactory.class);
    }
}
