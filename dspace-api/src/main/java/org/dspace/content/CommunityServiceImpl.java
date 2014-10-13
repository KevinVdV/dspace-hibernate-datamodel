/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.app.util.AuthorizeUtil;
import org.dspace.authorize.*;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.dao.CommunityDAO;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.CommunityService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.I18nUtil;
import org.dspace.core.LogManager;
import org.dspace.eperson.Group;
import org.dspace.eperson.service.GroupService;
import org.dspace.event.Event;
import org.dspace.handle.service.HandleService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;

/**
 * Class representing a community
 * <P>
 * The community's metadata (name, introductory text etc.) is loaded into'
 * memory. Changes to this metadata are only reflected in the database after
 * <code>update</code> is called.
 * 
 * @author Robert Tansley
 * @version $Revision$
 */
public class CommunityServiceImpl extends DSpaceObjectServiceImpl<Community> implements CommunityService
{
    /** log4j category */
    private static Logger log = Logger.getLogger(Community.class);

    @Autowired(required = true)
    protected CommunityDAO communityDAO;

    @Autowired(required = true)
    protected CollectionService collectionService;
    @Autowired(required = true)
    protected GroupService groupService;
    @Autowired(required = true)
    protected BitstreamService bitstreamService;
    @Autowired(required = true)
    protected HandleService handleService;
    @Autowired(required = true)
    protected AuthorizeService authorizeService;



    public CommunityServiceImpl()
    {
    }

    /**
     * Get a community from the database. Loads in the metadata
     * 
     * @param context
     *            DSpace context object
     * @param id
     *            ID of the community
     * 
     * @return the community, or null if the ID is invalid.
     */
    @Override
    public Community find(Context context, UUID id) throws SQLException
    {

        Community community = communityDAO.findByID(context, Community.class, id);
        if (community == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug(LogManager.getHeader(context, "find_community",
                        "not_found,community_id=" + id));
            }
        }
        else
        {
            if (log.isDebugEnabled())
            {
                log.debug(LogManager.getHeader(context, "find_community",
                        "community_id=" + id));
            }
        }
        return community;
    }

    @Override
    public String getName(Community community) {
        return community.getNameInternal();
    }

    /**
     * Create a new top-level community, with a new ID.
     * 
     * @param context
     *            DSpace context object
     * 
     * @return the newly created community
     */
    @Override
    public Community create(Context context, Community parent)
            throws SQLException, AuthorizeException
    {
        return create(context, parent, null);
    }

    /**
     * Create a new top-level community, with a new ID.
     *
     * @param context
     *            DSpace context object
     * @param handle the pre-determined Handle to assign to the new community
     *
     * @return the newly created community
     */
    @Override
    public Community create(Context context, Community parent, String handle)
            throws SQLException, AuthorizeException
    {
        if (!(authorizeService.isAdmin(context) ||
              (parent != null && authorizeService.authorizeActionBoolean(context, parent, Constants.ADD))))
        {
            throw new AuthorizeException(
                    "Only administrators can create communities");
        }

        Community newCommunity = communityDAO.create(context, new Community());
        //Update our community so we have a community identifier

        try
        {
            if(handle == null)
            {
                handleService.createHandle(context, newCommunity);
            } else {
                handleService.createHandle(context, newCommunity, handle);
            }
        }
        catch(IllegalStateException ie)
        {
            //If an IllegalStateException is thrown, then an existing object is already using this handle
            throw ie;
        }

        if(parent != null)
        {
            addSubcommunity(context, parent, newCommunity);
        }

        // create the default authorization policy for communities
        // of 'anonymous' READ
        Group anonymousGroup = groupService.findByName(context, Group.ANONYMOUS);


        authorizeService.createResourcePolicy(context, newCommunity, anonymousGroup, null, Constants.READ, null);

        communityDAO.save(context, newCommunity);
        context.addEvent(new Event(Event.CREATE, Constants.COMMUNITY, newCommunity.getID(), newCommunity.getHandle(context)));

        // if creating a top-level Community, simulate an ADD event at the Site.
        if (parent == null)
        {
            context.addEvent(new Event(Event.ADD, Constants.SITE, ContentServiceFactory.getInstance().getSiteService().findSite(context).getID(), Constants.COMMUNITY, newCommunity.getID(), newCommunity.getHandle(context)));
        }

        log.info(LogManager.getHeader(context, "create_community",
                "community_id=" + newCommunity.getID())
                + ",handle=" + newCommunity.getHandle(context));

        return newCommunity;
    }

    /**
     * Get a list of all communities in the system. These are alphabetically
     * sorted by community name.
     * 
     * @param context
     *            DSpace context object
     * 
     * @return the communities in the system
     */
    @Override
    public List<Community> findAll(Context context) throws SQLException
    {
        return communityDAO.findAll(context, "name");
    }

    /**
     * Get a list of all top-level communities in the system. These are
     * alphabetically sorted by community name. A top-level community is one
     * without a parent community.
     * 
     * @param context
     *            DSpace context object
     * 
     * @return the top-level communities in the system
     */
    @Override
    public List<Community> findAllTop(Context context) throws SQLException
    {
        // get all communities that are not children
        return communityDAO.findMany(context, "from Community where parentCommunity IS NULL ORDER BY name");
    }

    @Override
    public Community findByAdminGroup(Context context, Group group) throws SQLException {
        return communityDAO.findByAdminGroup(context, group);
    }

    /**
     * Set a metadata value
     * 
     * @param value
     *            value to set the field to
     * 
     * @exception IllegalArgumentException
     *                if the requested metadata field doesn't exist
     * @exception MissingResourceException
     */
    @Override
    public void setName(Community community, String value)throws MissingResourceException
    {
        if (StringUtils.isBlank(value))
        {
            try
            {
                value = I18nUtil.getMessage("org.dspace.workflow.WorkflowManager.untitled");
            }
            catch (MissingResourceException e)
            {
                value = "Untitled";
            }
        }
        community.setNameInternal(value);
    }

    /**
     * Give the community a logo. Passing in <code>null</code> removes any
     * existing logo. You will need to set the format of the new logo bitstream
     * before it will work, for example to "JPEG". Note that
     * <code>update(/code> will need to be called for the change to take
     * effect.  Setting a logo and not calling <code>update</code> later may
     * result in a previous logo lying around as an "orphaned" bitstream.
     *
     * @param  is   the stream to use as the new logo
     *
     * @return   the new logo bitstream, or <code>null</code> if there is no
     *           logo (<code>null</code> was passed in)
     */
    @Override
    public Bitstream createLogo(Context context, Community community, InputStream is) throws AuthorizeException,
            IOException, SQLException
    {
        // Check authorisation
        // authorized to remove the logo when DELETE rights
        // authorized when canEdit
        if (!((is == null) && authorizeService.authorizeActionBoolean(
                context, community, Constants.DELETE)))
        {
            canEdit(context, community);
        }

        // First, delete any existing logo
        if (community.getLogo() != null)
        {
            log.info(LogManager.getHeader(context, "remove_logo",
                    "community_id=" + community.getID()));
            community.setLogo(null);
            bitstreamService.delete(context, community.getLogo());
        }

        if (is != null)
        {
            Bitstream newLogo = bitstreamService.create(context, is);
            community.setLogo(newLogo);

            // now create policy for logo bitstream
            // to match our READ policy
            List<ResourcePolicy> policies = authorizeService.getPoliciesActionFilter(context, community, Constants.READ);
            authorizeService.addPolicies(context, policies, newLogo);

            log.info(LogManager.getHeader(context, "set_logo",
                    "community_id=" + community.getID() + "logo_bitstream_id="
                            + newLogo.getID()));
        }

        return community.getLogo();
    }

    @Override
    public void updateLastModified(Context context, Community community)
    {
        //Also fire a modified event since the community HAS been modified
        context.addEvent(new Event(Event.MODIFY, Constants.COMMUNITY, community.getID(), null));
    }

    /**
     * Update the community metadata (including logo) to the database.
     */
    @Override
    public void update(Context context, Community community) throws SQLException, AuthorizeException
    {
        // Check authorisation
        canEdit(context, community);

        log.info(LogManager.getHeader(context, "update_community",
                "community_id=" + community.getID()));

        communityDAO.save(context, community);
        if (community.isModified())
        {
            context.addEvent(new Event(Event.MODIFY, Constants.COMMUNITY, community.getID(), null));
            community.clearModified();
        }
        if (community.isModifiedMetadata())
        {
            context.addEvent(new Event(Event.MODIFY_METADATA, Constants.COMMUNITY, community.getID(), community.getDetails()));
            community.clearModifiedMetadata();
            community.clearDetails();
        }
    }

    /**
     * Create a default administrators group if one does not already exist.
     * Returns either the newly created group or the previously existing one.
     * Note that other groups may also be administrators.
     * 
     * @return the default group of editors associated with this community
     * @throws SQLException
     * @throws AuthorizeException
     */
    @Override
    public Group createAdministrators(Context context, Community community) throws SQLException, AuthorizeException
    {
        // Check authorisation - Must be an Admin to create more Admins
        AuthorizeUtil.authorizeManageAdminGroup(context, community);

        Group admins = community.getAdministrators();
        if (admins == null)
        {
            //turn off authorization so that Community Admins can create Sub-Community Admins
            context.turnOffAuthorisationSystem();
            admins = groupService.create(context);
            context.restoreAuthSystemState();
            
            admins.setName("COMMUNITY_" + community.getID() + "_ADMIN");
            groupService.update(context, admins);
        }

        authorizeService.addPolicy(context, community, Constants.ADMIN, admins);
        
        // register this as the admin group
        community.setAdmins(admins);
        return admins;
    }
    
    /**
     * Remove the administrators group, if no group has already been created 
     * then return without error. This will merely dereference the current 
     * administrators group from the community so that it may be deleted 
     * without violating database constraints.
     */
    @Override
    public void removeAdministrators(Context context, Community community) throws SQLException, AuthorizeException
    {
        // Check authorisation - Must be an Admin of the parent community (or system admin) to delete Admin group
        AuthorizeUtil.authorizeRemoveAdminGroup(context, community);

        // just return if there is no administrative group.
        if (community.getAdministrators() == null)
        {
            return;
        }

        // Remove the link to the community table.
        community.setAdmins(null);
    }

    /**
     * Return an array of parent communities of this community, in ascending
     * order. If community is top-level, return an empty array.
     * 
     * @return an array of parent communities, empty if top-level
     */
    @Override
    public List<Community> getAllParents(Context context, Community community) throws SQLException
    {
        List<Community> parentList = new ArrayList<Community>();
        Community parent = (Community) getParentObject(context, community);

        while (parent != null)
        {
            parentList.add(parent);
            parent = (Community) getParentObject(context, community);
        }

        // Put them in an array
        return parentList;
    }

    /**
     * Return an array of collections of this community and its subcommunities
     * 
     * @return an array of collections
     */
    @Override
    public List<Collection> getAllCollections(Community community) throws SQLException
    {
        List<Collection> collectionList = new ArrayList<Collection>();
        List<Community> subCommunities = community.getSubCommunities();
        for (Community subCommunity : subCommunities)
        {
            addCollectionList(subCommunity, collectionList);
        }

        List<Collection> collections = community.getCollections();
        for (Collection collection : collections)
        {
            collectionList.add(collection);
        }
        return collectionList;

    }

    /**
     * Internal method to process subcommunities recursively
     */
    protected void addCollectionList(Community community, List<Collection> collectionList) throws SQLException
    {
        for (Community subcommunity : community.getSubCommunities())
        {
            addCollectionList(subcommunity, collectionList);
        }

        for (Collection collection : community.getCollections())
        {
            collectionList.add(collection);
        }
    }


    /**
     * Add an exisiting collection to the community
     * 
     * @param collection
     *            collection to add
     */
    @Override
    public void addCollection(Context context, Community community, Collection collection) throws SQLException,
            AuthorizeException
    {
        // Check authorisation
        authorizeService.authorizeAction(context, community, Constants.ADD);

        log.info(LogManager.getHeader(context, "add_collection",
                "community_id=" + community.getID() + ",collection_id=" + collection.getID()));

        if(!community.getCollections().contains(collection))
        {
            community.addCollection(collection);
            collection.addCommunity(community);
        }
        context.addEvent(new Event(Event.ADD, Constants.COMMUNITY, community.getID(), Constants.COLLECTION, collection.getID(), collection.getHandle(context)));
    }
    /**
     * Create a new sub-community within this community.
     * 
     * @return the new community
     */
    @Override
    public Community createSubcommunity(Context context, Community parentCommunity) throws SQLException,
            AuthorizeException
    {
        return createSubcommunity(context, parentCommunity, null);
    }

    /**
     * Create a new sub-community within this community.
     *
     * @param handle the pre-determined Handle to assign to the new community
     * @return the new community
     */
    @Override
    public Community createSubcommunity(Context context, Community parentCommunity, String handle) throws SQLException,
            AuthorizeException
    {
        // Check authorisation
        authorizeService.authorizeAction(context, parentCommunity, Constants.ADD);

        Community c = create(context, parentCommunity, handle);
        addSubcommunity(context, parentCommunity, c);

        return c;
    }

    /**
     * Add an exisiting community as a subcommunity to the community
     * 
     * @param childCommunity
     *            subcommunity to add
     */
    @Override
    public void addSubcommunity(Context context, Community parentCommunity, Community childCommunity) throws SQLException,
            AuthorizeException
    {
        // Check authorisation
        authorizeService.authorizeAction(context, parentCommunity, Constants.ADD);

        log.info(LogManager.getHeader(context, "add_subcommunity",
                "parent_comm_id=" + parentCommunity.getID() + ",child_comm_id=" + childCommunity.getID()));

        if(!parentCommunity.getSubCommunities().contains(childCommunity))
        {
            parentCommunity.addSubCommunity(childCommunity);
            childCommunity.setParentCommunities(Arrays.asList(parentCommunity));
        }
        context.addEvent(new Event(Event.ADD, Constants.COMMUNITY, parentCommunity.getID(), Constants.COMMUNITY, childCommunity.getID(), childCommunity.getHandle(context)));
    }

    /**
     * Remove a collection. Any items then orphaned are deleted.
     * 
     * @param c
     *            collection to remove
     */
    @Override
    public void removeCollection(Context context, Community community, Collection c) throws SQLException,
            AuthorizeException, IOException
    {
        // Check authorisation
        authorizeService.authorizeAction(context, community, Constants.REMOVE);

        community.removeCollection(c);
        c.removeCommunity(community);
        if(CollectionUtils.isEmpty(c.getCommunities())){
            collectionService.delete(context, c);
        }

        log.info(LogManager.getHeader(context, "remove_collection",
                "community_id=" + community.getID() + ",collection_id=" + c.getID()));
        
        // Remove any mappings
        context.addEvent(new Event(Event.REMOVE, Constants.COMMUNITY, community.getID(), Constants.COLLECTION, c.getID(), c.getHandle(context)));
    }


    /**
     * Remove a subcommunity. Any substructure then orphaned is deleted.
     * 
     * @param childCommunity
     *            subcommunity to remove
     */
    @Override
    public void removeSubcommunity(Context context, Community parentCommunity, Community childCommunity) throws SQLException,
            AuthorizeException, IOException
    {
        // Check authorisation
        authorizeService.authorizeAction(context, parentCommunity, Constants.REMOVE);

        parentCommunity.removeSubCommunity(childCommunity);
        childCommunity.setParentCommunities(null);
        log.info(LogManager.getHeader(context, "remove_subcommunity",
                "parent_comm_id=" + parentCommunity.getID() + ",child_comm_id=" + childCommunity.getID()));
        
        context.addEvent(new Event(Event.REMOVE, Constants.COMMUNITY, parentCommunity.getID(), Constants.COMMUNITY, childCommunity.getID(), childCommunity.getHandle(context)));
    }

    /**
     * Delete the community, including the metadata and logo. Collections and
     * subcommunities that are then orphans are deleted.
     */
    @Override
    public void delete(Context context, Community community) throws SQLException, AuthorizeException, IOException
    {
        // Check authorisation
        // FIXME: If this was a subcommunity, it is first removed from it's
        // parent.
        // This means the parentCommunity == null
        // But since this is also the case for top-level communities, we would
        // give everyone rights to remove the top-level communities.
        // The same problem occurs in removing the logo
        if (!authorizeService.authorizeActionBoolean(context, getParentObject(context, community), Constants.REMOVE))
        {
            authorizeService.authorizeAction(context, community, Constants.DELETE);
        }

        // If not a top-level community, have parent remove me; this
        // will call rawDelete() before removing the linkage
        Community parent = (Community) getParentObject(context, community);

        if (parent != null)
        {
            // remove the subcommunities first
            List<Community> subcommunities = community.getSubCommunities();
            for (Community subCommunity : subcommunities)
            {
                delete(context, subCommunity);
            }
            // now let the parent remove the community
            removeSubcommunity(context, parent, community);

            return;
        }

        rawDelete(context, community);
    }
    
    /**
     * Internal method to remove the community and all its childs from the database without aware of eventually parent  
     */
    protected void rawDelete(Context context, Community community) throws SQLException, AuthorizeException, IOException
    {
        log.info(LogManager.getHeader(context, "delete_community",
                "community_id=" + community.getID()));

        context.addEvent(new Event(Event.DELETE, Constants.COMMUNITY, community.getID(), community.getHandle(context)));

        // Remove collections
        List<Collection> collections = community.getCollections();

        for (Collection coll : collections)
        {
            removeCollection(context, community, coll);
        }
        // delete subcommunities
        List<Community> subCommunities = community.getSubCommunities();

        for (Community subComm : subCommunities)
        {
            delete(context, subComm);
        }

        // Remove the logo
        createLogo(context, community, null);

        // Remove all authorization policies
        authorizeService.removeAllPolicies(context, community);

        // Remove any Handle
        handleService.unbindHandle(context, community);

        Group g = community.getAdministrators();

        // Delete community row
        communityDAO.delete(context, community);

        // Remove administrators group - must happen after deleting community

        if (g != null)
        {
            groupService.delete(context, g);
        }
    }

    /**
     * return TRUE if context's user can edit community, false otherwise
     * 
     * @return boolean true = current user can edit community
     */
    @Override
    public boolean canEditBoolean(Context context, Community community) throws java.sql.SQLException
    {
        try
        {
            canEdit(context, community);

            return true;
        }
        catch (AuthorizeException e)
        {
            return false;
        }
    }

    @Override
    public void canEdit(Context context, Community community) throws AuthorizeException, SQLException
    {
        List<Community> parents = getAllParents(context, community);

        for (Community parent : parents) {
            if (authorizeService.authorizeActionBoolean(context, parent,
                    Constants.WRITE)) {
                return;
            }

            if (authorizeService.authorizeActionBoolean(context, parent,
                    Constants.ADD)) {
                return;
            }
        }

        authorizeService.authorizeAction(context, community, Constants.WRITE);
    }

    @Override
	public DSpaceObject getAdminObject(Context context, Community community, int action) throws SQLException
    {
        DSpaceObject adminObject = null;
        switch (action)
        {
        case Constants.REMOVE:
            if (AuthorizeConfiguration.canCommunityAdminPerformSubelementDeletion())
            {
                adminObject = community;
            }
            break;

        case Constants.DELETE:
            if (AuthorizeConfiguration.canCommunityAdminPerformSubelementDeletion())
            {
                adminObject = getParentObject(context, community);
            }
            break;
        case Constants.ADD:
            if (AuthorizeConfiguration.canCommunityAdminPerformSubelementCreation())
            {
                adminObject = community;
            }
            break;
        default:
            adminObject = community;
            break;
        }
        return adminObject;
    }

    @Override
    public DSpaceObject getParentObject(Context context, Community community) throws SQLException
    {
        List<Community> parentCommunities = community.getParentCommunities();
        if (CollectionUtils.isNotEmpty(parentCommunities))
        {
            return parentCommunities.iterator().next();
        }
        else
        {
            return null;
        }       
    }
}