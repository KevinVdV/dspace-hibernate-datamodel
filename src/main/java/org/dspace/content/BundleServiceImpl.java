/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeConfiguration;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.content.dao.BundleDAO;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.BundleService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.event.Event;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class representing bundles of bitstreams stored in the DSpace system
 * <P>
 * The corresponding Bitstream objects are loaded into memory. At present, there
 * is no metadata associated with bundles - they are simple containers. Thus,
 * the <code>update</code> method doesn't do much yet. Creating, adding or
 * removing bitstreams has instant effect in the database.
 * 
 * @author Robert Tansley
 * @version $Revision$
 */
public class BundleServiceImpl extends DSpaceObjectServiceImpl<Bundle> implements BundleService
{
    /** log4j logger */
    private static Logger log = Logger.getLogger(Bundle.class);

    @Autowired(required = true)
    protected BundleDAO bundleDAO;

    @Autowired(required = true)
    protected ItemService itemService;
    @Autowired(required = true)
    protected BitstreamService bitstreamService;

    /**
     * Construct a bundle object with the given table row
     * 
     */
    public BundleServiceImpl()
    {
    }

    /**
     * Get a bundle from the database. The bundle and bitstream metadata are all
     * loaded into memory.
     * 
     * @param context
     *            DSpace context object
     * @param id
     *            ID of the bundle
     * 
     * @return the bundle, or null if the ID is invalid.
     */
    public Bundle find(Context context, int id) throws SQLException
    {
        // First check the cache
        Bundle bundle = bundleDAO.findByID(context, Bundle.class, id);
        if (bundle == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug(LogManager.getHeader(context, "find_bundle",
                        "not_found,bundle_id=" + id));
            }

            return null;
        }
        else
        {
            if (log.isDebugEnabled())
            {
                log.debug(LogManager.getHeader(context, "find_bundle",
                        "bundle_id=" + id));
            }

            return bundle;
        }
    }

    public String getName(Bundle dso) {
        return dso.getName();
    }

    /**
     * Create a new bundle, with a new ID. This method is not public, since
     * bundles need to be created within the context of an item. For this
     * reason, authorisation is also not checked; that is the responsibility of
     * the caller.
     * 
     * @param context
     *            DSpace context object
     * 
     * @return the newly created bundle
     */
    //TODO: Implement properly so item doesn't needt to access this
    public Bundle create(Context context) throws SQLException
    {
        // Create a table row
        Bundle bundle = bundleDAO.create(context, new Bundle());
        log.info(LogManager.getHeader(context, "create_bundle", "bundle_id="
                + bundle.getID()));

        context.addEvent(new Event(Event.CREATE, Constants.BUNDLE, bundle.getID(), null));

        return bundle;
    }

    /**
     * @param name
     *            name of the bitstream you're looking for
     * 
     * @return the bitstream or null if not found
     */
    public Bitstream getBitstreamByName(Bundle bundle, String name)
    {
        Bitstream target = null;

        for (Bitstream b : bundle.getBitstreams()) {
            if (name.equals(b.getName())) {
                target = b;
                break;
            }
        }

        return target;
    }

    /**
     * Create a new bitstream in this bundle.
     * 
     * @param is
     *            the stream to read the new bitstream from
     * 
     * @return the newly created bitstream
     */
    public Bitstream createBitstream(Context context, Bundle bundle, InputStream is) throws AuthorizeException,
            IOException, SQLException
    {
        // Check authorisation
        AuthorizeManager.authorizeAction(context, bundle, Constants.ADD);

        Bitstream b = bitstreamService.create(context, is);

        // FIXME: Set permissions for bitstream
        addBitstream(context, bundle, b);

        return b;
    }

    /**
     * Create a new bitstream in this bundle. This method is for registering
     * bitstreams.
     *
     * @param assetstore corresponds to an assetstore in dspace.cfg
     * @param bitstreamPath the path and filename relative to the assetstore 
     * @return  the newly created bitstream
     * @throws IOException
     * @throws SQLException
     */
    public Bitstream registerBitstream(Context context, Bundle bundle, int assetstore, String bitstreamPath)
        throws AuthorizeException, IOException, SQLException
    {
        // check authorisation
        AuthorizeManager.authorizeAction(context, bundle, Constants.ADD);

        Bitstream b = bitstreamService.register(context, assetstore, bitstreamPath);

        // FIXME: Set permissions for bitstream

        addBitstream(context, bundle, b);
        return b;
    }

    /**
     * Add an existing bitstream to this bundle
     * 
     * @param b
     *            the bitstream to add
     */
    public void addBitstream(Context context, Bundle bundle, Bitstream b) throws SQLException,
            AuthorizeException
    {
        // Check authorisation
        AuthorizeManager.authorizeAction(context, bundle, Constants.ADD);

        log.info(LogManager.getHeader(context, "add_bitstream", "bundle_id="
                + bundle.getID() + ",bitstream_id=" + b.getID()));

        List<Bitstream> bitstreams = bundle.getBitstreams();
        int topOrder = 0;
        // First check that the bitstream isn't already in the list
        for (Bitstream existingBitstream : bitstreams) {
            if (b.getID() == existingBitstream.getID()) {
                // Bitstream is already there; no change
                return;
            }
            //The last file we encounter will have the highest order
            topOrder = existingBitstream.getBitstreamOrder();
        }


        b.setBitstreamOrder(topOrder++);
        // Add the bitstream object
        bundle.addBitstream(b);

        context.addEvent(new Event(Event.ADD, Constants.BUNDLE, bundle.getID(), Constants.BITSTREAM, b.getID(), String.valueOf(b.getSequenceID())));

        // copy authorization policies from bundle to bitstream
        // FIXME: multiple inclusion is affected by this...
        AuthorizeManager.inheritPolicies(context, bundle, b);
        bitstreamService.update(context, b);
    }

    /**
     * Changes bitstream order according to the array
     * @param bitstreamIds the identifiers in the order they are to be set
     * @throws SQLException when an SQL error has occurred (querying DSpace)
     * @throws AuthorizeException If the user can't make the changes
     */
    public void setOrder(Context context, Bundle bundle, int bitstreamIds[]) throws AuthorizeException, SQLException {
        AuthorizeManager.authorizeAction(context, bundle, Constants.WRITE);

        //TODO: REORDER HIBERNATE CACHE !
        for (int i = 0; i < bitstreamIds.length; i++) {
            int bitstreamId = bitstreamIds[i];
            Bitstream bitstream = bitstreamService.find(context, bitstreamId);
            if(bitstream == null){
                //This should never occur but just in case
                log.warn(LogManager.getHeader(context, "Invalid bitstream id while changing bitstream order", "Bundle: " + bundle.getID() + ", bitstream id: " + bitstreamId));
                continue;
            }
            bitstream.setBitstreamOrder(i);
            bitstreamService.update(context, bitstream);
        }

        //The order of the bitstreams has changed, ensure that we update the last modified of our item
        Item owningItem = (Item) getParentObject(bundle);
        if(owningItem != null)
        {
            itemService.updateLastModified(context, owningItem);
            itemService.update(context, owningItem);

        }
    }

    /**
     * Remove a bitstream from this bundle - the bitstream is only deleted if
     * this was the last reference to it
     * <p>
     * If the bitstream in question is the primary bitstream recorded for the
     * bundle the primary bitstream field is unset in order to free the
     * bitstream from the foreign key constraint so that the
     * <code>cleanup</code> process can run normally.
     * 
     * @param b
     *            the bitstream to remove
     */
    public void removeBitstream(Context context, Bundle bundle, Bitstream b) throws AuthorizeException,
            SQLException, IOException
    {
        // Check authorisation
        AuthorizeManager.authorizeAction(context, bundle, Constants.REMOVE);

        log.info(LogManager.getHeader(context, "remove_bitstream",
                "bundle_id=" + bundle.getID() + ",bitstream_id=" + b.getID()));

        // Remove from internal list of bitstreams
        ListIterator li = bundle.getBitstreams().listIterator();

        while (li.hasNext())
        {
            Bitstream existing = (Bitstream) li.next();

            if (b.getID() == existing.getID())
            {
                // We've found the bitstream to remove
                li.remove();
            }
        }

        context.addEvent(new Event(Event.REMOVE, Constants.BUNDLE, bundle.getID(), Constants.BITSTREAM, b.getID(), String.valueOf(b.getSequenceID())));

        //Ensure that the last modified from the item is triggered !
        Item owningItem = (Item) getParentObject(bundle);
        if(owningItem != null)
        {
            itemService.updateLastModified(context, owningItem);
            itemService.update(context, owningItem);

        }

        // In the event that the bitstream to remove is actually
        // the primary bitstream, be sure to unset the primary
        // bitstream.
        if (b.equals(bundle.getPrimaryBitstream()))
        {
            bundle.setPrimaryBitstream(null);
        }

        bundle.removeBitstream(b);
        bitstreamService.delete(context, b);


    }

    public void updateLastModified(Context context, Bundle bundle) {
    }

    /**
     * Update the bundle metadata
     */
    public void update(Context context, Bundle bundle) throws SQLException, AuthorizeException
    {
        // Check authorisation
        //AuthorizeManager.authorizeAction(ourContext, this, Constants.WRITE);
        log.info(LogManager.getHeader(context, "update_bundle", "bundle_id="
                + bundle.getID()));

        if (bundle.isModified())
        {
            context.addEvent(new Event(Event.MODIFY, Constants.BUNDLE, bundle.getID(), null));
            bundle.clearModified();
        }
        if (bundle.isModifiedMetadata())
        {
            context.addEvent(new Event(Event.MODIFY_METADATA, Constants.BUNDLE, bundle.getID(), null));
            bundle.cleartModifiedMetadata();
        }

        bundleDAO.save(context, bundle);
    }

    /**
     * Delete the bundle. Bitstreams contained by the bundle are removed first;
     * this may result in their deletion, if deleting this bundle leaves them as
     * orphans.
     */
    //TODO: Implement properrly so item doesn't have issues with this
    public void delete(Context context, Bundle bundle) throws SQLException, AuthorizeException, IOException
    {
        log.info(LogManager.getHeader(context, "delete_bundle", "bundle_id="
                + bundle.getID()));

        context.addEvent(new Event(Event.DELETE, Constants.BUNDLE, bundle.getID(), bundle.getName()));

        // Remove bitstreams
        List<Bitstream> bs = bundle.getBitstreams();

        for (Bitstream b : bs) {
            removeBitstream(context, bundle, b);
        }

        // remove our authorization policies
        AuthorizeManager.removeAllPolicies(context, bundle);

        // Remove ourself
        bundleDAO.delete(context, bundle);
    }


    /**
     * remove all policies on the bundle and its contents, and replace them with
     * the DEFAULT_BITSTREAM_READ policies belonging to the collection.
     * 
     * @param c
     *            Collection
     * @throws java.sql.SQLException
     *             if an SQL error or if no default policies found. It's a bit
     *             draconian, but default policies must be enforced.
     * @throws AuthorizeException
     */
    public void inheritCollectionDefaultPolicies(Context context, Bundle bundle, Collection c)
            throws java.sql.SQLException, AuthorizeException
    {
        List<ResourcePolicy> policies = AuthorizeManager.getPoliciesActionFilter(context, c,
                Constants.DEFAULT_BITSTREAM_READ);

        // change the action to just READ
        // just don't call update on the resourcepolicies!!!
        Iterator<ResourcePolicy> i = policies.iterator();

        if (!i.hasNext())
        {
            throw new java.sql.SQLException("Collection " + c.getID()
                    + " has no default bitstream READ policies");
        }

        while (i.hasNext())
        {
            ResourcePolicy rp = i.next();
            rp.setAction(Constants.READ);
        }

        replaceAllBitstreamPolicies(context, bundle, policies);
    }
    
    /**
     * remove all of the policies for the bundle and bitstream contents and replace
     * them with a new list of policies
     * 
     * @param newpolicies -
     *            this will be all of the new policies for the bundle and
     *            bitstream contents
     * @throws SQLException
     * @throws AuthorizeException
     */
    public void replaceAllBitstreamPolicies(Context context, Bundle bundle, List<ResourcePolicy> newpolicies)
            throws SQLException, AuthorizeException
    {
        List<Bitstream> bitstreams = bundle.getBitstreams();
        if (bitstreams != null && bitstreams.size() > 0)
        {
            for (Bitstream bs : bitstreams)
            {
                // change bitstream policies
                AuthorizeManager.removeAllPolicies(context, bs);
                AuthorizeManager.addPolicies(context, newpolicies, bs);
            }
        }
        // change bundle policies
        AuthorizeManager.removeAllPolicies(context, bundle);
        AuthorizeManager.addPolicies(context, newpolicies, bundle);
    }

    public List<ResourcePolicy> getBitstreamPolicies(Context context, Bundle bundle) throws SQLException
    {
        List<Bitstream> bitstreams = bundle.getBitstreams();
        List<ResourcePolicy> list = new ArrayList<ResourcePolicy>();
        if (bitstreams != null && bitstreams.size() > 0)
        {
            for (Bitstream bs : bitstreams)
            {
                list.addAll(AuthorizeManager.getPolicies(context, bs));
            }
        }
        return list;
    }
    
    public DSpaceObject getAdminObject(Context context, Bundle bundle, int action) throws SQLException
    {
        DSpaceObject adminObject = null;
        Item item = (Item) getParentObject(bundle);
        Collection collection = null;
        Community community = null;
        if (item != null)
        {
            collection = item.getOwningCollection();
            if (collection != null)
            {
                community = collection.getCommunities().iterator().next();
            }
        }
        switch (action)
        {
        case Constants.REMOVE:
            if (AuthorizeConfiguration.canItemAdminPerformBitstreamDeletion())
            {
                adminObject = item;
            }
            else if (AuthorizeConfiguration.canCollectionAdminPerformBitstreamDeletion())
            {
                adminObject = collection;
            }
            else if (AuthorizeConfiguration
                    .canCommunityAdminPerformBitstreamDeletion())
            {
                adminObject = community;
            }
            break;
        case Constants.ADD:
            if (AuthorizeConfiguration.canItemAdminPerformBitstreamCreation())
            {
                adminObject = item;
            }
            else if (AuthorizeConfiguration
                    .canCollectionAdminPerformBitstreamCreation())
            {
                adminObject = collection;
            }
            else if (AuthorizeConfiguration
                    .canCommunityAdminPerformBitstreamCreation())
            {
                adminObject = community;
            }
            break;

        default:
            adminObject = bundle;
            break;
        }
        return adminObject;
    }
    
    public DSpaceObject getParentObject(Bundle bundle) throws SQLException
    {
        List<Item> items = bundle.getItems();
        if(CollectionUtils.isNotEmpty(items))
        {
            return items.iterator().next();
        }else{
            return null;
        }
    }
}