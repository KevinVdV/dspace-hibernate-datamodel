package org.dspace.embargo.factory;

import org.dspace.embargo.service.EmbargoService;
import org.dspace.utils.DSpace;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 28/05/14
 * Time: 09:55
 */
public abstract class EmbargoServiceFactory {

    public abstract EmbargoService getEmbargoService();

    public static EmbargoServiceFactory getInstance(){
        return new DSpace().getServiceManager().getServiceByName("embargoFactory", EmbargoServiceFactory.class);
    }

}
