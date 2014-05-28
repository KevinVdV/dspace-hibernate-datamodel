package org.dspace.event.service;

import org.dspace.event.Dispatcher;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 27/05/14
 * Time: 14:15
 */
public interface EventService {

    // The name of the default dispatcher assigned to every new context unless
    // overridden
    public static final String DEFAULT_DISPATCHER = "default";

    public Dispatcher getDispatcher(String name);

    public void returnDispatcher(String key, Dispatcher disp);

    public int getConsumerIndex(String consumerClass);
}
