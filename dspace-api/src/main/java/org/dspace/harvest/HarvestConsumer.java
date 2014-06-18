/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.harvest;

import org.apache.log4j.Logger;
import org.dspace.content.Collection;
import org.dspace.content.DSpaceObject;
import org.dspace.core.*;
import org.dspace.event.Consumer;
import org.dspace.event.Event;
import org.dspace.harvest.factory.HarvestServiceFactory;
import org.dspace.harvest.service.HarvestedCollectionService;
import org.dspace.harvest.service.HarvestedItemService;

/**
 * Class for handling cleanup of harvest settings for collections and items
 *
 *
 * @version $Revision: 3705 $
 *
 * @author Stuart Lewis
 * @author Alexey Maslov
 */
public class HarvestConsumer implements Consumer
{
    /** log4j logger */
    private static Logger log = Logger.getLogger(HarvestConsumer.class);

    private HarvestedItemService harvestedItemService = HarvestServiceFactory.getInstance().getHarvestedItemService();
    private HarvestedCollectionService harvestedCollectionService = HarvestServiceFactory.getInstance().getHarvestedCollectionService();

    /**
     * Initialise the consumer
     *
     * @throws Exception
     */
    public void initialize()
        throws Exception
    {

    }

    /**
     * Consume the event
     *
     * @param context
     * @param event
     * @throws Exception
     */
    public void consume(Context context, Event event)
        throws Exception
    {
	    int et = event.getEventType();
        DSpaceObject dso = event.getSubject(context);
        if(dso == null)
        {
            return;
        }

	    switch (dso.getType())
	    {
            case Constants.ITEM:
	            if (et == Event.DELETE)
	            {
	            	HarvestedItem hi = harvestedItemService.find(context, (org.dspace.content.Item) dso);
	            	if (hi != null) {
	            		log.debug("Deleted item '" + dso.getID() + "', also deleting associated harvested_item '" + hi.getOaiId() + "'.");
                        harvestedItemService.delete(context, hi);
	            	}
	            	else
                    {
                        log.debug("Deleted item '" + dso.getID() + "' and the associated harvested_item.");
                    }
	            } 
	            break;
	        case Constants.COLLECTION:
	        	if (et == Event.DELETE)
	            {
	        		HarvestedCollection hc = harvestedCollectionService.findByCollection(context, (Collection) dso);
	            	if (hc != null) {
	            		log.debug("Deleted collection '" + dso.getID() + "', also deleting associated harvested_collection '" + hc.getOaiSource() + ":" + hc.getOaiSetId() + "'.");
	            		harvestedCollectionService.delete(context, hc);
	            	}
	            	else
                    {
                        log.debug("Deleted collection '" + dso.getID() + "' and the associated harvested_collection.");
                    }
	            }
	        default:
	            log.warn("consume() got unrecognized event: " + event.toString());
	    }
    }

    /**
     * Handle the end of the event
     *
     * @param ctx
     * @throws Exception
     */
    public void end(Context ctx)
        throws Exception
    {

    }

    /**
     * Finish the event
     *
     * @param ctx
     */
    public void finish(Context ctx)
    {

    }
}