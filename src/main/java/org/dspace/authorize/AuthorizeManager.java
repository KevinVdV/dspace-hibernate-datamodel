/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authorize;

import java.sql.SQLException;
import java.util.*;

import org.apache.commons.collections.CollectionUtils;
import org.dspace.content.*;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.eperson.GroupRepo;
import org.dspace.eperson.GroupRepoImpl;
import org.dspace.hibernate.HibernateQueryUtil;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

/**
 * AuthorizeManager handles all authorization checks for DSpace. For better
 * security, DSpace assumes that you do not have the right to do something
 * unless that permission is spelled out somewhere. That "somewhere" is the
 * ResourcePolicy table. The AuthorizeManager is given a user, an object, and an
 * action, and it then does a lookup in the ResourcePolicy table to see if there
 * are any policies giving the user permission to do that action.
 * <p>
 * ResourcePolicies now apply to single objects (such as submit (ADD) permission
 * to a collection.)
 * <p>
 * Note: If an eperson is a member of the administrator group (id 1), then they
 * are automatically given permission for all requests another special group is
 * group 0, which is anonymous - all EPeople are members of group 0.
 */
//TODO: SPLIT UP INTO DAO, MANAGER, .....
public class AuthorizeManager
{
    /**
     * Utility method, checks that the current user of the given context can
     * perform all of the specified actions on the given object. An
     * <code>AuthorizeException</code> if all the authorizations fail.
     *
     * @param c
     *         context with the current user
     * @param o
     *         DSpace object user is attempting to perform action on
     * @param actions
     *         array of action IDs from
     *         <code>org.dspace.core.Constants</code>
     * @throws AuthorizeException
     *         if any one of the specified actions cannot be performed by
     *         the current user on the given object.
     * @throws SQLException
     *         if there's a database problem
     */
    public static void authorizeAnyOf(Context c, DSpaceObject o, int[] actions)
            throws AuthorizeException, SQLException
    {
        AuthorizeException ex = null;

        for (int action : actions) {
            try {
                authorizeAction(c, o, action);

                return;
            } catch (AuthorizeException e) {
                if (ex == null) {
                    ex = e;
                }
            }
        }

        throw ex;
    }

    /**
     * Checks that the context's current user can perform the given action on
     * the given object. Throws an exception if the user is not authorized,
     * otherwise the method call does nothing.
     *
     * @param c
     *         context
     * @param o
     *         a DSpaceObject
     * @param action
     *         action to perform from <code>org.dspace.core.Constants</code>
     * @throws AuthorizeException
     *         if the user is denied
     */
    public static void authorizeAction(Context c, DSpaceObject o, int action)
            throws AuthorizeException, SQLException
    {
        authorizeAction(c, o, action, true);
    }

    /**
     * Checks that the context's current user can perform the given action on
     * the given object. Throws an exception if the user is not authorized,
     * otherwise the method call does nothing.
     *
     * @param c
     *         context
     * @param o
     *         a DSpaceObject
     * @param useInheritance
     *         flag to say if ADMIN action on the current object or parent
     *         object can be used
     * @param action
     *         action to perform from <code>org.dspace.core.Constants</code>
     * @throws AuthorizeException
     *         if the user is denied
     */
    public static void authorizeAction(Context c, DSpaceObject o, int action, boolean useInheritance)
            throws AuthorizeException, SQLException
    {
        if (o == null)
        {
            // action can be -1 due to a null entry
            String actionText;

            if (action == -1)
            {
                actionText = "null";
            } else
            {
                actionText = Constants.actionText[action];
            }

            EPerson e = c.getCurrentUser();
            int userid;

            if (e == null)
            {
                userid = 0;
            } else
            {
                userid = e.getID();
            }

            throw new AuthorizeException(
                    "Authorization attempted on null DSpace object "
                            + actionText + " by user " + userid);
        }

        if (!authorize(c, o, action, c.getCurrentUser(), useInheritance))
        {
            // denied, assemble and throw exception
            int otype = o.getType();
            int oid = o.getID();
            int userid;
            EPerson e = c.getCurrentUser();

            if (e == null)
            {
                userid = 0;
            } else
            {
                userid = e.getID();
            }

            //            AuthorizeException j = new AuthorizeException("Denied");
            //            j.printStackTrace();
            // action can be -1 due to a null entry
            String actionText;

            if (action == -1)
            {
                actionText = "null";
            } else
            {
                actionText = Constants.actionText[action];
            }

            throw new AuthorizeException("Authorization denied for action "
                    + actionText + " on " + Constants.typeText[otype] + ":"
                    + oid + " by user " + userid, o, action);
        }
    }

    /**
     * same authorize, returns boolean for those who don't want to deal with
     * catching exceptions.
     *
     * @param c
     *         DSpace context, containing current user
     * @param o
     *         DSpaceObject
     * @param a
     *         action being attempted, from
     *         <code>org.dspace.core.Constants</code>
     * @return <code>true</code> if the current user in the context is
     *         authorized to perform the given action on the given object
     */
    public static boolean authorizeActionBoolean(Context c, DSpaceObject o,
                                                 int a) throws SQLException
    {
        return authorizeActionBoolean(c, o, a, true);
    }

    /**
     * same authorize, returns boolean for those who don't want to deal with
     * catching exceptions.
     *
     * @param c
     *         DSpace context, containing current user
     * @param o
     *         DSpaceObject
     * @param a
     *         action being attempted, from
     *         <code>org.dspace.core.Constants</code>
     * @param useInheritance
     *         flag to say if ADMIN action on the current object or parent
     *         object can be used
     * @return <code>true</code> if the current user in the context is
     *         authorized to perform the given action on the given object
     */
    public static boolean authorizeActionBoolean(Context c, DSpaceObject o,
                                                 int a, boolean useInheritance) throws SQLException
    {
        boolean isAuthorized = true;

        if (o == null)
        {
            return false;
        }

        try
        {
            authorizeAction(c, o, a, useInheritance);
        } catch (AuthorizeException e)
        {
            isAuthorized = false;
        }

        return isAuthorized;
    }

    /**
     * Check to see if the given user can perform the given action on the given
     * object. Always returns true if the ignore authorization flat is set in
     * the current context.
     *
     * @param c
     *         current context. User is irrelevant; "ignore authorization"
     *         flag is relevant
     * @param o
     *         object action is being attempted on
     * @param action
     *         ID of action being attempted, from
     *         <code>org.dspace.core.Constants</code>
     * @param e
     *         user attempting action
     * @param useInheritance
     *         flag to say if ADMIN action on the current object or parent
     *         object can be used
     * @return <code>true</code> if user is authorized to perform the given
     *         action, <code>false</code> otherwise
     * @throws SQLException
     */
    private static boolean authorize(Context c, DSpaceObject o, int action,
                                     EPerson e, boolean useInheritance) throws SQLException
    {
        // return FALSE if there is no DSpaceObject
        if (o == null)
        {
            return false;
        }

        // is authorization disabled for this context?
        if (c.ignoreAuthorization())
        {
            return true;
        }

        // is eperson set? if not, userid = 0 (anonymous)
        int userid = 0;
        if (e != null)
        {
            userid = e.getID();

            // perform isAdmin check to see
            // if user is an Admin on this object
            //TODO: HIBERNATE: IMPLEMENT PARENT OBJECT
//            DSpaceObject testObject = useInheritance ? o.getAdminObject(action) : null;
//
//            if (isAdmin(c, testObject))
//            {
//                return true;
//            }
        }

        GroupRepo groupDAO = new GroupRepoImpl();
        ResourcePolicyRepo resourcePolicyDAO = new ResourcePolicyRepoImpl();
        for (ResourcePolicy rp : getPoliciesActionFilter(c, o, action))
        {
            // check policies for date validity
            if (resourcePolicyDAO.isDateValid(rp))
            {
                if (rp.getEPerson() != null && rp.getEPerson().getID() == userid)
                {
                    return true; // match
                }

                if ((rp.getGroup() != null)
                        && (groupDAO.isMember(c, rp.getGroup())))
                {
                    // group was set, and eperson is a member
                    // of that group
                    return true;
                }
            }
        }

        // default authorization is denial
        return false;
    }

    ///////////////////////////////////////////////
    // admin check methods
    ///////////////////////////////////////////////

    /**
     * Check to see if the current user is an Administrator of a given object
     * within DSpace. Always return <code>true</code> if the user is a System
     * Admin
     *
     * @param c
     *         current context
     * @param o
     *         current DSpace Object, if <code>null</code> the call will be
     *         equivalent to a call to the <code>isAdmin(Context c)</code>
     *         method
     * @return <code>true</code> if user has administrative privileges on the
     *         given DSpace object
     */
    public static boolean isAdmin(Context c, DSpaceObject o) throws SQLException
    {

        // return true if user is an Administrator
        if (isAdmin(c))
        {
            return true;
        }

        if (o == null)
        {
            return false;
        }

        // is eperson set? if not, userid = 0 (anonymous)
        int userid = 0;
        EPerson e = c.getCurrentUser();
        if (e != null)
        {
            userid = e.getID();
        }

        //
        // First, check all Resource Policies directly on this object
        //
        List<ResourcePolicy> policies = getPoliciesActionFilter(c, o, Constants.ADMIN);

        ResourcePolicyRepo policyDAO = new ResourcePolicyRepoImpl();
        GroupRepo groupDAO = new GroupRepoImpl();
        for (ResourcePolicy rp : policies)
        {
            // check policies for date validity
            if (policyDAO.isDateValid(rp))
            {
                if (rp.getEPerson() != null && rp.getEPerson().getID() == userid)
                {
                    return true; // match
                }

                if ((rp.getGroup() != null)
                        && (groupDAO.isMember(c, rp.getGroup())))
                {
                    // group was set, and eperson is a member
                    // of that group
                    return true;
                }
            }
        }

        // If user doesn't have specific Admin permissions on this object,
        // check the *parent* objects of this object.  This allows Admin
        // permissions to be inherited automatically (e.g. Admin on Community
        // is also an Admin of all Collections/Items in that Community)
        //TODO: HIBERNATE: IMPLEMENT PARENT OBJECT
//        DSpaceObject parent = o.getParentObject();
//        if (parent != null)
//        {
//            return isAdmin(c, parent);
//        }

        return false;
    }


    /**
     * Check to see if the current user is a System Admin. Always return
     * <code>true</code> if c.ignoreAuthorization is set. Anonymous users
     * can't be Admins (EPerson set to NULL)
     *
     * @param c
     *         current context
     * @return <code>true</code> if user is an admin or ignore authorization
     *         flag set
     */
    public static boolean isAdmin(Context c) throws SQLException
    {
        // if we're ignoring authorization, user is member of admin
        if (c.ignoreAuthorization())
        {
            return true;
        }

        EPerson e = c.getCurrentUser();

        if (e == null)
        {
            return false; // anonymous users can't be admins....
        } else
        {
            return new GroupRepoImpl().isMember(c, 1);
        }
    }

    ///////////////////////////////////////////////
    // policy manipulation methods
    ///////////////////////////////////////////////

    /**
     * Add a policy for an individual eperson
     *
     * @param c
     *         context. Current user irrelevant
     * @param o
     *         DSpaceObject to add policy to
     * @param actionID
     *         ID of action from <code>org.dspace.core.Constants</code>
     * @param e
     *         eperson who can perform the action
     * @throws AuthorizeException
     *         if current user in context is not authorized to add policies
     */
    public static void addPolicy(Context c, DSpaceObject o, int actionID,
                                 EPerson e) throws SQLException, AuthorizeException
    {
        addPolicy(c, o, actionID, e, null);
    }


    /**
     * Add a policy for an individual eperson
     *
     * @param context
     *         context. Current user irrelevant
     * @param o
     *         DSpaceObject to add policy to
     * @param actionID
     *         ID of action from <code>org.dspace.core.Constants</code>
     * @param e
     *         eperson who can perform the action
     * @param type
     *         policy type, deafult types are declared in the ResourcePolicy class
     * @throws AuthorizeException
     *         if current user in context is not authorized to add policies
     */
    public static void addPolicy(Context context, DSpaceObject o, int actionID,
                                 EPerson e, String type) throws SQLException, AuthorizeException
    {
        ResourcePolicyRepo policyDAO = new ResourcePolicyRepoImpl();
        ResourcePolicy rp = policyDAO.create(context);

        policyDAO.setResource(rp, o);
        rp.setAction(actionID);
        rp.setEPerson(e);
        rp.setRpType(type);

        policyDAO.update(context, rp);

        o.updateLastModified(context);
    }

    /**
     * Add a policy for a group
     *
     * @param c
     *         current context
     * @param o
     *         object to add policy for
     * @param actionID
     *         ID of action from <code>org.dspace.core.Constants</code>
     * @param g
     *         group to add policy for
     * @throws SQLException
     *         if there's a database problem
     * @throws AuthorizeException
     *         if the current user is not authorized to add this policy
     */
    public static void addPolicy(Context c, DSpaceObject o, int actionID,
                                 Group g) throws SQLException, AuthorizeException
    {
        addPolicy(c, o, actionID, g, null);
    }

    /**
     * Add a policy for a group
     *
     * @param c
     *         current context
     * @param o
     *         object to add policy for
     * @param actionID
     *         ID of action from <code>org.dspace.core.Constants</code>
     * @param g
     *         group to add policy for
     * @param type
     *         policy type, deafult types are declared in the ResourcePolicy class
     * @throws SQLException
     *         if there's a database problem
     * @throws AuthorizeException
     *         if the current user is not authorized to add this policy
     */
    public static void addPolicy(Context c, DSpaceObject o, int actionID,
                                 Group g, String type) throws SQLException, AuthorizeException
    {
        ResourcePolicyRepo policyDAO = new ResourcePolicyRepoImpl();
        ResourcePolicy rp = policyDAO.create(c);

        policyDAO.setResource(rp, o);
        rp.setAction(actionID);
        policyDAO.setGroup(rp, g);
        rp.setRpType(type);

        policyDAO.update(c, rp);

        o.updateLastModified(c);
    }

    /**
     * Return a List of the policies for an object
     *
     * @param c
     *         current context
     * @param o
     *         object to retrieve policies for
     * @return List of <code>ResourcePolicy</code> objects
     */
    public static List<ResourcePolicy> getPolicies(Context c, DSpaceObject o)
            throws SQLException
    {
        Criteria criteria = c.getDBConnection().createCriteria(ResourcePolicy.class);
        criteria.add(Restrictions.and(
                Restrictions.eq("resource_type_id", o.getType()),
                Restrictions.eq("resource_id", o.getID())
        ));
        return criteria.list();
    }


    /**
     * Return a List of the policies for an object
     *
     * @param c
     *         current context
     * @param o
     *         object to retrieve policies for
     * @return List of <code>ResourcePolicy</code> objects
     */
    public static List<ResourcePolicy> findPoliciesByDSOAndType(Context c, DSpaceObject o, String type)
            throws SQLException
    {
        Criteria criteria = c.getDBConnection().createCriteria(ResourcePolicy.class);
        criteria.add(Restrictions.and(
                Restrictions.eq("resource_type_id", o.getType()),
                Restrictions.eq("resource_id", o.getID()),
                Restrictions.eq("rptype", type)
        ));
        return criteria.list();
    }

    /**
     * Return a List of the policies for a group
     *
     * @param c
     *         current context
     * @param g
     *         group to retrieve policies for
     * @return List of <code>ResourcePolicy</code> objects
     */
    public static List<ResourcePolicy> getPoliciesForGroup(Context c, Group g)
            throws SQLException
    {
        Criteria criteria = c.getDBConnection().createCriteria(ResourcePolicy.class);
        criteria.add(Restrictions.eq("epersonGroup", g.getID()));
        return criteria.list();
    }

    /**
     * Return a list of policies for an object that match the action
     *
     * @param c
     *         context
     * @param o
     *         DSpaceObject policies relate to
     * @param actionID
     *         action (defined in class Constants)
     * @throws SQLException
     *         if there's a database problem
     */
    public static List<ResourcePolicy> getPoliciesActionFilter(Context c, DSpaceObject o,
                                                               int actionID) throws SQLException
    {
        Criteria criteria = c.getDBConnection().createCriteria(ResourcePolicy.class);
        criteria.add(Restrictions.and(
                Restrictions.eq("resource_type_id", o.getType()),
                Restrictions.eq("resource_id", o.getID()),
                Restrictions.eq("action_id", actionID)
        ));
        return criteria.list();
    }

    /**
     * Add policies to an object to match those from a previous object
     *
     * @param c
     *         context
     * @param src
     *         source of policies
     * @param dest
     *         destination of inherited policies
     * @throws SQLException
     *         if there's a database problem
     * @throws AuthorizeException
     *         if the current user is not authorized to add these policies
     */
    public static void inheritPolicies(Context c, DSpaceObject src,
                                       DSpaceObject dest) throws SQLException, AuthorizeException
    {
        // find all policies for the source object
        List<ResourcePolicy> policies = getPolicies(c, src);

        //Only inherit non-ADMIN policies (since ADMIN policies are automatically inherited)
        List<ResourcePolicy> nonAdminPolicies = new ArrayList<ResourcePolicy>();
        for (ResourcePolicy rp : policies)
        {
            if (rp.getAction() != Constants.ADMIN)
            {
                nonAdminPolicies.add(rp);
            }
        }
        addPolicies(c, nonAdminPolicies, dest);
    }

    /**
     * Copies policies from a list of resource policies to a given DSpaceObject
     *
     * @param c
     *         DSpace context
     * @param policies
     *         List of ResourcePolicy objects
     * @param dest
     *         object to have policies added
     * @throws SQLException
     *         if there's a database problem
     * @throws AuthorizeException
     *         if the current user is not authorized to add these policies
     */
    public static void addPolicies(Context c, List<ResourcePolicy> policies, DSpaceObject dest)
            throws SQLException, AuthorizeException
    {
        ResourcePolicyRepo resourcePolicyDAO = new ResourcePolicyRepoImpl();
        // now add them to the destination object
        for (ResourcePolicy srp : policies)
        {
            ResourcePolicy rp = resourcePolicyDAO.create(c);

            // copy over values
            resourcePolicyDAO.setResource(rp, dest);
            rp.setAction(srp.getAction());
            rp.setEPerson(srp.getEPerson());
            rp.setGroup(srp.getGroup());
            rp.setStartDate(srp.getStartDate());
            rp.setEndDate(srp.getEndDate());
            rp.setRpName(srp.getRpName());
            rp.setRpDescription(srp.getRpDescription());
            rp.setRpType(srp.getRpType());
            // and write out new policy
            resourcePolicyDAO.update(c, rp);
        }

        dest.updateLastModified(c);
    }

    /**
     * removes ALL policies for an object.  FIXME doesn't check authorization
     *
     * @param c
     *         DSpace context
     * @param o
     *         object to remove policies for
     * @throws SQLException
     *         if there's a database problem
     */
    public static void removeAllPolicies(Context c, DSpaceObject o)
            throws SQLException
    {
        o.updateLastModified(c);

        // FIXME: authorization check?
        Criteria criteria = c.getDBConnection().createCriteria(ResourcePolicy.class);
        criteria.add(
                Restrictions.and(
                        Restrictions.eq("resource_type_id", o.getType()),
                        Restrictions.eq("resource_id", o.getID())
                )
        );

        List<ResourcePolicy> resourcePolicy = criteria.list();
        for (ResourcePolicy policy : resourcePolicy) {
            HibernateQueryUtil.delete(c, policy);
        }
    }

    /**
     * removes ALL policies for an object that are not of the input type.
     *
     * @param c
     *         DSpace context
     * @param o
     *         object to remove policies for
     * @throws SQLException
     *         if there's a database problem
     */
    public static void removeAllPoliciesByDSOAndTypeNotEqualsTo(Context c, DSpaceObject o, String type)
            throws SQLException
    {
        Criteria criteria = c.getDBConnection().createCriteria(ResourcePolicy.class);
        List<ResourcePolicy> list = criteria.add(Restrictions.and(
                Restrictions.eq("resource_type_id", o.getType()),
                Restrictions.eq("resource_id", o.getID()),
                Restrictions.not(Restrictions.eq("rptype", type))
        )).list();
        for (int i = 0; i < list.size(); i++) {
            ResourcePolicy resourcePolicy = list.get(i);
            c.getDBConnection().delete(resourcePolicy);
        }

    }


    /**
     * removes policies
     *
     * @param c
     *         DSpace context
     * @param o
     *         object to remove policies for
     * @param type
     *         policy type
     * @throws SQLException
     *         if there's a database problem
     */
    public static void removeAllPoliciesByDSOAndType(Context c, DSpaceObject o, String type)
            throws SQLException
    {
        Criteria criteria = c.getDBConnection().createCriteria(ResourcePolicy.class);
        criteria.add(
                Restrictions.and(
                        Restrictions.eq("resource_type_id", o.getType()),
                        Restrictions.eq("resource_id", o.getID()),
                        Restrictions.eq("rptype", type)
                )
        );

        List<ResourcePolicy> resourcePolicy = criteria.list();
        for (ResourcePolicy policy : resourcePolicy) {
            HibernateQueryUtil.delete(c, policy);
        }
    }

    /**
     * Remove all policies from an object that match a given action. FIXME
     * doesn't check authorization
     *
     * @param context
     *         current context
     * @param dso
     *         object to remove policies from
     * @param actionID
     *         ID of action to match from
     *         <code>org.dspace.core.Constants</code>, or -1=all
     * @throws SQLException
     *         if there's a database problem
     */
    public static void removePoliciesActionFilter(Context context,
                                                  DSpaceObject dso, int actionID) throws SQLException
    {
        dso.updateLastModified(context);
        if (actionID == -1)
        {
            // remove all policies from object
            removeAllPolicies(context, dso);
        } else
        {
            Criteria criteria = context.getDBConnection().createCriteria(ResourcePolicy.class);
            criteria.add(
                    Restrictions.and(
                            Restrictions.eq("resource_type_id", dso.getType()),
                            Restrictions.eq("resource_id", dso.getID()),
                            Restrictions.eq("action_id", actionID)
                    )
            );

            List<ResourcePolicy> resourcePolicy = criteria.list();
            for (ResourcePolicy policy : resourcePolicy) {
                HibernateQueryUtil.delete(context, policy);
            }
        }
    }

    /**
     * Removes all policies relating to a particular group. FIXME doesn't check
     * authorization
     *
     * @param c
     *         current context
     * @param group
     *         ID of the group
     * @throws SQLException
     *         if there's a database problem
     */
    public static void removeGroupPolicies(Context c, Group group)
            throws SQLException
    {
        Criteria criteria = c.getDBConnection().createCriteria(ResourcePolicy.class);
        criteria.add(
                Restrictions.and(
                        Restrictions.eq("epersonGroup", group)
                )
        );

        List<ResourcePolicy> resourcePolicy = criteria.list();
        for (ResourcePolicy policy : resourcePolicy) {
            HibernateQueryUtil.delete(c, policy);
        }
    }

    /**
     * Removes all policies from a group for a particular object that belong to
     * a Group. FIXME doesn't check authorization
     *
     * @param c
     *         current context
     * @param o
     *         the object
     * @param g
     *         the group
     * @throws SQLException
     *         if there's a database problem
     */
    public static void removeGroupPolicies(Context c, DSpaceObject o, Group g)
            throws SQLException
    {
        o.updateLastModified(c);
        Criteria criteria = c.getDBConnection().createCriteria(ResourcePolicy.class);
        criteria.add(
                Restrictions.and(
                        Restrictions.eq("resource_type_id", o.getType()),
                        Restrictions.eq("resource_id", o.getID()),
                        Restrictions.eq("epersonGroup", g)
                )
        );

        List<ResourcePolicy> resourcePolicy = criteria.list();
        for (ResourcePolicy policy : resourcePolicy) {
            HibernateQueryUtil.delete(c, policy);
        }
    }

    /**
     * Removes all policies from an eperson for a particular object that belong to
     * an EPerson. FIXME doesn't check authorization
     *
     * @param c
     *         current context
     * @param o
     *         the object
     * @param e
     *         the eperson
     * @throws java.sql.SQLException
     *         if there's a database problem
     */
    public static void removeEPersonPolicies(Context c, DSpaceObject o, EPerson e)
            throws SQLException
    {
        o.updateLastModified(c);
        Criteria criteria = c.getDBConnection().createCriteria(ResourcePolicy.class);
        criteria.add(
                Restrictions.and(
                        Restrictions.eq("resource_type_id", o.getType()),
                        Restrictions.eq("resource_id", o.getID()),
                        Restrictions.eq("eperson", e)
                )
        );

        List<ResourcePolicy> resourcePolicy = criteria.list();
        for (ResourcePolicy policy : resourcePolicy) {
            HibernateQueryUtil.delete(c, policy);
        }
    }

    /**
     * Returns all groups authorized to perform an action on an object. Returns
     * empty array if no matches.
     *
     * @param c
     *         current context
     * @param o
     *         object
     * @param actionID
     *         ID of action frm <code>org.dspace.core.Constants</code>
     * @return array of <code>Group</code>s that can perform the specified
     *         action on the specified object
     * @throws java.sql.SQLException
     *         if there's a database problem
     */
    public static Group[] getAuthorizedGroups(Context c, DSpaceObject o,
                                              int actionID) throws java.sql.SQLException
    {
        List<ResourcePolicy> policies = getPoliciesActionFilter(c, o, actionID);

        List<Group> groups = new ArrayList<Group>();
        for (ResourcePolicy resourcePolicy : policies) {
            if(resourcePolicy.getGroup() != null)
            {
                groups.add(resourcePolicy.getGroup());
            }
        }
        return groups.toArray(new Group[groups.size()]);
    }


    public static boolean isAnIdenticalPolicyAlreadyInPlace(Context c, DSpaceObject o, ResourcePolicy rp) throws SQLException
    {
        return isAnIdenticalPolicyAlreadyInPlace(c, o.getType(), o.getID(), rp.getGroup(), rp.getAction(), rp.getID());
    }


    public static boolean isAnIdenticalPolicyAlreadyInPlace(Context c, DSpaceObject o, Group groupID, int action, int policyID) throws SQLException
    {
        return isAnIdenticalPolicyAlreadyInPlace(c, o.getType(), o.getID(), groupID, action, policyID);
    }

    public static boolean isAnIdenticalPolicyAlreadyInPlace(Context c, int dsoType, int dsoID, Group group, int action, int policyID) throws SQLException
    {
        Criteria criteria = c.getDBConnection().createCriteria(ResourcePolicy.class);
        criteria.add(Restrictions.and(
                Restrictions.eq("resource_type_id", dsoType),
                Restrictions.eq("resource_id", dsoID),
                Restrictions.eq("epersonGroup", group),
                Restrictions.eq("action_id", action)
        ));
        criteria.setMaxResults(1);


        List results;
        if (policyID != -1)
        {
            criteria.add(Restrictions.and(Restrictions.not(Restrictions.eq("id", action))));
            results = criteria.list();
        } else
        {
            results = criteria.list();
        }

        return CollectionUtils.isNotEmpty(results);

    }

    public static ResourcePolicy findByTypeIdGroupAction(Context c, int dsoType, int dsoID, Group group, int action, int policyID) throws SQLException
    {

        Criteria criteria = c.getDBConnection().createCriteria(ResourcePolicy.class);
        criteria.add(Restrictions.and(
                Restrictions.eq("resource_type_id", dsoType),
                Restrictions.eq("resource_id", dsoID),
                Restrictions.eq("epersonGroup", group),
                Restrictions.eq("action_id", action)
        ));
        criteria.setMaxResults(1);

        List<ResourcePolicy> results;
        if (policyID != -1)
        {
            criteria.add(Restrictions.and(Restrictions.not(Restrictions.eq("id", action))));
            results = criteria.list();
        } else
        {
            results = criteria.list();
        }

        if (CollectionUtils.isNotEmpty(results))
        {
            return results.get(0);
        }else{
            return null;
        }

    }


    /**
     * Generate Policies policies READ for the date in input adding reason. New policies are assigned automatically at the groups that
     * have right on the collection. E.g., if the anonymous can access the collection policies are assigned to anonymous.
     *
     * @param context
     * @param embargoDate
     * @param reason
     * @param dso
     * @param owningCollection
     * @throws SQLException
     * @throws AuthorizeException
     */
    public static void generateAutomaticPolicies(Context context, Date embargoDate,
                                                 String reason, DSpaceObject dso, org.dspace.content.Collection owningCollection) throws SQLException, AuthorizeException
    {

        if (embargoDate != null || (embargoDate == null && dso.getType() == Constants.BITSTREAM))
        {

            Group[] authorizedGroups = AuthorizeManager.getAuthorizedGroups(context, owningCollection, Constants.DEFAULT_ITEM_READ);

            AuthorizeManager.removeAllPoliciesByDSOAndType(context, dso, ResourcePolicy.TYPE_CUSTOM);

            // look for anonymous
            boolean isAnonymousInPlace = false;
            for (Group g : authorizedGroups)
            {
                if (g.getID() == 0)
                {
                    isAnonymousInPlace = true;
                }
            }
            ResourcePolicyRepo resourcePolicyDAO = new ResourcePolicyRepoImpl();
            if (!isAnonymousInPlace)
            {
                // add policies for all the groups
                for (Group g : authorizedGroups)
                {
                    ResourcePolicy rp = AuthorizeManager.createOrModifyPolicy(null, context, null, g, null, embargoDate, Constants.READ, reason, dso);
                    if (rp != null)
                        resourcePolicyDAO.update(context, rp);
                }

            } else
            {
                // add policy just for anonymous
                ResourcePolicy rp = AuthorizeManager.createOrModifyPolicy(null, context, null, null, null, embargoDate, Constants.READ, reason, dso);
                if (rp != null)
                    resourcePolicyDAO.update(context, rp);
            }
        }
    }


    public static ResourcePolicy createOrModifyPolicy(ResourcePolicy policy, Context context, String name, Group group, EPerson ePerson,
                                                      Date embargoDate, int action, String reason, DSpaceObject dso) throws AuthorizeException, SQLException
    {
        int policyID = -1;
        if (policy != null) policyID = policy.getID();

        // if an identical policy (same Action and same Group) is already in place modify it...
        ResourcePolicy policyTemp = AuthorizeManager.findByTypeIdGroupAction(context, dso.getType(), dso.getID(), group, action, policyID);
        if (policyTemp != null)
        {
            policy = policyTemp;
            policy.setRpType(ResourcePolicy.TYPE_CUSTOM);
        }

        ResourcePolicyRepo resourcePolicyDAO = new ResourcePolicyRepoImpl();
        if (policy == null)
        {
            policy = resourcePolicyDAO.create(context);
            policy.setResourceID(dso.getID());
            policy.setResourceType(dso.getType());
            policy.setAction(action);
            policy.setRpType(ResourcePolicy.TYPE_CUSTOM);
        }
        policy.setGroup(group);
        policy.setEPerson(ePerson);

        if (embargoDate != null)
        {
            policy.setStartDate(embargoDate);
        } else
        {
            policy.setStartDate(null);
            policy.setEndDate(null);
        }
        policy.setRpName(name);
        policy.setRpDescription(reason);
        return policy;
    }

}