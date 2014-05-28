package org.dspace.event.factory;

import org.dspace.event.service.EventService;
import org.dspace.utils.DSpace;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 27/05/14
 * Time: 14:37
 */
public abstract class EventServiceFactory {

    public abstract EventService getEventService();

    public static EventServiceFactory getInstance()
    {
        return new DSpace().getServiceManager().getServiceByName("eventServiceFactory", EventServiceFactory.class);
    }
}
