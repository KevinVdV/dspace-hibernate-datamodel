package org.dspace.checker.factory;

import org.dspace.checker.service.ChecksumHistoryService;
import org.dspace.checker.service.ChecksumResultService;
import org.dspace.checker.service.MostRecentChecksumService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 2/06/14
 * Time: 11:19
 */
public class CheckerServiceFactoryImpl extends CheckerServiceFactory {

    @Autowired(required = true)
    private MostRecentChecksumService mostRecentChecksumService;
    @Autowired(required = true)
    private ChecksumHistoryService checksumHistoryService;
    @Autowired(required = true)
    private ChecksumResultService checksumResultService;

    @Override
    public MostRecentChecksumService getMostRecentChecksumService() {
        return mostRecentChecksumService;
    }

    @Override
    public ChecksumHistoryService getChecksumHistoryService() {
        return checksumHistoryService;
    }

    @Override
    public ChecksumResultService getChecksumResultService() {
        return checksumResultService;
    }
}
