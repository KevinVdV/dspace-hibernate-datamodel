package org.dspace.checker.factory;

import org.dspace.checker.service.ChecksumHistoryService;
import org.dspace.checker.service.ChecksumResultService;
import org.dspace.checker.service.MostRecentChecksumService;
import org.dspace.utils.DSpace;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 2/06/14
 * Time: 11:16
 */
public abstract class CheckerServiceFactory {

    public abstract MostRecentChecksumService getMostRecentChecksumService();

    public abstract ChecksumHistoryService getChecksumHistoryService();

    public abstract ChecksumResultService getChecksumResultService();

    public static CheckerServiceFactory getInstance()
    {
        return new DSpace().getServiceManager().getServiceByName("checkerServiceFactory", CheckerServiceFactory.class);
    }
}
