/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import java.sql.SQLException;

import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.EPersonDAO;
import org.dspace.eperson.Group;
import org.dspace.eperson.GroupDAO;

/**
 * Abstract base class for DSpace objects
 */
public abstract class DSpaceObjectDAO<T extends DSpaceObject>
{
    // accumulate information to add to "detail" element of content Event,
    // e.g. to document metadata fields touched, etc.
    private StringBuffer eventDetails = null;

    /**
     * Reset the cache of event details.
     */
    protected void clearDetails()
    {
        eventDetails = null;
    }

    /**
     * Add a string to the cache of event details.  Automatically
     * separates entries with a comma.
     * Subclass can just start calling addDetails, since it creates
     * the cache if it needs to.
     * @param d detail string to add.
     */
    protected void addDetails(String d)
    {
        if (eventDetails == null)
        {
            eventDetails = new StringBuffer(d);
        }
        else
        {
            eventDetails.append(", ").append(d);
        }
    }

    /**
     * @return summary of event details, or null if there are none.
     */
    protected String getDetails()
    {
        return (eventDetails == null ? null : eventDetails.toString());
    }

    /**
     * Generic find for when the precise type of a DSO is not known, just the
     * a pair of type number and database ID.
     *
     * @param context - the context
     * @param type - type number
     * @param id - id within table of type'd objects
     * @return the object found, or null if it does not exist.
     * @throws SQLException only upon failure accessing the database.
     */
    public static DSpaceObject find(Context context, int type, int id)
            throws SQLException
    {
        switch (type)
        {
            //TODO: Hibernate
            //case Constants.BITSTREAM : return Bitstream.find(context, id);
            //case Constants.BUNDLE    : return Bundle.find(context, id);
            //case Constants.ITEM      : return Item.find(context, id);
            case Constants.COLLECTION: return new CollectionDAO().find(context, id);
            case Constants.COMMUNITY : return new CommunityDAO().find(context, id);
            case Constants.GROUP     : return new GroupDAO().find(context, id);
            case Constants.EPERSON   : return new EPersonDAO().find(context, id);
            case Constants.SITE      : return Site.find(context, id);
        }
        return null;
    }



    /**
     * Return the dspace object where an ADMIN action right is sufficient to
     * grant the initial authorize check.
     * <p>
     * Default behaviour is ADMIN right on the object grant right on all other
     * action on the object itself. Subclass should override this method as
     * needed.
     *
     * @param action
     *            ID of action being attempted, from
     *            <code>org.dspace.core.Constants</code>. The ADMIN action is
     *            not a valid parameter for this method, an
     *            IllegalArgumentException should be thrown
     * @return the dspace object, if any, where an ADMIN action is sufficient to
     *         grant the original action
     * @throws java.sql.SQLException
     * @throws IllegalArgumentException
     *             if the ADMIN action is supplied as parameter of the method
     *             call
     */
    public DSpaceObject getAdminObject(Context context, T dso, int action) throws SQLException
    {
        if (action == Constants.ADMIN)
        {
            throw new IllegalArgumentException("Illegal call to the DSpaceObject.getAdminObject method");
        }
        return dso;
    }

    /**
     * Return the dspace object that "own" the current object in the hierarchy.
     * Note that this method has a meaning slightly different from the
     * getAdminObject because it is independent of the action but it is in a way
     * related to it. It defines the "first" dspace object <b>OTHER</b> then the
     * current one, where allowed ADMIN actions imply allowed ADMIN actions on
     * the object self.
     *
     * @return the dspace object that "own" the current object in
     *         the hierarchy
     * @throws SQLException
     */
    public DSpaceObject getParentObject(Context context, T dso) throws SQLException
    {
        return null;
    }

    public abstract void update(Context context, T dso) throws SQLException, AuthorizeException;


    /**
     * Provide the text name of the type of this DSpaceObject. It is most likely all uppercase.
     * @return Object type as text
     */
    public String getTypeText(DSpaceObject dso)
    {
        return Constants.typeText[dso.getType()];
    }
}