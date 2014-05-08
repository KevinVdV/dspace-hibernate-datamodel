/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.checker;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.IteratorUtils;
import org.apache.log4j.Logger;
import org.dspace.content.Bitstream;
import org.dspace.content.DSpaceObject;
import org.dspace.content.service.BitstreamService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.factory.DSpaceServiceFactory;
import org.dspace.handle.service.HandleService;

/**
 * A BitstreamDispatcher that checks all the bitstreams contained within an
 * item, collection or community referred to by Handle.
 * 
 * @author Jim Downing
 * @author Grace Carpenter
 * @author Nathan Sarr
 * 
 */
public class HandleDispatcher implements BitstreamDispatcher
{

    /** Log 4j logger. */
    private static final Logger LOG = Logger.getLogger(HandleDispatcher.class);

    private Context context;

    /** Handle to retrieve bitstreams from. */
    private String handle = null;

    /** Has the type of object the handle refers to been determined. */
    private boolean init = false;

    /** the delegate to dispatch to. */
    private BitstreamDispatcher delegate = null;

    /**
     * Database access for retrieving bitstreams
     */
    protected BitstreamService bitstreamService = DSpaceServiceFactory.getInstance().getBitstreamService();
    protected HandleService handleService = DSpaceServiceFactory.getInstance().getHandleService();

    /**
     * Blanked off, no-op constructor.
     */
    private HandleDispatcher()
    {
    }

    /**
     * Main constructor.
     * 
     * @param hdl
     *            the handle to get bitstreams from.
     */
    public HandleDispatcher(Context context, String hdl)
    {
        this.context = context;
        this.handle = hdl;
    }

    /**
     * Private initialization routine.
     * 
     * @throws SQLException
     *             if database access fails.
     */
    private synchronized void init() throws SQLException {
        if (!init)
        {
            DSpaceObject dso = handleService.resolveToObject(context, handle);

            Iterator<Bitstream> bitstreams = null;

            switch (dso.getType())
            {
            case Constants.BITSTREAM:
                bitstreams = IteratorUtils.arrayIterator(dso);
                break;

            case Constants.ITEM:
                bitstreams = bitstreamService.findAllInItem(context, (org.dspace.content.Item) dso);
                break;

            case Constants.COLLECTION:
                bitstreams = bitstreamService.findAllInCollection(context, (org.dspace.content.Collection) dso);
                break;

            case Constants.COMMUNITY:
                bitstreams = bitstreamService.findAllInCommunity(context, (org.dspace.content.Community) dso);
                break;
            }

            delegate = new IteratorDispatcher(bitstreams);
            init = true;
        }
    }

    /**
     * Initializes this dispatcher on first execution.
     * 
     * @see org.dspace.checker.BitstreamDispatcher#next()
     */
    public Bitstream next() throws SQLException {
        if (!init)
        {
            init();
        }

        return delegate.next();
    }
}
