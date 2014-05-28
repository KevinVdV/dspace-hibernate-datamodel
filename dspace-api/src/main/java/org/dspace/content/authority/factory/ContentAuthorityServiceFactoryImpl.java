package org.dspace.content.authority.factory;

import org.dspace.content.authority.service.ChoiceAuthorityService;
import org.dspace.content.authority.service.MetadataAuthorityService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 28/05/14
 * Time: 10:32
 */
public class ContentAuthorityServiceFactoryImpl extends ContentAuthorityServiceFactory {

    @Autowired(required = true)
    private ChoiceAuthorityService choiceAuthorityService;

    @Autowired(required = true)
    private MetadataAuthorityService metadataAuthorityService;


    @Override
    public ChoiceAuthorityService getChoiceAuthorityService()
    {
        return choiceAuthorityService;
    }

    @Override
    public MetadataAuthorityService getMetadataAuthorityService()
    {
        return metadataAuthorityService;
    }
}
