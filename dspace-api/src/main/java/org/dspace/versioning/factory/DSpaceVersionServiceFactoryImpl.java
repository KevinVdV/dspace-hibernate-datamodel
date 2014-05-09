package org.dspace.versioning.factory;

import org.dspace.versioning.VersionHistory;
import org.dspace.versioning.service.VersionHistoryService;
import org.dspace.versioning.service.VersionService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 8/05/14
 * Time: 14:00
 */
public class DSpaceVersionServiceFactoryImpl extends DSpaceVersionServiceFactory{

    @Autowired(required = true)
    protected VersionHistoryService versionHistoryService;

    @Autowired(required = true)
    protected VersionService versionService;

    @Override
    public VersionHistoryService getVersionHistoryService() {
        return versionHistoryService;
    }

    @Override
    public VersionService getVersionService() {
        return versionService;
    }
}
