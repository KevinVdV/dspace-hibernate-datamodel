package org.dspace.harvest.factory;

import org.dspace.harvest.service.HarvestedCollectionService;
import org.dspace.harvest.service.HarvestedItemService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 2/06/14
 * Time: 10:16
 */
public class HarvestServiceFactoryImpl extends HarvestServiceFactory {

    @Autowired(required = true)
    private HarvestedItemService harvestedItemService;

    @Autowired(required = true)
    private HarvestedCollectionService harvestedCollectionService;

    @Override
    public HarvestedItemService getHarvestedItemService() {
        return harvestedItemService;
    }

    @Override
    public HarvestedCollectionService getHarvestedCollectionService() {
        return harvestedCollectionService;
    }
}
