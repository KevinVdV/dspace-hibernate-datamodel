/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authorize;

import java.sql.SQLException;
import java.util.Date;
import org.apache.log4j.Logger;

import org.dspace.content.DSpaceObject;
import org.dspace.content.DSpaceObjectDAO;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.EPersonDAO;
import org.dspace.eperson.Group;
import org.dspace.eperson.GroupDAO;
import org.dspace.hibernate.HibernateQueryUtil;

/**
 * Class representing a ResourcePolicy
 *
 * @author David Stuve
 * @version $Revision$
 */
public class ResourcePolicyDAO
{
    /** log4j logger */
    private static Logger log = Logger.getLogger(ResourcePolicyDAO.class);

    /**
     * Construct an ResourcePolicy
     */
    public ResourcePolicyDAO()
    {
    }



    /**
     * Get an ResourcePolicy from the database.
     *
     * @param context
     *            DSpace context object
     * @param id
     *            ID of the ResourcePolicy
     *
     * @return the ResourcePolicy format, or null if the ID is invalid.
     */
    public ResourcePolicy find(Context context, int id)
            throws SQLException
    {
        return (ResourcePolicy) context.getDBConnection().get(ResourcePolicy.class, id);
    }

    /**
     * Create a new ResourcePolicy
     *
     * @param context
     *            DSpace context object
     */
    public ResourcePolicy create(Context context) throws SQLException,
            AuthorizeException
    {
        // FIXME: Check authorisation
        // Create a table row
        ResourcePolicy resourcePolicy = new ResourcePolicy();
        HibernateQueryUtil.update(context, resourcePolicy);
        return resourcePolicy;
    }

    /**
     * Delete an ResourcePolicy
     *
     */
    public void delete(Context context, ResourcePolicy resourcePolicy) throws SQLException
    {
        if(resourcePolicy.getResourceID() != -1 && resourcePolicy.getResourceType() != -1)
        {
            //A policy for a DSpace Object has been modified, fire a modify event on the DSpace object
            DSpaceObject dso = DSpaceObjectDAO.find(context, resourcePolicy.getResourceType(), resourcePolicy.getResourceID());
            if(dso != null)
            {
                dso.updateLastModified(context);
            }
        }

        // FIXME: authorizations
        // Remove ourself
        HibernateQueryUtil.delete(context, resourcePolicy);
    }

    /**
     * set both type and id of resource referred to by policy
     *
     */
    public void setResource(ResourcePolicy resourcePolicy, DSpaceObject o)
    {
        resourcePolicy.setResourceType(o.getType());
        resourcePolicy.setResourceID(o.getID());
    }

    /**
     * @return action text or 'null' if action row empty
     */
    public String getActionText(ResourcePolicy resourcePolicy)
    {
        int myAction = resourcePolicy.getAction();

        if (myAction == -1)
        {
            return "...";
        }
        else
        {
            return Constants.actionText[myAction];
        }
    }

    /**
     * set Group for this policy
     *
     * @param g group
     */
    public void setGroup(ResourcePolicy resourcePolicy, Group g)
    {
        if (g != null)
        {
            resourcePolicy.setGroup(g);
        }
        else
        {
            resourcePolicy.setGroup(null);
        }
    }

    /**
     * figures out if the date is valid for the policy
     *
     * @return true if policy has begun and hasn't expired yet (or no dates are
     *         set)
     */
    public boolean isDateValid(ResourcePolicy resourcePolicy)
    {
        Date sd = resourcePolicy.getStartDate();
        Date ed = resourcePolicy.getEndDate();

        // if no dates set, return true (most common case)
        if ((sd == null) && (ed == null))
        {
            return true;
        }

        // one is set, now need to do some date math
        Date now = new Date();

        // check start date first
        if (sd != null && now.before(sd))
        {
            // start date is set, return false if we're before it
            return false;
        }

        // now expiration date
        if (ed != null && now.after(ed))
        {
            // end date is set, return false if we're after it
            return false;
        }

        // if we made it this far, start < now < end
        return true; // date must be okay
    }


    /**
     * Update the ResourcePolicy
     */
    public void update(Context context, ResourcePolicy resourcePolicy) throws SQLException, AuthorizeException {
        if(resourcePolicy.getResourceID() != -1 && resourcePolicy.getResourceType() != -1){
            //A policy for a DSpace Object has been modified, fire a modify event on the DSpace object
            DSpaceObject dso = DSpaceObjectDAO.find(context, resourcePolicy.getResourceType(), resourcePolicy.getResourceID());
            if(dso != null){
                dso.updateLastModified(context);
            }
        }

        // FIXME: Check authorisation
        HibernateQueryUtil.update(context, resourcePolicy);
    }
}