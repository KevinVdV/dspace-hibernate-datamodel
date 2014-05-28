package org.dspace.embargo.factory;

import org.dspace.embargo.service.EmbargoService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 28/05/14
 * Time: 09:56
 */
public class EmbargoServiceFactoryImpl extends EmbargoServiceFactory {

    @Autowired(required = true)
    private EmbargoService embargoService;

    @Override
    public EmbargoService getEmbargoService() {
        return embargoService;
    }
}
