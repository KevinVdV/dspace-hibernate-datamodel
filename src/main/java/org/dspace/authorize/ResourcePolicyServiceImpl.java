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
import java.util.List;

import org.apache.log4j.Logger;

import org.dspace.authorize.dao.ResourcePolicyDAO;
import org.dspace.authorize.service.ResourcePolicyService;
import org.dspace.content.DSpaceObject;
import org.dspace.content.service.DSpaceObjectService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.factory.DSpaceServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class representing a ResourcePolicy
 *
 * @author David Stuve
 * @version $Revision$
 */
public class ResourcePolicyServiceImpl implements ResourcePolicyService
{
    /** log4j logger */
    private static Logger log = Logger.getLogger(ResourcePolicyServiceImpl.class);

    @Autowired(required = true)
    protected DSpaceServiceFactory serviceFactory;

    @Autowired(required = true)
    protected ResourcePolicyDAO resourcePolicyDAO;

    /**
     * Construct an ResourcePolicy
     */
    public ResourcePolicyServiceImpl()
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
    public ResourcePolicy find(Context context, int id) throws SQLException
    {
        return resourcePolicyDAO.findByID(context, ResourcePolicy.class, id);
    }

    /**
     * Create a new ResourcePolicy
     *
     * @param context
     *            DSpace context object
     */
    public ResourcePolicy create(Context context) throws SQLException, AuthorizeException
    {
        // FIXME: Check authorisation
        // Create a table row
        ResourcePolicy resourcePolicy = new ResourcePolicy();
        resourcePolicyDAO.save(context, resourcePolicy);
        return resourcePolicy;
    }

    public List<ResourcePolicy> find(Context c, DSpaceObject o) throws SQLException
    {
        return resourcePolicyDAO.findByDso(c, o);
    }


    public List<ResourcePolicy> find(Context c, DSpaceObject o, String type) throws SQLException
    {
        return resourcePolicyDAO.findByDsoAndType(c, o, type);
    }

    public List<ResourcePolicy> find(Context context, Group group) throws SQLException {
        return resourcePolicyDAO.findByGroup(context, group);
    }

    public List<ResourcePolicy> find(Context c, DSpaceObject o, int actionId) throws SQLException
    {
        return resourcePolicyDAO.findByDSoAndAction(c, o, actionId);
    }

    public List<ResourcePolicy> find(Context c, int dsoType, int dsoID, Group group, int action, int notPolicyID) throws SQLException {
        return resourcePolicyDAO.findByTypeIdGroupAction(c, dsoType, dsoID, group, action, notPolicyID);
    }

    /**
     * Delete an ResourcePolicy
     *
     */
    public void delete(Context context, ResourcePolicy resourcePolicy) throws SQLException, AuthorizeException {
        if(resourcePolicy.getResourceID() != -1 && resourcePolicy.getResourceType() != -1)
        {
            //A policy for a DSpace Object has been modified, fire a modify event on the DSpace object
            DSpaceObject dso = serviceFactory.getDSpaceObjectService(resourcePolicy.getResourceType()).find(context, resourcePolicy.getResourceID());
            if(dso != null)
            {
                serviceFactory.getDSpaceObjectManager(dso).updateLastModified(context, dso);
            }
        }

        // FIXME: authorizations
        // Remove ourself
        resourcePolicyDAO.delete(context, resourcePolicy);
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

    public void removeAllPolicies(Context c, DSpaceObject o) throws SQLException, AuthorizeException {
        // FIXME: authorization check?
        serviceFactory.getDSpaceObjectManager(o).updateLastModified(c, o);
        resourcePolicyDAO.deleteByDso(c, o);
    }

    public void removePolicies(Context c, DSpaceObject o, String type) throws SQLException, AuthorizeException {
        serviceFactory.getDSpaceObjectManager(o).updateLastModified(c, o);
        resourcePolicyDAO.deleteByDsoAndType(c, o, type);
    }

    public void removeDsoGroupPolicies(Context context, DSpaceObject dso, Group group) throws SQLException, AuthorizeException {
        serviceFactory.getDSpaceObjectManager(dso).updateLastModified(context, dso);
        resourcePolicyDAO.deleteByDsoGroupPolicies(context, dso, group);
    }

    public void removeDsoEPersonPolicies(Context context, DSpaceObject dso, EPerson ePerson) throws SQLException, AuthorizeException {
        serviceFactory.getDSpaceObjectManager(dso).updateLastModified(context, dso);
        resourcePolicyDAO.deleteByDsoEPersonPolicies(context, dso, ePerson);

    }

    public void removeGroupPolicies(Context c, Group group) throws SQLException {
        resourcePolicyDAO.deleteByGroup(c, group);
    }

    public void removePolicies(Context c, DSpaceObject o, int actionId) throws SQLException, AuthorizeException {
        if (actionId == -1)
        {
            removeAllPolicies(c, o);
        }else{
            serviceFactory.getDSpaceObjectManager(o).updateLastModified(c, o);
            resourcePolicyDAO.deleteByDsoAndAction(c, o, actionId);
        }
    }

    public void removeDsoAndTypeNotEqualsToPolicies(Context c, DSpaceObject o, String type) throws SQLException, AuthorizeException {
        serviceFactory.getDSpaceObjectManager(o).updateLastModified(c, o);
        resourcePolicyDAO.deleteByDSOAndTypeNotEqualsTo(c, o, type);
    }


    /**
     * Update the ResourcePolicy
     */
    public void update(Context context, ResourcePolicy resourcePolicy) throws SQLException, AuthorizeException {
        if(resourcePolicy.getResourceID() != -1 && resourcePolicy.getResourceType() != -1){
            //A policy for a DSpace Object has been modified, fire a modify event on the DSpace object
            DSpaceObjectService dsoService = serviceFactory.getDSpaceObjectService(resourcePolicy.getResourceType());
            DSpaceObject dso = dsoService.find(context, resourcePolicy.getResourceID());
            if(dso != null){
                serviceFactory.getDSpaceObjectManager(dso).updateLastModified(context, dso);
            }
        }

        // FIXME: Check authorisation
        resourcePolicyDAO.save(context, resourcePolicy);
    }
}