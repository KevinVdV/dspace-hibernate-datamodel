/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.versioning;

import org.apache.commons.collections.CollectionUtils;
import org.dspace.content.Collection;
import org.dspace.content.DCDate;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.versioning.dao.VersionDAO;
import org.dspace.versioning.service.VersionHistoryService;
import org.dspace.versioning.service.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 *
 *
 * @author Fabio Bolognesi (fabio at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 */
public class VersionServiceImpl implements VersionService {

    @Autowired(required = true)
    protected VersionHistoryService versionHistoryService;

    @Autowired(required = true)
    protected ItemService itemService;

    @Autowired(required = true)
    protected VersionDAO versionDAO;

    @Autowired(required = true)
    protected CollectionService collectionService;

    private ItemVersionProvider provider;


    /** Service Methods */
    public Version createVersion(Context c, Item item){
        return createVersion(c, item, null);
    }

    public Version createVersion(Context c, Item item, String summary) {
        try{
            VersionHistory vh = versionHistoryService.findByItem(c, item);
            if(vh==null)
            {
                // first time: create 2 versions, .1(old version) and .2(new version)
                vh = versionHistoryService.create(c);

                // get dc:date.accessioned to be set as first version date...
                List<MetadataValue> values = itemService.getMetadata(item, "dc", "date", "accessioned", Item.ANY);
                Date versionDate = new Date();
                for (MetadataValue metadataValue : values) {
                    String date = metadataValue.getValue();
                    versionDate = new DCDate(date).toDate();
                }
                createVersion(c, vh, item, "", versionDate, getNextVersionNumer(versionHistoryService.getLatestVersion(vh)));
            }
            // Create new Item
            Item itemNew = provider.createNewItemAndAddItInWorkspace(c, item);

            // create new version
            Version version = createVersion(c, vh, itemNew, summary, new Date(), getNextVersionNumer(versionHistoryService.getLatestVersion(vh)));

            // Complete any update of the Item and new Identifier generation that needs to happen
            provider.updateItemState(c, itemNew, item);

            return version;
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void removeVersion(Context c, int versionID) throws SQLException {
        Version version = find(c, versionID);
        if(version!=null){
            removeVersion(c, version);
        }
    }

    public void removeVersion(Context c, Item item) throws SQLException {
        Version version = versionDAO.findByItem(c, item);
        if(version!=null){
            removeVersion(c, version);
        }
    }

    protected void removeVersion(Context context, Version version) {
        try{
            VersionHistory history = version.getVersionHistory();
            provider.deleteVersionedItem(context, version, history);

            history.removeVersion(version);

            if(CollectionUtils.isEmpty(history.getVersions())){
                versionHistoryService.delete(context, version.getVersionHistory());
            }

            //Delete the item linked to the version
            Item item = version.getItem();
            List<Collection> collections = item.getCollections();
            versionDAO.delete(context, version);


            // Completely delete the item
            itemService.delete(context, item);
        }catch (Exception e) {
            context.abort();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Version find(Context c, int versionID) throws SQLException {
        return versionDAO.findByID(c, Version.class, versionID);
    }


    public Version restoreVersion(Context c, int versionID){
        return restoreVersion(c, versionID, null);
    }

    public Version restoreVersion(Context c, int versionID, String summary)  {
        return null;
    }

    public Version updateVersion(Context c, Item item, String summary) throws SQLException {
        Version version = versionDAO.findByItem(c, item);
        version.setSummary(summary);
        versionDAO.save(c, version);
        return version;
    }

    public Version findByItem(Context c, Item item) throws SQLException {
        return versionDAO.findByItem(c, item);
    }

// **** PROTECTED METHODS!!

    public Version createVersion(Context c, VersionHistory vh, Item item, String summary, Date date, int versionNumber) {
        try {
            Version version = versionDAO.create(c, new Version());

            version.setVersionNumber(versionNumber);
            version.setVersionDate(date);
            version.setePerson(item.getSubmitter());
            version.setItem(item);
            version.setSummary(summary);
            version.setVersionHistory(vh);
            versionDAO.save(c, version);
            versionHistoryService.add(vh, version);
            return version;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected int getNextVersionNumer(Version latest){
        if(latest==null) return 0;

        return latest.getId()+1;
    }

    @Required
    public void setProvider(ItemVersionProvider provider) {
        this.provider = provider;
    }
}
