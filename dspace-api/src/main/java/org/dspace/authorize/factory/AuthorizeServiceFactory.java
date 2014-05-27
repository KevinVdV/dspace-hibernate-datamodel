package org.dspace.authorize.factory;

import org.dspace.authorize.service.AuthorizeService;
import org.dspace.authorize.service.ResourcePolicyService;
import org.dspace.utils.DSpace;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 27/05/14
 * Time: 09:40
 */
public abstract class AuthorizeServiceFactory {

    public abstract AuthorizeService getAuthorizeService();

    public abstract ResourcePolicyService getResourcePolicyService();

    public static AuthorizeServiceFactory getInstance()
    {
        return new DSpace().getServiceManager().getServiceByName("authorizeServiceFactory", AuthorizeServiceFactory.class);
    }
}
