/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.eperson;

import java.sql.SQLException;
import java.util.*;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.DSpaceObject;
import org.dspace.content.DSpaceObjectDAO;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.event.Event;
import org.dspace.hibernate.HibernateQueryUtil;

/**
 * Class representing a group of e-people.
 *
 * @author David Stuve
 * @version $Revision$
 */
public class GroupDAO extends DSpaceObjectDAO
{
    // findAll sortby types
    public static final int ID = 0; // sort by ID

    public static final int NAME = 1; // sort by NAME (default)

    /** log4j logger */
    private static Logger log = Logger.getLogger(GroupDAO.class);

    /**
     * Construct a Group from a given context and tablerow
     */
    public GroupDAO() throws SQLException
    {
    }

    /**
     * Create a new group
     *
     * @param context
     *            DSpace context object
     */
    public Group create(Context context) throws SQLException,
            AuthorizeException
    {
        // FIXME - authorization?
        //TODO: HIBERNATE WHEN AUTHORIZE MANAGER IS READY
        if (!AuthorizeManager.isAdmin(context))
        {
            throw new AuthorizeException(
                    "You must be an admin to create an EPerson Group");
        }

        // Create a table row
        Group g = new Group();
        HibernateQueryUtil.update(context, g);

        log.info(LogManager.getHeader(context, "create_group", "group_id="
                + g.getID()));

        context.addEvent(new Event(Event.CREATE, Constants.GROUP, g.getID(), null));

        return g;
    }


    /**
     * add an eperson member
     *
     * @param e
     *            eperson
     */
    public void addMember(Context context, Group groupEntity, EPerson e)
    {
        if (isMember(groupEntity, e))
        {
            return;
        }
        groupEntity.addMember(e);
        context.addEvent(new Event(Event.ADD, Constants.GROUP, groupEntity.getID(), Constants.EPERSON, e.getID(), e.getEmail()));
    }

    /**
     * add group to this group
     *
     * @param groupParent the group to which we add the group
     */
    public void addMember(Context context, Group groupParent, Group groupChild)
    {
        // don't add if it's already a member
        // and don't add itself
        if (groupParent.contains(groupChild) || groupParent.getID()==groupChild.getID())
        {
            return;
        }

        groupParent.addMember(groupChild);
        //TODO: HIBERNATE IMPLEMENT ?
        // groupsChanged = true;

        context.addEvent(new Event(Event.ADD, Constants.GROUP, groupParent.getID(), Constants.GROUP, groupChild.getID(), groupChild.getName()));
    }

    /**
     * remove an eperson from a group
     *
     * @param owningGroup
     */
    public void removeMember(Context context, Group owningGroup, EPerson childPerson)
    {
        if (owningGroup.remove(childPerson))
        {
            context.addEvent(new Event(Event.REMOVE, Constants.GROUP, owningGroup.getID(), Constants.EPERSON, childPerson.getID(), childPerson.getEmail()));
        }
    }

    /**
     * remove group from this group
     *
     * @param owningGroup
     */
    public void removeMember(Context context, Group owningGroup, Group childGroup)
    {
        if (owningGroup.remove(childGroup))
        {
            //TODO: HIBERNATE IMPLEMENT ?
            // groupsChanged = true;
            context.addEvent(new Event(Event.REMOVE, Constants.GROUP, owningGroup.getID(), Constants.GROUP, childGroup.getID(), childGroup.getName()));
        }
    }

    /**
     * check to see if an eperson is a direct member.
     * If the eperson is a member via a subgroup will be returned <code>false</code>
     *
     * @param e
     *            eperson to check membership
     */
    public boolean isMember(Group group, EPerson e)
    {
        // special, group 0 is anonymous
        return group.getID() == 0 || group.contains(e);
    }

    /**
     * Check to see if g is a direct group member.
     * If g is a subgroup via another group will be returned <code>false</code>
     *
     * @param owningGroup
     *            group to check
     */
    public boolean isMember(Group owningGroup, Group childGroup)
    {
        return owningGroup.contains(childGroup);
    }

    /**
     * fast check to see if an eperson is a member called with eperson id, does
     * database lookup without instantiating all of the epeople objects and is
     * thus a static method
     *
     * @param c
     *            context
     * @param groupid
     *            group ID to check
     */
    public boolean isMember(Context c, int groupid) throws SQLException
    {
        // special, everyone is member of group 0 (anonymous)
        if (groupid == 0)
        {
            return true;
        }

        EPerson currentuser = c.getCurrentUser();

        return epersonInGroup(c, groupid, currentuser);
    }

    /**
     * Get all of the groups that an eperson is a member of.
     *
     * @param c
     * @param e
     * @throws SQLException
     */
    public Group[] allMemberGroups(Context c, EPerson e)
            throws SQLException
    {
        List<Group> groupList = new ArrayList<Group>();

        Set<Integer> myGroups = allMemberGroupIDs(c, e);
        // now convert those Integers to Groups

        for (Integer myGroup : myGroups) {
            groupList.add(find(c, myGroup));
        }

        return groupList.toArray(new Group[groupList.size()]);
    }

    /**
     * get Set of Integers all of the group memberships for an eperson
     *
     * @param c
     * @param e
     * @return Set of Integer groupIDs
     * @throws SQLException
     */
    public Set<Integer> allMemberGroupIDs(Context c, EPerson e)
            throws SQLException
    {
        Set<Integer> groupIDs = new HashSet<Integer>();

        if (e != null)
        {
            // two queries - first to get groups eperson is a member of
            // second query gets parent groups for groups eperson is a member of
            Map<String, String> params = new HashMap<String, String>();
            params.put("eperson_id", String.valueOf(e.getID()));
            List<Group> groupEntities = HibernateQueryUtil.searchQuery(c, Group.class, params, null);
            Set<Integer> result = new LinkedHashSet<Integer>();
            for (Group groupEntity : groupEntities) {
                result.add(groupEntity.getID());
            }
            return result;
        }
        // Also need to get all "Special Groups" user is a member of!
        // Otherwise, you're ignoring the user's membership to these groups!
        // However, we only do this is we are looking up the special groups
        // of the current user, as we cannot look up the special groups
        // of a user who is not logged in.
        if ((c.getCurrentUser() == null) || (((c.getCurrentUser() != null) && (c.getCurrentUser().getID() == e.getID()))))
        {
            Group[] specialGroups = c.getSpecialGroups();
            for(Group special : specialGroups)
            {
                groupIDs.add(special.getID());
            }
        }

        // all the users are members of the anonymous group 
        groupIDs.add(0);

        // now we have all owning groups, also grab all parents of owning groups
        // yes, I know this could have been done as one big query and a union,
        // but doing the Oracle port taught me to keep to simple SQL!

        StringBuilder groupQuery = new StringBuilder();
        groupQuery.append("SELECT * FROM group2groupcache WHERE ");

        Iterator<Integer> i = groupIDs.iterator();

        // Build a list of query parameters
        Object[] parameters = new Object[groupIDs.size()];
        int idx = 0;
        while (i.hasNext())
        {
            int groupID = (i.next()).intValue();

            parameters[idx++] = Integer.valueOf(groupID);

            groupQuery.append("child_id= ? ");
            if (i.hasNext())
            {
                groupQuery.append(" OR ");
            }
        }

        // was member of at least one group
        // NOTE: even through the query is built dynamically, all data is
        // separated into the parameters array.
        //TODO: HIBERNATE IMPLEMENT
        /*
        TableRowIterator tri = DatabaseManager.queryTable(c, "group2groupcache",
                groupQuery.toString(),
                parameters);

        try
        {
            while (tri.hasNext())
            {
                TableRow row = tri.next();

                int parentID = row.getIntColumn("parent_id");

                groupIDs.add(Integer.valueOf(parentID));
            }
        }
        finally
        {
            // close the TableRowIterator to free up resources
            if (tri != null)
            {
                tri.close();
            }
        }
        */
        return groupIDs;
    }


    /**
     * Get all of the epeople who are a member of the
     * specified group, or a member of a sub-group of the
     * specified group, etc.
     *
     * @param c
     *          DSpace context
     * @param g
     *          Group object
     * @return   Array of EPerson objects
     * @throws SQLException
     */
    public EPerson[] allMembers(Context c, GroupDAO g)
            throws SQLException
    {
        List<EPerson> epersonList = new ArrayList<EPerson>();

        //TODO: HIBERNATE IMPLEMENT
        /*
        Set<Integer> myEpeople = allMemberIDs(c, g);
        // now convert those Integers to EPerson objects

        for (Integer aMyEpeople : myEpeople) {
            epersonList.add(EPerson.find(c, aMyEpeople));
        }
        */
        return epersonList.toArray(new EPerson[epersonList.size()]);
    }

    /**
     * Get Set of all Integers all of the epeople
     * members for a group
     *
     * @param c
     *          DSpace context
     * @param g
     *          Group object
     * @return Set of Integer epersonIDs
     * @throws SQLException
     */
    //TODO: HIBERNATE IMPLEMENT
    /*
    public Set<Integer> allMemberIDs(Context c, Group g)
            throws SQLException
    {
        // two queries - first to get all groups which are a member of this group
        // second query gets all members of each group in the first query
        Set<Integer> epeopleIDs = new HashSet<Integer>();

        // Get all groups which are a member of this group
        TableRowIterator tri = DatabaseManager.queryTable(c, "group2groupcache",
                "SELECT * FROM group2groupcache WHERE parent_id= ? ",
                g.getID());

        Set<Integer> groupIDs = new HashSet<Integer>();

        try
        {
            while (tri.hasNext())
            {
                TableRow row = tri.next();

                int childID = row.getIntColumn("child_id");

                groupIDs.add(Integer.valueOf(childID));
            }
        }
        finally
        {
            // close the TableRowIterator to free up resources
            if (tri != null)
            {
                tri.close();
            }
        }

        // now we have all the groups (including this one)
        // it is time to find all the EPeople who belong to those groups
        // and filter out all duplicates

        Object[] parameters = new Object[groupIDs.size()+1];
        int idx = 0;
        Iterator<Integer> i = groupIDs.iterator();

        // don't forget to add the current group to this query!
        parameters[idx++] = Integer.valueOf(g.getID());

        StringBuilder epersonQuery = new StringBuilder();
        epersonQuery.append("SELECT * FROM epersongroup2eperson WHERE ");
        epersonQuery.append("eperson_group_id= ? ");

        if (i.hasNext())
        {
            epersonQuery.append(" OR ");
        }

        while (i.hasNext())
        {
            int groupID = (i.next()).intValue();
            parameters[idx++] = Integer.valueOf(groupID);

            epersonQuery.append("eperson_group_id= ? ");
            if (i.hasNext())
            {
                epersonQuery.append(" OR ");
            }
        }

        //get all the EPerson IDs
        // Note: even through the query is dynamically built all data is separated
        // into the parameters array.
        tri = DatabaseManager.queryTable(c, "epersongroup2eperson",
                epersonQuery.toString(),
                parameters);

        try
        {
            while (tri.hasNext())
            {
                TableRow row = tri.next();

                int epersonID = row.getIntColumn("eperson_id");

                epeopleIDs.add(Integer.valueOf(epersonID));
            }
        }
        finally
        {
            // close the TableRowIterator to free up resources
            if (tri != null)
            {
                tri.close();
            }
        }

        return epeopleIDs;
    }
    */
    private boolean epersonInGroup(Context c, int groupID, EPerson e)
            throws SQLException
    {
        Set<Integer> groupIDs = allMemberGroupIDs(c, e);

        return groupIDs.contains(Integer.valueOf(groupID));
    }

    /**
     * find the group by its ID
     *
     * @param context
     * @param id
     */
    public Group find(Context context, int id) throws SQLException
    {
        // First check the cache
        Group fromCache = (Group) context.fromCache(Group.class, id);

        if (fromCache != null)
        {
            return fromCache;
        }

        return (Group) context.getDBConnection().get(Group.class, id);
    }

    /**
     * Find the group by its name - assumes name is unique
     *
     * @param context
     * @param name
     *
     * @return the named Group, or null if not found
     */
    public Group findByName(Context context, String name)
            throws SQLException
    {
        if(name == null)
        {
            return null;
        }
        return (Group) HibernateQueryUtil.findByUnique(context, Group.class, "name", name);
    }

    /**
     * Finds all groups in the site
     *
     * @param context
     *            DSpace context
     * @param sortField
     *            field to sort by -- Group.ID or Group.NAME
     *
     * @return array of all groups in the site
     */
    public Group[] findAll(Context context, int sortField)
            throws SQLException
    {
        String s;

        switch (sortField)
        {
            case ID:
                s = "eperson_group_id";

                break;

            case NAME:
                s = "name";

                break;

            default:
                s = "name";
        }

        Map<String, String> order = new LinkedHashMap<String, String>();
        order.put(s, "asc");

        List<Group> objects = HibernateQueryUtil.searchQuery(context, Group.class, MapUtils.EMPTY_MAP, order);
        return objects.toArray(new Group[objects.size()]);
    }

    /**
     * Find the groups that match the search query across eperson_group_id or name
     *
     * @param context
     *            DSpace context
     * @param query
     *            The search string
     * @param offset
     *            Inclusive offset 
     * @param limit
     *            Maximum number of matches returned
     *
     * @return array of Group objects
     */
    public static Group[] search(Context context, String query, int offset, int limit)
            throws SQLException
    {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("eperson_group_id", query);
        parameters.put("name", "%"+query.toLowerCase()+"%");

        List<Group> objects = HibernateQueryUtil.searchQuery(context, Group.class, parameters, MapUtils.EMPTY_MAP, offset, limit);
        return objects.toArray(new Group[objects.size()]);
    }

    /**
     * Returns the total number of groups returned by a specific query, without the overhead 
     * of creating the Group objects to store the results.
     *
     * @param context
     *            DSpace context
     * @param query
     *            The search string
     *
     * @return the number of groups matching the query
     */
    public static int searchResultCount(Context context, String query)
            throws SQLException
    {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("eperson_group_id", query);
        parameters.put("name", "%"+query.toLowerCase()+"%");

        return HibernateQueryUtil.searchQueryCount(context, Group.class, parameters);
    }


    /**
     * Delete a group
     *
     */
    public void delete(Context context, Group groupEntity) throws SQLException
    {
        // FIXME: authorizations

        context.addEvent(new Event(Event.DELETE, Constants.GROUP, groupEntity.getID(), groupEntity.getName()));

        // Remove from cache
        context.removeCached(this, groupEntity.getID());

        // Remove any ResourcePolicies that reference this group
        AuthorizeManager.removeGroupPolicies(context, groupEntity.getID());

        // don't forget the new table
        //TODO: HIBERNATE IMPLEMENT
//        deleteEpersonGroup2WorkspaceItem();

        // Remove ourself
        HibernateQueryUtil.delete(context, groupEntity);

        log.info(LogManager.getHeader(context, "delete_group", "group_id="
                + groupEntity.getID()));
    }

    /**
     * @throws SQLException
     */
    //TODO: HIBERNATE IMPLEMENT
/*
    private void deleteEpersonGroup2WorkspaceItem() throws SQLException
    {
        DatabaseManager.updateQuery(myContext,
                "DELETE FROM EPersonGroup2WorkspaceItem WHERE eperson_group_id= ? ",
                getID());
    }
*/
    /**
     * Return true if group has no direct or indirect members
     */
    public boolean isEmpty(Group groupEntity)
    {
        // the only fast check available is on epeople...
        boolean hasMembers = (groupEntity.getEpeople().size() != 0);

        if (hasMembers)
        {
            return false;
        }
        else
        {
            // well, groups is never null...
            for (Group subGroup : groupEntity.getGroups()){
                hasMembers = !isEmpty(subGroup);
                if (hasMembers){
                    return false;
                }
            }
            return !hasMembers;
        }
    }

    /**
     * Update the group - writing out group object and EPerson list if necessary
     */
    public void update(Context context, DSpaceObject dSpaceObject) throws SQLException, AuthorizeException
    {
        // FIXME: Check authorisation
        HibernateQueryUtil.update(context, dSpaceObject);

        if (((Group) dSpaceObject).isModifiedMetadata())
        {
            context.addEvent(new Event(Event.MODIFY_METADATA, Constants.GROUP, dSpaceObject.getID(), getDetails()));
            //TODO: HIBERNATE, move details to top
            clearDetails();
        }

        log.info(LogManager.getHeader(context, "update_group", "group_id="
                + dSpaceObject.getID()));
    }

    public int getType()
    {
        return Constants.GROUP;
    }

    /**
     * Used recursively to generate a map of ALL of the children of the given
     * parent
     *
     * @param parents
     *            Map of parent,child relationships
     * @param parent
     *            the parent you're interested in
     * @return Map whose keys are all of the children of a parent
     */
    private Set<Integer> getChildren(Map<Integer,Set<Integer>> parents, Integer parent)
    {
        Set<Integer> myChildren = new HashSet<Integer>();

        // degenerate case, this parent has no children
        if (!parents.containsKey(parent))
        {
            return myChildren;
        }

        // got this far, so we must have children
        Set<Integer> children =  parents.get(parent);

        // now iterate over all of the children
        Iterator<Integer> i = children.iterator();

        while (i.hasNext())
        {
            Integer childID = i.next();

            // add this child's ID to our return set
            myChildren.add(childID);

            // and now its children
            myChildren.addAll(getChildren(parents, childID));
        }

        return myChildren;
    }

    //TODO: HIBERNATE IMPLEMENT
/*
    public DSpaceObject getParentObject() throws SQLException
    {
        // could a collection/community administrator manage related groups?
        // check before the configuration options could give a performance gain
        // if all group management are disallowed
        if (AuthorizeConfiguration.canCollectionAdminManageAdminGroup()
                || AuthorizeConfiguration.canCollectionAdminManageSubmitters()
                || AuthorizeConfiguration.canCollectionAdminManageWorkflows()
                || AuthorizeConfiguration.canCommunityAdminManageAdminGroup()
                || AuthorizeConfiguration
                .canCommunityAdminManageCollectionAdminGroup()
                || AuthorizeConfiguration
                .canCommunityAdminManageCollectionSubmitters()
                || AuthorizeConfiguration
                .canCommunityAdminManageCollectionWorkflows())
        {
            // is this a collection related group?
            TableRow qResult = DatabaseManager
                    .querySingle(
                            myContext,
                            "SELECT collection_id, workflow_step_1, workflow_step_2, " +
                                    " workflow_step_3, submitter, admin FROM collection "
                                    + " WHERE workflow_step_1 = ? OR "
                                    + " workflow_step_2 = ? OR "
                                    + " workflow_step_3 = ? OR "
                                    + " submitter =  ? OR " + " admin = ?",
                            getID(), getID(), getID(), getID(), getID());
            if (qResult != null)
            {
                Collection collection = Collection.find(myContext, qResult
                        .getIntColumn("collection_id"));

                if ((qResult.getIntColumn("workflow_step_1") == getID() ||
                        qResult.getIntColumn("workflow_step_2") == getID() ||
                        qResult.getIntColumn("workflow_step_3") == getID()))
                {
                    if (AuthorizeConfiguration.canCollectionAdminManageWorkflows())
                    {
                        return collection;
                    }
                    else if (AuthorizeConfiguration.canCommunityAdminManageCollectionWorkflows())
                    {
                        return collection.getParentObject();
                    }
                }
                if (qResult.getIntColumn("submitter") == getID())
                {
                    if (AuthorizeConfiguration.canCollectionAdminManageSubmitters())
                    {
                        return collection;
                    }
                    else if (AuthorizeConfiguration.canCommunityAdminManageCollectionSubmitters())
                    {
                        return collection.getParentObject();
                    }
                }
                if (qResult.getIntColumn("admin") == getID())
                {
                    if (AuthorizeConfiguration.canCollectionAdminManageAdminGroup())
                    {
                        return collection;
                    }
                    else if (AuthorizeConfiguration.canCommunityAdminManageCollectionAdminGroup())
                    {
                        return collection.getParentObject();
                    }
                }
            }
            // is the group related to a community and community administrator allowed
            // to manage it?
            else if (AuthorizeConfiguration.canCommunityAdminManageAdminGroup())
            {
                qResult = DatabaseManager.querySingle(myContext,
                        "SELECT community_id FROM community "
                                + "WHERE admin = ?", getID());

                if (qResult != null)
                {
                    Community community = Community.find(myContext, qResult
                            .getIntColumn("community_id"));
                    return community;
                }
            }
        }
        return null;
    }
    */
}