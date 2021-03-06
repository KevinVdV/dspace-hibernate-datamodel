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
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.dao.BitstreamDAO;
import org.dspace.content.dao.BundleBitstreamDAO;
import org.dspace.content.service.*;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.event.Event;
import org.dspace.storage.service.BitstreamStorageService;
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
    protected BundleBitstreamDAO bundleBitstreamDAO;

    @Autowired(required = true)
    protected BitstreamFormatService bitstreamFormatService;
    @Autowired(required = true)
    protected BundleService bundleService;
    @Autowired(required = true)
    protected AuthorizeService authorizeService;
    @Autowired(required = true)
    protected BitstreamStorageService bitstreamStorageService;


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
    @Override
    public Bitstream find(Context context, UUID id) throws SQLException
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

    @Override
    public String getName(Bitstream dso) {
        return dso.getNameInternal();
    }

    @Override
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
    @Override
    public Bitstream create(Context context, InputStream is)
            throws IOException, SQLException, AuthorizeException {
        // Store the bits
        UUID bitstreamID = bitstreamStorageService.store(context, bitstreamDAO.create(context, new Bitstream()), is);

        log.info(LogManager.getHeader(context, "create_bitstream",
                "bitstream_id=" + bitstreamID));

        // Set the format to "unknown"
        Bitstream bitstream = find(context, bitstreamID);
        setFormat(context, bitstream, null);

        context.addEvent(new Event(Event.CREATE, Constants.BITSTREAM, bitstreamID, null));

        return bitstream;
    }

    /**
     * Create a new bitstream in this bundle.
     *
     * @param is
     *            the stream to read the new bitstream from
     *
     * @return the newly created bitstream
     */
    @Override
    public Bitstream create(Context context, Bundle bundle, InputStream is) throws AuthorizeException,
            IOException, SQLException
    {
        // Check authorisation
        authorizeService.authorizeAction(context, bundle, Constants.ADD);

        Bitstream b = create(context, is);
        bundleService.addBitstream(context, bundle, b);
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
    @Override
    public Bitstream register(Context context, Bundle bundle, int assetstore, String bitstreamPath)
        throws AuthorizeException, IOException, SQLException
    {
        // check authorisation
        authorizeService.authorizeAction(context, bundle, Constants.ADD);

        Bitstream b = register(context, assetstore, bitstreamPath);
        bundleService.addBitstream(context, bundle, b);
        return b;
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
    @Override
    public Bitstream register(Context context,
    		int assetstore, String bitstreamPath)
            throws IOException, SQLException, AuthorizeException {
        // Store the bits
        Bitstream bitstream = bitstreamDAO.create(context, new Bitstream());
        bitstreamStorageService.register(
                context, bitstream, assetstore, bitstreamPath);

        log.info(LogManager.getHeader(context,
            "create_bitstream",
            "bitstream_id=" + bitstream.getID()));

        // Set the format to "unknown"
        setFormat(context, bitstream, null);

        context.addEvent(new Event(Event.CREATE, Constants.BITSTREAM, bitstream.getID(), "REGISTER"));

        return bitstream;
    }

    @Override
    public List<Bitstream> findDeletedBitstreams(Context context) throws SQLException {
        return bitstreamDAO.findDeletedBitstreams(context);
    }

    @Override
    public List<Bitstream> findDuplicateInternalIdentifier(Context context, Bitstream bitstream) throws SQLException {
        return bitstreamDAO.findDuplicateInternalIdentifier(context, bitstream);
    }

    @Override
    public Iterator<Bitstream> findAllInCommunity(Context context, Community community) throws SQLException {
        return bitstreamDAO.findByCommunity(context, community);
    }

    @Override
    public Iterator<Bitstream> findAllInCollection(Context context, Collection collection) throws SQLException {
        return bitstreamDAO.findByCollection(context, collection);
    }

    @Override
    public Iterator<Bitstream> findAllInItem(Context context, Item item) throws SQLException {
        return bitstreamDAO.findByItem(context, item);
    }

    /**
     * Set the user's format description. This implies that the format of the
     * bitstream is uncertain, and the format is set to "unknown."
     *
     * @param desc
     *            the user's description of the format
     * @throws SQLException
     */
    @Override
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
    @Override
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
    @Override
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

    @Override
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
    @Override
    public void update(Context context, Bitstream bitstream) throws SQLException, AuthorizeException
    {
        // Check authorisation
        authorizeService.authorizeAction(context, bitstream, Constants.WRITE);

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
    @Override
    public void delete(Context context, Bitstream bitstream) throws SQLException, AuthorizeException {

        Iterator<BundleBitstream> bundleBits = bitstream.getBundles().iterator();
        while(bundleBits.hasNext())
        {
            BundleBitstream bundleBitstream = bundleBits.next();
            if(bundleBitstream.getBitstream().getID() == bitstream.getID())
            {
                bundleBits.remove();
                bundleBitstreamDAO.delete(context, bundleBitstream);

            }
        }
        // changed to a check on remove
        // Check authorisation
        //AuthorizeManager.authorizeAction(bContext, this, Constants.DELETE);
        log.info(LogManager.getHeader(context, "delete_bitstream",
                "bitstream_id=" + bitstream.getID()));

        //TODO: remove from bundle ?
        context.addEvent(new Event(Event.DELETE, Constants.BITSTREAM, bitstream.getID(), String.valueOf(bitstream.getSequenceID())));

        // Remove policies
        authorizeService.removeAllPolicies(context, bitstream);

        // Remove bitstream itself
        bitstream.setDeleted(true);
        update(context, bitstream);
    }

    @Override
    public void expunge(Context context, Bitstream bitstream) throws SQLException, AuthorizeException {
        authorizeService.authorizeAction(context, bitstream, Constants.DELETE);
        if(!bitstream.isDeleted())
        {
            throw new IllegalStateException("Bitstream must be deleted before it can be removed from the database");
        }
        bitstreamDAO.delete(context, bitstream);
    }


    /**
     * Retrieve the contents of the bitstream
     * 
     * @return a stream from which the bitstream can be read.
     * @throws IOException
     * @throws SQLException
     * @throws AuthorizeException
     */
    @Override
    public InputStream retrieve(Context context, Bitstream bitstream) throws IOException, SQLException,
            AuthorizeException
    {
        // Maybe should return AuthorizeException??
        authorizeService.authorizeAction(context, bitstream, Constants.READ);

        return bitstreamStorageService.retrieve(context, bitstream.getID());
    }

    /**
     * Determine if this bitstream is registered
     * 
     * @return true if the bitstream is registered, false otherwise
     */
    @Override
    public boolean isRegisteredBitstream(Bitstream bitstream) {
        return bitstreamStorageService
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
    @Override
    public DSpaceObject getParentObject(Context context, Bitstream bitstream) throws SQLException
    {
        List<BundleBitstream> bundles = bitstream.getBundles();
        if (CollectionUtils.isNotEmpty(bundles))
        {
            // the ADMIN action is not allowed on Bundle object so skip to the item
            Item item = (Item) bundleService.getParentObject(context, bundles.iterator().next().getBundle());
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