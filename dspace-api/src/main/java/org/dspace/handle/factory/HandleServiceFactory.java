package org.dspace.handle.factory;

import org.dspace.handle.service.HandleService;
import org.dspace.utils.DSpace;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 2/06/14
 * Time: 10:40
 */
public abstract class HandleServiceFactory {

    public abstract HandleService getHandleService();

    public static HandleServiceFactory getInstance()
    {
        return new DSpace().getServiceManager().getServiceByName("handleServiceFactory", HandleServiceFactory.class);
    }
}
