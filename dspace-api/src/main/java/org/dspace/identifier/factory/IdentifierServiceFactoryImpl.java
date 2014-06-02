package org.dspace.identifier.factory;

import org.dspace.identifier.service.DOIService;
import org.dspace.identifier.service.IdentifierService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 2/06/14
 * Time: 09:03
 */
public class IdentifierServiceFactoryImpl extends IdentifierServiceFactory {

    @Autowired(required = true)
    private IdentifierService identifierService;
    @Autowired(required = true)
    private DOIService doiService;

    @Override
    public IdentifierService getIdentifierService() {
        return identifierService;
    }

    @Override
    public DOIService getDOIService() {
        return doiService;
    }
}
