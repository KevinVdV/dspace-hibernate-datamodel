package org.dspace.content.authority.factory;

import org.dspace.content.authority.service.ChoiceAuthorityService;
import org.dspace.content.authority.service.MetadataAuthorityService;
import org.dspace.utils.DSpace;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 28/05/14
 * Time: 10:18
 */
public abstract class ContentAuthorityServiceFactory {

    public abstract ChoiceAuthorityService getChoiceAuthorityService();

    public abstract MetadataAuthorityService getMetadataAuthorityService();

    public static ContentAuthorityServiceFactory getInstance(){
        return new DSpace().getServiceManager().getServiceByName("contentAuthorityServiceFactory", ContentAuthorityServiceFactory.class);
    }
}
