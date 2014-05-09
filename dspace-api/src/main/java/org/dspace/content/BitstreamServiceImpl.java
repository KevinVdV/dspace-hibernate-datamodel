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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.dao.BitstreamDAO;
import org.dspace.content.service.BitstreamFormatService;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.BundleService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.event.Event;
import org.dspace.storage.bitstore.BitstreamStorageManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class representing bitstreams stored in the DSpace system.
 * <P>
 * When modifying the bitstream metadata, changes are not reflected in the
 * database until <code>update</code> is called. Note that you cannot alter
 * the contents of a bitstream; you need to create a new bitstream.
 * 
 * @author Robert Tansley
 * @version $Revision$
 */
public class BitstreamServiceImpl extends DSpaceObjectServiceImpl<Bitstream> implements BitstreamService
{
    /** log4j logger */
    private static Logger log = Logger.getLogger(Bitstream.class);


    @Autowired(required = true)
    protected BitstreamDAO bitstreamDAO;

    @Autowired(required = true)
    protected BitstreamFormatService bitstreamFormatService;
    @Autowired(required = true)
    protected BundleService bundleService;

    public BitstreamServiceImpl()
    {
    }

    /**
     * Get a bitstream from the database. The bitstream metadata is loaded into
     * memory.
     * 
     * @param context
     *            DSpace context object
     * @param id
     *            ID of the bitstream
     * 
     * @return the bitstream, or null if the ID is invalid.
     * @throws SQLException
     */
    public Bitstream find(Context context, int id) throws SQLException
    {
        // First check the cache
        Bitstream bitstream = bitstreamDAO.findByID(context, Bitstream.class, id);

        if (bitstream == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug(LogManager.getHeader(context, "find_bitstream",
                        "not_found,bitstream_id=" + id));
            }

            return null;
        }

        // not null, return Bitstream
        if (log.isDebugEnabled())
        {
            log.debug(LogManager.getHeader(context, "find_bitstream",
                    "bitstream_id=" + id));
        }

        return bitstream;
    }

    public String getName(Bitstream dso) {
        return dso.getNameInternal();
    }

    public List<Bitstream> findAll(Context context) throws SQLException
    {
        return bitstreamDAO.findAll(context, Bitstream.class);
    }

    /**
     * Create a new bitstream, with a new ID. The checksum and file size are
     * calculated. This method is not public, and does not check authorisation;
     * other methods such as Bundle.createBitstream() will check authorisation.
     * The newly created bitstream has the "unknown" format.
     * 
     * @param context
     *            DSpace context object
     * @param is
     *            the bits to put in the bitstream
     * 
     * @return the newly created bitstream
     * @throws IOException
     * @throws SQLException
     */
    public Bitstream create(Context context, InputStream is)
            throws IOException, SQLException, AuthorizeException {
        // Store the bits
        int bitstreamID = BitstreamStorageManager.store(context, bitstreamDAO.create(context, new Bitstream()), is);

        log.info(LogManager.getHeader(context, "create_bitstream",
                "bitstream_id=" + bitstreamID));

        // Set the format to "unknown"
        Bitstream bitstream = find(context, bitstreamID);
        bitstream.setFormat(null);

        context.addEvent(new Event(Event.CREATE, Constants.BITSTREAM, bitstreamID, null));

        return bitstream;
    }

    /**
     * Register a new bitstream, with a new ID.  The checksum and file size
     * are calculated.  This method is not public, and does not check
     * authorisation; other methods such as Bundle.createBitstream() will
     * check authorisation.  The newly created bitstream has the "unknown"
     * format.
     *
     * @param  context DSpace context object
     * @param assetstore corresponds to an assetstore in dspace.cfg
     * @param bitstreamPath the path and filename relative to the assetstore 
     * @return  the newly registered bitstream
     * @throws IOException
     * @throws SQLException
     */
    public Bitstream register(Context context,
    		int assetstore, String bitstreamPath)
            throws IOException, SQLException, AuthorizeException {
        // Store the bits
        int bitstreamID = BitstreamStorageManager.register(
                context, assetstore, bitstreamPath);

        log.info(LogManager.getHeader(context,
            "create_bitstream",
            "bitstream_id=" + bitstreamID));

        // Set the format to "unknown"
        Bitstream bitstream = find(context, bitstreamID);
        bitstream.setFormat(null);

        context.addEvent(new Event(Event.CREATE, Constants.BITSTREAM, bitstreamID, "REGISTER"));

        return bitstream;
    }

    public List<Bitstream> findDeletedBitstreams(Context context) throws SQLException {
        return bitstreamDAO.findDeletedBitstreams(context);
    }

    public List<Bitstream> findDuplicateInternalIdentifier(Context context, Bitstream bitstream) throws SQLException {
        return bitstreamDAO.findDuplicateInternalIdentifier(context, bitstream);
    }

    @Override
    public Iterator<Bitstream> findAllInCommunity(Context context, Community community) {
        //TODO: IMPLEMENT THIS ONCE BUNDLE HAS BEEN REFACTORED
        return null;
    }

    @Override
    public Iterator<Bitstream> findAllInCollection(Context context, Collection collection) {
        //TODO: IMPLEMENT THIS ONCE BUNDLE HAS BEEN REFACTORED
        return null;
    }

    @Override
    public Iterator<Bitstream> findAllInItem(Context context, Item item) {
        //TODO: IMPLEMENT THIS ONCE BUNDLE HAS BEEN REFACTORED
        return null;
    }

    /**
     * Set the user's format description. This implies that the format of the
     * bitstream is uncertain, and the format is set to "unknown."
     *
     * @param desc
     *            the user's description of the format
     * @throws SQLException
     */
    public void setUserFormatDescription(Context context, Bitstream bitstream, String desc) throws SQLException
    {
        // FIXME: Would be better if this didn't throw an SQLException,
        // but we need to find the unknown format!
        setFormat(context,bitstream,  null);
        bitstream.setUserFormatDescription(desc);
    }

    /**
     * Get the description of the format - either the user's or the description
     * of the format defined by the system.
     * 
     * @return a description of the format.
     */
    public String getFormatDescription(Bitstream bitstream)
    {
        if (bitstream.getFormat().getShortDescription().equals("Unknown"))
        {
            // Get user description if there is one
            String desc = bitstream.getUserFormatDescription();

            if (desc == null)
            {
                return "Unknown";
            }

            return desc;
        }

        // not null or Unknown
        return bitstream.getFormat().getShortDescription();
    }

    /**
     * Set the format of the bitstream. If the user has supplied a type
     * description, it is cleared. Passing in <code>null</code> sets the type
     * of this bitstream to "unknown".
     * 
     * @param f
     *            the format of this bitstream, or <code>null</code> for
     *            unknown
     * @throws SQLException
     */
    public void setFormat(Context context, Bitstream bitstream, BitstreamFormat f) throws SQLException
    {
        // FIXME: Would be better if this didn't throw an SQLException,
        // but we need to find the unknown format!
        if (f == null)
        {
            // Use "Unknown" format
            f = bitstreamFormatService.findUnknown(context);
        }

        // Remove user type description
        bitstream.setUserFormatDescription(null);

        // Update the ID in the table row
        bitstream.setFormat(f);
    }

    public void updateLastModified(Context context, Bitstream bitstream) {
        //Fire a modified event since the bitstream HAS been modified
        context.addEvent(new Event(Event.MODIFY, Constants.BITSTREAM, bitstream.getID(), null));
    }

    /**
     * Update the bitstream metadata. Note that the content of the bitstream
     * cannot be changed - for that you need to create a new bitstream.
     * 
     * @throws SQLException
     * @throws AuthorizeException
     */
    public void update(Context context, Bitstream bitstream) throws SQLException, AuthorizeException
    {
        // Check authorisation
        AuthorizeManager.authorizeAction(context, bitstream, Constants.WRITE);

        log.info(LogManager.getHeader(context, "update_bitstream",
                "bitstream_id=" + bitstream.getID()));

        if (bitstream.isModified())
        {
            context.addEvent(new Event(Event.MODIFY, Constants.BITSTREAM, bitstream.getID(), null));
            bitstream.setModified(false);
        }
        if (bitstream.isModifiedMetadata())
        {
            context.addEvent(new Event(Event.MODIFY_METADATA, Constants.BITSTREAM, bitstream.getID(), bitstream.getDetails()));
            bitstream.setModifiedMetadata(false);
            bitstream.clearDetails();
        }

        bitstreamDAO.save(context, bitstream);
    }

    /**
     * Delete the bitstream, including any mappings to bundles
     * 
     * @throws SQLException
     */
    public void delete(Context context, Bitstream bitstream) throws SQLException, AuthorizeException {
        bitstream.setBundles(null);
        // changed to a check on remove
        // Check authorisation
        //AuthorizeManager.authorizeAction(bContext, this, Constants.DELETE);
        log.info(LogManager.getHeader(context, "delete_bitstream",
                "bitstream_id=" + bitstream.getID()));

        //TODO: remove from bundle ?
        context.addEvent(new Event(Event.DELETE, Constants.BITSTREAM, bitstream.getID(), String.valueOf(bitstream.getSequenceID())));

        // Remove policies
        AuthorizeManager.removeAllPolicies(context, bitstream);

        // Remove bitstream itself
        bitstream.setDeleted(true);
        update(context, bitstream);
    }

    /**
     * Retrieve the contents of the bitstream
     * 
     * @return a stream from which the bitstream can be read.
     * @throws IOException
     * @throws SQLException
     * @throws AuthorizeException
     */
    public InputStream retrieve(Context context, Bitstream bitstream) throws IOException, SQLException,
            AuthorizeException
    {
        // Maybe should return AuthorizeException??
        AuthorizeManager.authorizeAction(context, bitstream, Constants.READ);

        return BitstreamStorageManager.retrieve(context, bitstream.getID());
    }

    /**
     * Determine if this bitstream is registered
     * 
     * @return true if the bitstream is registered, false otherwise
     */
    public boolean isRegisteredBitstream(Bitstream bitstream) {
        return BitstreamStorageManager
				.isRegisteredBitstream(bitstream.getInternalId());
    }

    @Override
    public List<Bitstream> findBitstreamsWithNoRecentChecksum(Context context) throws SQLException {
        return bitstreamDAO.findBitstreamsWithNoRecentChecksum(context);
    }

    /**
     * Get the parent object of a bitstream. The parent can be an item if this
     * is a normal bitstream, or it could be a collection or a community if the
     * bitstream is a logo.
     *
     * @return this bitstream's parent.
     * @throws SQLException
     */    
    public DSpaceObject getParentObject(Bitstream bitstream) throws SQLException
    {
        List<Bundle> bundles = bitstream.getBundles();
        if (CollectionUtils.isNotEmpty(bundles))
        {
            // the ADMIN action is not allowed on Bundle object so skip to the item
            Item item = (Item) bundleService.getParentObject(bundles.iterator().next());
            if (item != null)
            {
                return item;
            }
            else
            {
                return null;
            }
        }
        else
        if(bitstream.getCommunity() != null)
        {
            return bitstream.getCommunity();
        }else
        if(bitstream.getCollection() != null)
        {
            return bitstream.getCollection();
        }
        return null;
    }
}