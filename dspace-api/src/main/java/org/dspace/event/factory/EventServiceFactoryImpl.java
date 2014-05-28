package org.dspace.event.factory;

import org.dspace.event.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 27/05/14
 * Time: 14:54
 */
public class EventServiceFactoryImpl extends EventServiceFactory {

    @Autowired(required = true)
    protected EventService eventService;

    @Override
    public EventService getEventService() {
        return eventService;
    }
}
