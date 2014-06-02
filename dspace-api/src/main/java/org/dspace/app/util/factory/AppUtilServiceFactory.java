package org.dspace.app.util.factory;

import org.dspace.app.util.service.WebAppService;
import org.dspace.utils.DSpace;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 2/06/14
 * Time: 16:03
 */
public abstract class AppUtilServiceFactory
{
    public abstract WebAppService getWebAppService();

    public static AppUtilServiceFactory getInstance()
    {
        return new DSpace().getServiceManager().getServiceByName("appUtilServiceFactory", AppUtilServiceFactory.class);
    }

}
