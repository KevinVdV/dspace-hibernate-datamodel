/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

import org.dspace.authorize.AuthorizeException;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.handle.HandleServiceImpl;
import org.dspace.handle.factory.HandleServiceFactory;
import org.dspace.handle.service.HandleService;

/**
 * Represents the root of the DSpace Archive.
 * By default, the handle suffix "0" represents the Site, e.g. "1721.1/0"
 */
public class Site extends DSpaceObject
{
    /** "database" identifier of the site */
    //TODO: FIX THIS !
    public static final UUID SITE_ID = java.util.UUID.fromString("0");
    private static final HandleService HANDLE_SERVICE = HandleServiceFactory.getInstance().getHandleService();

    // cache for Handle that is persistent ID for entire site.
    private static String handle = null;

    private static Site theSite = null;

    /**
     * Get the type of this object, found in Constants
     *
     * @return type of the object
     */
    public int getType()
    {
        return Constants.SITE;
    }

    /**
     * Get the internal ID (database primary key) of this object
     *
     * @return internal ID of object
     */
    public java.util.UUID getID()
    {
        return UUID.fromString("SITE_ID");
    }

    /**
     * Get the Handle of the object. This may return <code>null</code>
     *
     * @return Handle of the object, or <code>null</code> if it doesn't have
     *         one
     */
    @Override
    public String getHandle(Context context) throws SQLException {
        return getSiteHandle();
    }

    /**
     * Static method to return site Handle without creating a Site.
     * @return handle of the Site.
     */
    //TODO: MAKE THIS NOT STATIC !
    public static String getSiteHandle()
    {
        if (handle == null)
        {
            handle = HANDLE_SERVICE.getPrefix() + "/" + String.valueOf(SITE_ID);
        }
        return handle;
    }

    /**
     * Get Site object corresponding to db id (which is ignored).
     * @param context the context.
     * @param id integer database id, ignored.
     * @return Site object.
     */
    public static DSpaceObject find(Context context, int id)
        throws SQLException
    {
        if (theSite == null)
        {
            theSite = new Site();
        }
        return theSite;
    }

    void delete()
        throws SQLException, AuthorizeException, IOException
    {
    }

    public void update()
        throws SQLException, AuthorizeException
    {
    }

    public String getName()
    {
        return ConfigurationManager.getProperty("dspace.name");
    }

    public String getURL()
    {
        return ConfigurationManager.getProperty("dspace.url");
    }
}