package org.dspace.versioning.factory;

import org.dspace.utils.DSpace;
import org.dspace.versioning.service.VersionHistoryService;
import org.dspace.versioning.service.VersionService;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 8/05/14
 * Time: 13:16
 */
public abstract class DSpaceVersionServiceFactory {

    public abstract VersionHistoryService getVersionHistoryService();

    public abstract VersionService getVersionService();

    public static DSpaceVersionServiceFactory getInstance()
    {
        return new DSpace().getServiceManager().getServiceByName("versionServiceFactory", DSpaceVersionServiceFactory.class);
    }
}
