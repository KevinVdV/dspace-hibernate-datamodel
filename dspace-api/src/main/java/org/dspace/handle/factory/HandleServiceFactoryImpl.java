package org.dspace.handle.factory;

import org.dspace.handle.service.HandleService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 2/06/14
 * Time: 10:42
 */
public class HandleServiceFactoryImpl extends HandleServiceFactory {

    @Autowired(required = true)
    private HandleService handleService;

    @Override
    public HandleService getHandleService() {
        return handleService;
    }
}
