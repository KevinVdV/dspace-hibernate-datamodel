package org.dspace.authorize.factory;

import org.dspace.authorize.service.AuthorizeService;
import org.dspace.authorize.service.ResourcePolicyService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 27/05/14
 * Time: 09:44
 */
public class AuthorizeServiceFactoryImpl extends AuthorizeServiceFactory {

    @Autowired(required = true)
    private AuthorizeService authorizeService;
    @Autowired(required = true)
    private ResourcePolicyService resourcePolicyService;

    @Override
    public AuthorizeService getAuthorizeService() {
        return authorizeService;
    }

    @Override
    public ResourcePolicyService getResourcePolicyService() {
        return resourcePolicyService;
    }
}
