/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.versioning;

import org.dspace.content.Item;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Consumer;
import org.dspace.event.Event;
import org.dspace.factory.DSpaceServiceFactory;
import org.dspace.versioning.factory.DSpaceVersionServiceFactory;
import org.dspace.versioning.service.VersionHistoryService;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 *
 * @author Fabio Bolognesi (fabio at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 */
public class VersioningConsumer implements Consumer {

    private Set<Item> itemsToProcess;

    private VersionHistoryService versionHistoryService = DSpaceVersionServiceFactory.getInstance().getVersionHistoryService();
    private ItemService itemService = DSpaceServiceFactory.getInstance().getItemService();

    public void initialize() throws Exception {}

    public void finish(Context ctx) throws Exception {}

    public void consume(Context ctx, Event event) throws Exception {
        if(itemsToProcess == null){
            itemsToProcess = new HashSet<Item>();
        }

        int st = event.getSubjectType();
        int et = event.getEventType();

        if(st == Constants.ITEM && et == Event.INSTALL){
            Item item = (Item) event.getSubject(ctx);
            if (item != null && item.isArchived()) {
                VersionHistory history = retrieveVersionHistory(ctx, item);
                if (history != null) {
                    Version latest = versionHistoryService.getLatestVersion(history);
                    Version previous = versionHistoryService.getPrevious(history, latest);
                    if(previous != null){
                        Item previousItem = previous.getItem();
                        if(previousItem != null){
                            previousItem.setInArchive(false);
                            itemsToProcess.add(previousItem);
                            //Fire a new modify event for our previous item
                            //Due to the need to reindex the item in the search & browse index we need to fire a new event
                            ctx.addEvent(new Event(Event.MODIFY, previousItem.getType(), previousItem.getID(), null));
                        }
                    }
                }
            }
        }
    }

    public void end(Context ctx) throws Exception {
        if(itemsToProcess != null){
            for(Item item : itemsToProcess){
                ctx.turnOffAuthorisationSystem();
                try {
                    itemService.update(ctx, item);
                } finally {
                    ctx.restoreAuthSystemState();
                }
            }
            ctx.commitNoEventDispatching();
        }

        itemsToProcess = null;
    }

    private org.dspace.versioning.VersionHistory retrieveVersionHistory(Context c, Item item) throws SQLException {
        return versionHistoryService.findByItem(c, item);
    }
}