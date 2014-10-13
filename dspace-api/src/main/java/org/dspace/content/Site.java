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

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Represents the root of the DSpace Archive.
 * By default, the handle suffix "0" represents the Site, e.g. "1721.1/0"
 */
@Entity
@Table(name = "site", schema = "public")
public class Site extends DSpaceObject
{
    /** "database" identifier of the site */
    private static final HandleService HANDLE_SERVICE = HandleServiceFactory.getInstance().getHandleService();

    // cache for Handle that is persistent ID for entire site.
    @Transient
    private String handle = null;

    /**
     * Get the type of this object, found in Constants
     *
     * @return type of the object
     */
    @Override
    public int getType()
    {
        return Constants.SITE;
    }


    /**
     * Get the Handle of the object. This may return <code>null</code>
     *
     * @return Handle of the object, or <code>null</code> if it doesn't have
     *         one
     */
    @Override
    public String getHandle(Context context) throws SQLException {
        if (handle == null)
        {
            handle = HANDLE_SERVICE.getPrefix() + "/0";
        }
        return handle;

    }

    @Override
    public String getName()
    {
        return ConfigurationManager.getProperty("dspace.name");
    }

    public String getURL()
    {
        return ConfigurationManager.getProperty("dspace.url");
    }
}