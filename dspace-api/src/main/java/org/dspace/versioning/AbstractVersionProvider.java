/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.versioning;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.BundleService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.factory.DSpaceServiceFactory;
import org.dspace.storage.factory.StorageServiceFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 *
 *
 * @author Fabio Bolognesi (fabio at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 */
public abstract class AbstractVersionProvider {

    private Set<String> ignoredMetadataFields;

    protected void copyMetadata(Context context, Item itemNew, Item nativeItem) throws SQLException {
        ItemService itemService = DSpaceServiceFactory.getInstance().getItemService();

        List<MetadataValue> md = itemService.getMetadata(nativeItem, Item.ANY, Item.ANY, Item.ANY, Item.ANY);
        for (MetadataValue aMd : md) {
            MetadataField metadataField = aMd.getMetadataField();
            String unqualifiedMetadataField = aMd.getMetadataField().getMetadataSchema().getName() + "." + metadataField.getElement();
            String qualifiedMetadataField = unqualifiedMetadataField + (metadataField.getQualifier() == null ? "" : "." + metadataField.getQualifier());
            if(getIgnoredMetadataFields().contains(qualifiedMetadataField) ||
                    getIgnoredMetadataFields().contains(unqualifiedMetadataField + "." + Item.ANY))
            {
                //Skip this metadata field
                continue;
            }

            itemService.addMetadata(context, nativeItem, metadataField, aMd.getLanguage(), aMd.getValue());
        }
    }

    protected void createBundlesAndAddBitstreams(Context c, Item itemNew, Item nativeItem) throws SQLException, AuthorizeException {
        ItemService itemService = DSpaceServiceFactory.getInstance().getItemService();
        BundleService bundleService = DSpaceServiceFactory.getInstance().getBundleService();
        for(Bundle nativeBundle : nativeItem.getBundles())
        {
            Bundle bundleNew = bundleService.create(c, itemNew, nativeBundle.getName());

            for(Bitstream nativeBitstream : nativeBundle.getBitstreams())
            {

                Bitstream bitstreamNew = createBitstream(c, nativeBitstream);
                bundleService.addBitstream(c, bundleNew, bitstreamNew);

                if(nativeBundle.getPrimaryBitstream().getID() == nativeBitstream.getID())
                {
                    bundleNew.setPrimaryBitstream(bitstreamNew);
                }
            }
        }
    }


    protected Bitstream createBitstream(Context context, Bitstream nativeBitstream) throws AuthorizeException, SQLException {
        BitstreamService bitstreamService = DSpaceServiceFactory.getInstance().getBitstreamService();
        int idNew = StorageServiceFactory.getInstance().getBitstreamStorageService().clone(context, nativeBitstream.getID());
        return bitstreamService.find(context, idNew);
    }

    public void setIgnoredMetadataFields(Set<String> ignoredMetadataFields) {
        this.ignoredMetadataFields = ignoredMetadataFields;
    }

    public Set getIgnoredMetadataFields() {
        return ignoredMetadataFields;
    }
}
