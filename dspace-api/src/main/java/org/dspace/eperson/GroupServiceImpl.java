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

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeConfiguration;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.*;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.CommunityService;
import org.dspace.content.service.SupervisedItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.eperson.dao.Group2GroupCacheDAO;
import org.dspace.eperson.dao.Group2GroupDAO;
import org.dspace.eperson.dao.GroupDAO;
import org.dspace.eperson.service.EPersonService;
import org.dspace.eperson.service.GroupService;
import org.dspace.event.Event;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class representing a group of e-people.
 *
 * @author David Stuve
 * @version $Revision$
 */
public class GroupServiceImpl extends DSpaceObjectServiceImpl<Group> implements GroupService
{
    // findAll sortby types
    public static final int ID = 0; // sort by ID

    public static final int NAME = 1; // sort by NAME (default)

    /** log4j logger */
    private static Logger log = Logger.getLogger(GroupServiceImpl.class);

    @Autowired(required = true)
    protected GroupDAO groupDAO;

    @Autowired(required = true)
    protected Group2GroupDAO group2GroupDAO;

    @Autowired(required = true)
    protected Group2GroupCacheDAO group2GroupCacheDAO;

    @Autowired(required = true)
    protected CollectionService collectionService;

    @Autowired(required = true)
    protected EPersonService ePersonService;

    @Autowired(required = true)
    protected CommunityService communityService;

    @Autowired(required = true)
    protected SupervisedItemService supervisedItemService;

    /**
     * Construct a Group from a given context and tablerow
     */
    public GroupServiceImpl()
    {
    }

    /**
     * Create a new group
     *
     * @param context
     *            DSpace context object
     */
    @Override
    public Group create(Context context) throws SQLException, AuthorizeException
    {
        // FIXME - authorization?
        if (!AuthorizeManager.isAdmin(context))
        {
            throw new AuthorizeException(
                    "You must be an admin to create an EPerson Group");
        }

        // Create a table row
        Group g = groupDAO.create(context, new Group());

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
    @Override
    public void addMember(Context context, Group groupEntity, EPerson e)
    {
        if (isDirectMember(groupEntity, e))
        {
            return;
        }
        groupEntity.addMember(e);
        e.getGroups().add(groupEntity);
        context.addEvent(new Event(Event.ADD, Constants.GROUP, groupEntity.getID(), Constants.EPERSON, e.getID(), e.getEmail()));
    }

    /**
     * add group to this group
     *
     * @param groupParent the group to which we add the group
     */
    @Override
    public void addMember(Context context, Group groupParent, Group groupChild)
    {
        // don't add if it's already a member
        // and don't add itself
        if (groupParent.contains(groupChild) || groupParent.getID()==groupChild.getID())
        {
            return;
        }

        groupParent.addMember(groupChild);
        groupChild.addParentGroup(groupParent);

        context.addEvent(new Event(Event.ADD, Constants.GROUP, groupParent.getID(), Constants.GROUP, groupChild.getID(), groupChild.getName()));
    }

    /**
     * remove an eperson from a group
     *
     * @param owningGroup
     */
    @Override
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
    @Override
    public void removeMember(Context context, Group owningGroup, Group childGroup)
    {
        if (owningGroup.remove(childGroup))
        {
            childGroup.removeParentGroup(owningGroup);
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
    @Override
    public boolean isDirectMember(Group group, EPerson e)
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
    @Override
    public boolean isDirectMember(Group owningGroup, Group childGroup)
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
     * @param group
     *            group to check
     */
    @Override
    public boolean isMember(Context c, Group group) throws SQLException
    {
        if (group == null)
        {
            return false;
        }

        EPerson currentuser = c.getCurrentUser();
        return epersonInGroup(c, group, currentuser);
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
    @Override
    public boolean isMember(Context c, int groupid) throws SQLException
    {
        // special, everyone is member of group 0 (anonymous)
        if (groupid == 0)
        {
            return true;
        }

        EPerson currentuser = c.getCurrentUser();

        return epersonInGroup(c, find(c, groupid), currentuser);
    }

    /**
     * Get all of the groups that an eperson is a member of.
     *
     * @param c
     * @param e
     * @throws SQLException
     */
    @Override
    public List<Group> allMemberGroups(Context c, EPerson e) throws SQLException
    {
        Set<Group> groups = new HashSet<Group>();

        if (e != null)
        {
            // two queries - first to get groups eperson is a member of
            // second query gets parent groups for groups eperson is a member of
            groups.addAll(groupDAO.findByEPerson(c, e));
        }
        // Also need to get all "Special Groups" user is a member of!
        // Otherwise, you're ignoring the user's membership to these groups!
        // However, we only do this is we are looking up the special groups
        // of the current user, as we cannot look up the special groups
        // of a user who is not logged in.
        if ((c.getCurrentUser() == null) || (c.getCurrentUser().equals(e)))
        {
            List<Group> specialGroups = c.getSpecialGroups();
            for(Group special : specialGroups)
            {
                groups.add(special);
            }
        }

        // all the users are members of the anonymous group 
        groups.add(find(c, 0));


        List<Group2GroupCache> groupCache = group2GroupCacheDAO.findByChildren(c, groups);
        // now we have all owning groups, also grab all parents of owning groups
        // yes, I know this could have been done as one big query and a union,
        // but doing the Oracle port taught me to keep to simple SQL!
        for (Group2GroupCache group2GroupCache : groupCache) {
            groups.add(group2GroupCache.getParent());
        }

        return new ArrayList<Group>(groups);
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
    @Override
    public List<EPerson> allMembers(Context c, Group g) throws SQLException
    {
        // two queries - first to get all groups which are a member of this group
        // second query gets all members of each group in the first query

        // Get all groups which are a member of this group
        List<Group2GroupCache> group2GroupCaches = group2GroupCacheDAO.findByParent(c, g);
        Set<Group> groups = new HashSet<Group>();
        for (Group2GroupCache group2GroupCache : group2GroupCaches) {
            groups.add(group2GroupCache.getChild());
        }


        Set<EPerson> childGroupChildren = new HashSet<EPerson>(ePersonService.findByGroups(c, groups));
        //Don't forget to add our direct children
        childGroupChildren.addAll(g.getEpeople());

        return new ArrayList<EPerson>(childGroupChildren);
    }

    protected boolean epersonInGroup(Context c, Group group, EPerson e)
            throws SQLException
    {
        List<Group> groups = allMemberGroups(c, e);

        return groups.contains(group);
    }

    /**
     * find the group by its ID
     *
     * @param context
     * @param id
     */
    @Override
    public Group find(Context context, int id) throws SQLException
    {
        // First check the cache
        return groupDAO.findByID(context, Group.class, id);
    }

    @Override
    public String getName(Group dso) {
        return dso.getNameInternal();
    }

    /**
     * Find the group by its name - assumes name is unique
     *
     * @param context
     * @param name
     *
     * @return the named Group, or null if not found
     */
    @Override
    public Group findByName(Context context, String name) throws SQLException
    {
        if(name == null)
        {
            return null;
        }

        return groupDAO.findByName(context, name);

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
    @Override
    public List<Group> findAll(Context context, int sortField) throws SQLException
    {
        String s;
        switch (sortField)
        {
            case ID:
                s = "id";

                break;

            case NAME:
                s = "name";

                break;

            default:
                s = "name";
        }

        return groupDAO.findAll(context, s);
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
    @Override
    public List<Group> search(Context context, String query, int offset, int limit) throws SQLException
    {
        return groupDAO.search(context, query, offset, limit);
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
    @Override
    public int searchResultCount(Context context, String query) throws SQLException
    {
        return groupDAO.searchResultCount(context, query);
    }


    /**
     * Delete a group
     *
     */
    @Override
    public void delete(Context context, Group group) throws SQLException, AuthorizeException {
        // FIXME: authorizations

        context.addEvent(new Event(Event.DELETE, Constants.GROUP, group.getID(), group.getName()));

        //Remove the supervised group from any workspace items linked to us.
        supervisedItemService.removeSupervisedGroup(context, group);

        // Remove any ResourcePolicies that reference this group
        AuthorizeManager.removeGroupPolicies(context, group);

        group2GroupDAO.deleteByParent(context, group);
        group2GroupDAO.deleteByChild(context, group);


        //Remove all eperson references from this group
        Iterator<EPerson> ePeople = group.getEpeople().iterator();
        while (ePeople.hasNext()) {
            EPerson ePerson = ePeople.next();
            ePeople.remove();
            ePerson.getGroups().remove(group);
        }

        // Remove ourself
        groupDAO.delete(context, group);
        rethinkGroupCache(context);

        log.info(LogManager.getHeader(context, "delete_group", "group_id="
                + group.getID()));
    }

    /**
     * Return true if group has no direct or indirect members
     */
    @Override
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

    @Override
    public void updateLastModified(Context context, Group group) {
        //Not required for groups
    }

    /**
     * Update the group - writing out group object and EPerson list if necessary
     */
    @Override
    public void update(Context context, Group group) throws SQLException, AuthorizeException
    {
        // FIXME: Check authorisation
        groupDAO.save(context, group);

        if (group.isModifiedMetadata())
        {
            context.addEvent(new Event(Event.MODIFY_METADATA, Constants.GROUP, group.getID(), group.getDetails()));
            group.clearDetails();
        }

        if(group.isGroupsChanged())
        {
            rethinkGroupCache(context);
            group.clearGroupsChanged();
        }

        log.info(LogManager.getHeader(context, "update_group", "group_id="
                + group.getID()));
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
    protected Set<Group> getChildren(Map<Group,Set<Group>> parents, Group parent)
    {
        Set<Group> myChildren = new HashSet<Group>();

        // degenerate case, this parent has no children
        if (!parents.containsKey(parent))
        {
            return myChildren;
        }

        // got this far, so we must have children
        Set<Group> children =  parents.get(parent);

        // now iterate over all of the children

        for (Group child : children) {
            // add this child's ID to our return set
            myChildren.add(child);

            // and now its children
            myChildren.addAll(getChildren(parents, child));
        }

        return myChildren;
    }

    @Override
    public DSpaceObject getParentObject(Context context, Group group) throws SQLException
    {
        if(group == null)
        {
            return null;
        }
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
            org.dspace.content.Collection collection = collectionService.findByGroup(context, group);

            if (collection != null)
            {
                if ((group.equals(collection.getWorkflowStep1()) ||
                        group.equals(collection.getWorkflowStep2()) ||
                        group.equals(collection.getWorkflowStep3())))
                {
                    if (AuthorizeConfiguration.canCollectionAdminManageWorkflows())
                    {
                        return collection;
                    }
                    else if (AuthorizeConfiguration.canCommunityAdminManageCollectionWorkflows())
                    {
                        return collectionService.getParentObject(context, collection);
                    }
                }
                if (group.equals(collection.getSubmitters()))
                {
                    if (AuthorizeConfiguration.canCollectionAdminManageSubmitters())
                    {
                        return collection;
                    }
                    else if (AuthorizeConfiguration.canCommunityAdminManageCollectionSubmitters())
                    {
                        return collectionService.getParentObject(context, collection);
                    }
                }
                if (group.equals(collection.getAdministrators()))
                {
                    if (AuthorizeConfiguration.canCollectionAdminManageAdminGroup())
                    {
                        return collection;
                    }
                    else if (AuthorizeConfiguration.canCommunityAdminManageCollectionAdminGroup())
                    {
                        return collectionService.getParentObject(context, collection);
                    }
                }
            }
            // is the group related to a community and community administrator allowed
            // to manage it?
            else if (AuthorizeConfiguration.canCommunityAdminManageAdminGroup())
            {
                return communityService.findByAdminGroup(context, group);
            }
        }
        return null;
    }

    protected void rethinkGroupCache(Context context) throws SQLException
    {

        List<Group2Group> group2Groups = group2GroupDAO.findAll(context, Group2Group.class);

        Map<Group,Set<Group>> parents = new HashMap<Group,Set<Group>>();

        for (Group2Group group2Group : group2Groups) {
            Group parent = group2Group.getParent();
            Group child = group2Group.getChild();

            // if parent doesn't have an entry, create one
            if (!parents.containsKey(parent)) {
                Set<Group> children = new HashSet<Group>();

                // add child id to the list
                children.add(child);
                parents.put(parent, children);
            } else {
                // parent has an entry, now add the child to the parent's record
                // of children
                Set<Group> children = parents.get(parent);
                children.add(child);
            }
        }

        // now parents is a hash of all of the IDs of groups that are parents
        // and each hash entry is a hash of all of the IDs of children of those
        // parent groups
        // so now to establish all parent,child relationships we can iterate
        // through the parents hash
        for (Map.Entry<Group, Set<Group>> parent : parents.entrySet())
        {
            Set<Group> myChildren = getChildren(parents, parent.getKey());
            parent.getValue().addAll(myChildren);
        }

        // empty out group2groupcache table
        group2GroupCacheDAO.deleteAll(context);

        // write out new one
        for (Map.Entry<Group, Set<Group>> parent : parents.entrySet())
        {
            Group key  = parent.getKey();

            for (Group child : parent.getValue())
            {
                Group2GroupCache group2GroupCache = group2GroupCacheDAO.create(context, new Group2GroupCache());

                group2GroupCache.setParent(key);
                group2GroupCache.setChild(child);

                group2GroupCacheDAO.save(context, group2GroupCache);
            }
        }

    }
}