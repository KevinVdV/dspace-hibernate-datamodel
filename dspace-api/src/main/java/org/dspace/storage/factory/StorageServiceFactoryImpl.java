package org.dspace.storage.factory;

import org.dspace.storage.service.BitstreamStorageService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 27/05/14
 * Time: 12:05
 */
public class StorageServiceFactoryImpl extends StorageServiceFactory {

    @Autowired(required = true)
    private BitstreamStorageService bitstreamStorageService;

    @Override
    public BitstreamStorageService getBitstreamStorageService() {
        return bitstreamStorageService;
    }
}
