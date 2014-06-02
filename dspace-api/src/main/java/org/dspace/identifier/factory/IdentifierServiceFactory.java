package org.dspace.identifier.factory;

import org.dspace.identifier.service.DOIService;
import org.dspace.identifier.service.IdentifierService;
import org.dspace.utils.DSpace;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 2/06/14
 * Time: 09:03
 */
public abstract class IdentifierServiceFactory {

    public abstract IdentifierService getIdentifierService();

    public abstract DOIService getDOIService();

    public static IdentifierServiceFactory getInstance(){
        return new DSpace().getServiceManager().getServiceByName("identifierServiceFactory", IdentifierServiceFactory.class);
    }
}
