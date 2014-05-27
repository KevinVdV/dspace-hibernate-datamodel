package org.dspace.storage.factory;

import org.dspace.storage.service.BitstreamStorageService;
import org.dspace.utils.DSpace;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 27/05/14
 * Time: 12:05
 */
public abstract class StorageServiceFactory {

    public abstract BitstreamStorageService getBitstreamStorageService();

    public static StorageServiceFactory getInstance()
    {
        return new DSpace().getServiceManager().getServiceByName("storageServiceFactory", StorageServiceFactory.class);
    }
}
