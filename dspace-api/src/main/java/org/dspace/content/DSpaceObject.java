package org.dspace.content;

import org.dspace.core.Constants;
import org.dspace.core.Context;

import javax.persistence.Transient;
import java.sql.SQLException;

/**
 * Created by kevin on 08/02/14.
 */
//TODO: IMPLEMENT EQUALS/HASHCODE HERE ?
public abstract class DSpaceObject {

    // accumulate information to add to "detail" element of content Event,
    // e.g. to document metadata fields touched, etc.
    @Transient
    private StringBuilder eventDetails = null;

    /**
     * Get the internal ID (database primary key) of this object
     *
     * @return internal ID of object
     */
    public abstract int getID();

    /**
     * Get the Handle of the object. This may return <code>null</code>
     *
     * @return Handle of the object, or <code>null</code> if it doesn't have
     *         one
     */
    public String getHandle(Context context) throws SQLException
    {
        return null;
    }

    /**
     * Get a proper name for the object. This may return <code>null</code>.
     * Name should be suitable for display in a user interface.
     *
     * @return Name for the object, or <code>null</code> if it doesn't have
     *         one
     */
    public abstract String getName();

    /**
     * Get the type of this object, found in Constants
     *
     * @return type of the object
     */
    public abstract int getType();

    /**
     * Add a string to the cache of event details.  Automatically
     * separates entries with a comma.
     * Subclass can just start calling addDetails, since it creates
     * the cache if it needs to.
     * @param d detail string to add.
     */
    public void addDetails(String d)
    {
        if (eventDetails == null)
        {
            eventDetails = new StringBuilder(d);
        }
        else
        {
            eventDetails.append(", ").append(d);
        }
    }

    /**
     * Reset the cache of event details.
     */
    public void clearDetails()
    {
        eventDetails = null;
    }

    /**
     * @return summary of event details, or null if there are none.
     */
    public String getDetails()
    {
        return (eventDetails == null ? null : eventDetails.toString());
    }
}
