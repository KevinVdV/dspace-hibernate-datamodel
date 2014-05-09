/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.handle;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.dspace.content.*;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.factory.DSpaceServiceFactory;
import org.dspace.handle.dao.HandleDAO;
import org.dspace.handle.service.HandleService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Interface to the <a href="http://www.handle.net" target=_new>CNRI Handle
 * System </a>.
 *
 * <p>
 * Currently, this class simply maps handles to local facilities; handles which
 * are owned by other sites (including other DSpaces) are treated as
 * non-existent.
 * </p>
 *
 * @author Peter Breton
 * @version $Revision$
 */
public class HandleServiceImpl implements HandleService
{
    /** log4j category */
    private static Logger log = Logger.getLogger(HandleServiceImpl.class);

    /** Prefix registered to no one */
    static final String EXAMPLE_PREFIX = "123456789";

    @Autowired(required = true)
    protected HandleDAO handleDAO;

    /** Private Constructor */
    public HandleServiceImpl()
    {
    }

    /**
     * Return the local URL for handle, or null if handle cannot be found.
     *
     * The returned URL is a (non-handle-based) location where a dissemination
     * of the object referred to by handle can be obtained.
     *
     * @param context
     *            DSpace context
     * @param handle
     *            The handle
     * @return The local URL
     * @exception SQLException
     *                If a database error occurs
     */
    public String resolveToURL(Context context, String handle)
            throws SQLException
    {
        Handle dbhandle = findHandleInternal(context, handle);

        if (dbhandle == null)
        {
            return null;
        }

        String url = ConfigurationManager.getProperty("dspace.url")
                + "/handle/" + handle;

        if (log.isDebugEnabled())
        {
            log.debug("Resolved " + handle + " to " + url);
        }

        return url;
    }

    public void modifyHandleDSpaceObject(Context context, String handle, DSpaceObject newOwner) throws SQLException {
        Handle dbHandle = findHandleInternal(context, handle);
        if(dbHandle != null)
        {
            dbHandle.setResourceId(newOwner.getID());
            dbHandle.setResourceTypeId(newOwner.getType());
            handleDAO.save(context, dbHandle);
        }

    }
    
    /**
     * Try to detect a handle in a URL.
     * @param context DSpace context
     * @param url The URL
     * @return The handle or null if the handle couldn't be extracted of a URL
     * or if the extracted handle couldn't be found.
     * @throws SQLException  If a database error occurs
     */
    public String resolveUrlToHandle(Context context, String url)
            throws SQLException
    {
        String dspaceUrl = ConfigurationManager.getProperty("dspace.url")
                + "/handle/";
        String handleResolver = ConfigurationManager.getProperty("handle.canonical.prefix");
        
        String handle = null;
        
        if (url.startsWith(dspaceUrl))
        {
            handle = url.substring(dspaceUrl.length());
        }
        
        if (url.startsWith(handleResolver))
        {
            handle = url.substring(handleResolver.length());
        }
        
        if (null == handle)
        {
            return null;
        }
        
        // remove trailing slashes
        while (handle.startsWith("/"))
        {
            handle = handle.substring(1);
        }
        Handle dbhandle = findHandleInternal(context, handle);
        
        return (null == dbhandle) ? null : handle;
    }

    /**
     * Transforms handle into the canonical form <em>hdl:handle</em>.
     *
     * No attempt is made to verify that handle is in fact valid.
     *
     * @param handle
     *            The handle
     * @return The canonical form
     */
    public String getCanonicalForm(String handle)
    {

    	// Let the admin define a new prefix, if not then we'll use the
    	// CNRI default. This allows the admin to use "hdl:" if they want to or
    	// use a locally branded prefix handle.myuni.edu.
    	String handlePrefix = ConfigurationManager.getProperty("handle.canonical.prefix");
    	if (handlePrefix == null || handlePrefix.length() == 0)
    	{
    		handlePrefix = "http://hdl.handle.net/";
    	}

    	return handlePrefix + handle;
    }

    /**
     * Returns displayable string of the handle's 'temporary' URL
     * <em>http://hdl.handle.net/handle/em>.
     *
     * No attempt is made to verify that handle is in fact valid.
     *
     * @param handle The handle
     * @return The canonical form
     */

    //    public static String getURLForm(String handle)
    //    {
    //        return "http://hdl.handle.net/" + handle;
    //    }

    /**
     * Creates a new handle in the database.
     *
     * @param context
     *            DSpace context
     * @param dso
     *            The DSpaceObject to create a handle for
     * @return The newly created handle
     * @exception SQLException
     *                If a database error occurs
     */
    public String createHandle(Context context, DSpaceObject dso)
            throws SQLException
    {
        Handle handle = new Handle();
        //Update our changes so that we have an identifier
        handleDAO.save(context, handle);
        String handleId = createId(handle.getId());

        handle.setHandle(handleId);
        handle.setResourceTypeId(dso.getType());
        handle.setResourceId(dso.getID());
        handleDAO.save(context, handle);

        if (log.isDebugEnabled())
        {
            log.debug("Created new handle for "
                    + Constants.typeText[dso.getType()] + " (ID=" + dso.getID() + ") " + handleId );
        }

        return handleId;
    }

    /**
     * Creates a handle entry, but with a handle supplied by the caller (new
     * Handle not generated)
     *
     * @param context
     *            DSpace context
     * @param dso
     *            DSpaceObject
     * @param suppliedHandle
     *            existing handle value
     * @return the Handle
     * @throws IllegalStateException if specified handle is already in use by another object
     */
    public String createHandle(Context context, DSpaceObject dso, String suppliedHandle) throws SQLException, IllegalStateException
    {
        return createHandle(context, dso, suppliedHandle, false);
    }
    /**
     * Creates a handle entry, but with a handle supplied by the caller (new
     * Handle not generated)
     *
     * @param context
     *            DSpace context
     * @param dso
     *            DSpaceObject
     * @param suppliedHandle
     *            existing handle value
     * @return the Handle
     * @throws IllegalStateException if specified handle is already in use by another object
     */
    public String createHandle(Context context, DSpaceObject dso, String suppliedHandle, boolean force) throws SQLException, IllegalStateException
    {
        //Check if the supplied handle is already in use -- cannot use the same handle twice
        Handle handle = null;
        if(!force)
        {
            handle = findHandleInternal(context, suppliedHandle);
        }
        if(handle!=null && handle.getResourceId() != null)
        {
            //Check if this handle is already linked up to this specified DSpace Object
            if(handle.getResourceId()==dso.getID() &&
               handle.getResourceTypeId()==dso.getType())
            {
                //This handle already links to this DSpace Object -- so, there's nothing else we need to do
                return suppliedHandle;
            }
            else
            {
                //handle found in DB table & already in use by another existing resource
                throw new IllegalStateException("Attempted to create a handle which is already in use: " + suppliedHandle);
            }
        }
        else if(handle!=null && handle.getResourceTypeId() != null)
        {
            //If there is a 'resource_type_id' (but 'resource_id' is empty), then the object using
            // this handle was previously unbound (see unbindHandle() method) -- likely because object was deleted
            int previousType = handle.getResourceTypeId();

            //Since we are restoring an object to a pre-existing handle, double check we are restoring the same *type* of object
            // (e.g. we will not allow an Item to be restored to a handle previously used by a Collection)
            if(previousType != dso.getType())
            {
                throw new IllegalStateException("Attempted to reuse a handle previously used by a " +
                        Constants.typeText[previousType] + " for a new " +
                        Constants.typeText[dso.getType()]);
            }
        }
        else if(handle==null) //if handle not found, create it
        {
            //handle not found in DB table -- create a new table entry
            handle = handleDAO.create(context, new Handle());
            handle.setHandle(suppliedHandle);
        }

        handle.setResourceTypeId(dso.getType());
        handle.setResourceId(dso.getID());
        handleDAO.save(context, handle);

        if (log.isDebugEnabled())
        {
            log.debug("Created new handle for "
                    + Constants.typeText[dso.getType()] + " (ID=" + dso.getID() + ") " + suppliedHandle );
        }

        return suppliedHandle;
    }

    /**
     * Removes binding of Handle to a DSpace object, while leaving the
     * Handle in the table so it doesn't get reallocated.  The AIP
     * implementation also needs it there for foreign key references.
     *
     * @param context DSpace context
     * @param dso DSpaceObject whose Handle to unbind.
     */
    public void unbindHandle(Context context, DSpaceObject dso)
        throws SQLException
    {
        List<Handle> handles = getInternalHandles(context, dso.getType(), dso.getID());
        if (CollectionUtils.isNotEmpty(handles))
        {
            for (Handle handle: handles)
            {
                //Only set the "resouce_id" column to null when unbinding a handle.
                // We want to keep around the "resource_type_id" value, so that we
                // can verify during a restore whether the same *type* of resource
                // is reusing this handle!
                handle.setResourceId(null);
                handleDAO.save(context, handle);

                if(log.isDebugEnabled())
                {
                    log.debug("Unbound Handle " + handle.getHandle() + " from object " + Constants.typeText[dso.getType()] + " id=" + dso.getID());
                }
            }
        }
        else
        {
            log.warn("Cannot find Handle entry to unbind for object " + Constants.typeText[dso.getType()] + " id=" + dso.getID());
        }
    }

    /**
     * Return the object which handle maps to, or null. This is the object
     * itself, not a URL which points to it.
     *
     * @param context
     *            DSpace context
     * @param handle
     *            The handle to resolve
     * @return The object which handle maps to, or null if handle is not mapped
     *         to any object.
     * @exception IllegalStateException
     *                If handle was found but is not bound to an object
     * @exception SQLException
     *                If a database error occurs
     */
    public DSpaceObject resolveToObject(Context context, String handle)
            throws IllegalStateException, SQLException
    {
        Handle dbhandle = findHandleInternal(context, handle);

        if (dbhandle == null)
        {
            //If this is the Site-wide Handle, return Site object
            if (handle.equals(Site.getSiteHandle()))
            {
                return Site.find(context, 0);
            }
            //Otherwise, return null (i.e. handle not found in DB)
            return null;
        }

        // check if handle was allocated previously, but is currently not
        // associated with a DSpaceObject
        // (this may occur when 'unbindHandle()' is called for an obj that was removed)
        if ((dbhandle.getResourceTypeId() == null)
                || (dbhandle.getResourceId() == null))
        {
            //if handle has been unbound, just return null (as this will result in a PageNotFound)
            return null;
        }

        // What are we looking at here?
        int handletypeid = dbhandle.getResourceTypeId();
        int resourceID = dbhandle.getResourceId();

        DSpaceObject dso = DSpaceServiceFactory.getInstance().getDSpaceObjectService(handletypeid).find(context, resourceID);
        if(log.isDebugEnabled())
        {
            log.debug("Resolved handle " + handle + " to " + dso.getClass().getSimpleName()
                    + ((dso == null) ? (-1) : dso.getID()));

        }
        return dso;
    }

    /**
     * Return the handle for an Object, or null if the Object has no handle.
     *
     * @param context
     *            DSpace context
     * @param dso
     *            The object to obtain a handle for
     * @return The handle for object, or null if the object has no handle.
     * @exception SQLException
     *                If a database error occurs
     */
    public String findHandle(Context context, DSpaceObject dso)
            throws SQLException
    {
        List<Handle> handles = getInternalHandles(context, dso.getType(), dso.getID());
        if (CollectionUtils.isEmpty(handles))
        {
            if (dso.getType() == Constants.SITE)
            {
                return Site.getSiteHandle();
            }
            else
            {
                return null;
            }
        }
        else
        {
            //TODO: Move this code away from the HandleManager & into the Identifier provider
            //Attempt to retrieve a handle that does NOT look like {handle.part}/{handle.part}.{version}
            Iterator<Handle> handleIt = handles.iterator();
            String result = handleIt.next().getHandle();
            while (handleIt.hasNext()) {
                Handle handle = handleIt.next();
                //Ensure that the handle doesn't look like this 12346/213.{version}
                //If we find a match that indicates that we have a proper handle
                if(!handle.getHandle().matches(".*/.*\\.\\d+"))
                {
                    result = handle.getHandle();
                }
            }

            return result;
        }
    }

    /**
     * Return all the handles which start with prefix.
     *
     * @param context
     *            DSpace context
     * @param prefix
     *            The handle prefix
     * @return A list of the handles starting with prefix. The list is
     *         guaranteed to be non-null. Each element of the list is a String.
     * @exception SQLException
     *                If a database error occurs
     */
    public List<String> getHandlesForPrefix(Context context, String prefix)
            throws SQLException
    {
        List<Handle> handles = handleDAO.findByPrefix(context, prefix);
        List<String> handleStrings = new ArrayList<String>(handles.size());
        for (Handle handle : handles) {
            handleStrings.add(handle.getHandle());
        }
        return handleStrings;
    }

    /**
     * Get the configured Handle prefix string, or a default
     * @return configured prefix or "123456789"
     */
    public String getPrefix()
    {
        String prefix = ConfigurationManager.getProperty("handle.prefix");
        if (null == prefix)
        {
            prefix = EXAMPLE_PREFIX; // XXX no good way to exit cleanly
            log.error("handle.prefix is not configured; using " + prefix);
        }
        return prefix;
    }

    ////////////////////////////////////////
    // Internal methods
    ////////////////////////////////////////

    /**
     * Return the handle for an Object, or null if the Object has no handle.
     *
     * @param context
     *            DSpace context
     * @param type
     *            The type of object
     * @param id
     *            The id of object
     * @return The handle for object, or null if the object has no handle.
     * @exception SQLException
     *                If a database error occurs
     */
    protected List<Handle> getInternalHandles(Context context, int type, int id) throws SQLException
    {
        return handleDAO.getHandlesByTypeAndId(context, type, id);
    }

    /**
     * Find the database row corresponding to handle.
     *
     * @param context
     *            DSpace context
     * @param handle
     *            The handle to resolve
     * @return The database row corresponding to the handle
     * @exception SQLException
     *                If a database error occurs
     */
    protected Handle findHandleInternal(Context context, String handle)
            throws SQLException
    {
        if (handle == null)
        {
            throw new IllegalArgumentException("Handle is null");
        }

        return handleDAO.findByHandle(context, handle);
    }

    /**
     * Create a new handle id. The implementation uses the PK of the RDBMS
     * Handle table.
     *
     * @return A new handle id
     * @exception SQLException
     *                If a database error occurs
     */
    protected String createId(int id) throws SQLException
    {
        String handlePrefix = getPrefix();

        return new StringBuffer().append(handlePrefix).append(
                handlePrefix.endsWith("/") ? "" : "/").append(id).toString();
    }
}