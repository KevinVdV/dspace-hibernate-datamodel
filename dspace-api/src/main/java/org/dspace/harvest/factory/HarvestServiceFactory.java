package org.dspace.harvest.factory;

import org.dspace.harvest.service.HarvestedCollectionService;
import org.dspace.harvest.service.HarvestedItemService;
import org.dspace.utils.DSpace;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 2/06/14
 * Time: 10:14
 */
public abstract class HarvestServiceFactory {

    public abstract HarvestedItemService getHarvestedItemService();

    public abstract HarvestedCollectionService getHarvestedCollectionService();

    public static HarvestServiceFactory getInstance(){
        return new DSpace().getServiceManager().getServiceByName("harvestServiceFactory", HarvestServiceFactory.class);

    }
}
