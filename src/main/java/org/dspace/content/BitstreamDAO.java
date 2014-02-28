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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.event.Event;
import org.dspace.hibernate.HibernateQueryUtil;
import org.dspace.storage.bitstore.BitstreamStorageManager;
import org.hibernate.Criteria;

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
public class BitstreamDAO extends DSpaceObjectDAO<Bitstream>
{
    /** log4j logger */
    private static Logger log = Logger.getLogger(Bitstream.class);

    /** The bitstream format corresponding to this bitstream */
    private BitstreamFormat bitstreamFormat;

    /** Flag set when data is modified, for events */
    private boolean modified;

    /** Flag set when metadata is modified, for events */
    private boolean modifiedMetadata;

    /**
     * Private constructor for creating a Bitstream object based on the contents
     * of a DB table row.
     * 
     * @param context
     *            the context this object exists in
     * @param row
     *            the corresponding row in the table
     * @throws SQLException
     */
    public BitstreamDAO() throws SQLException
    {
        // Get the bitstream format
        bitstreamFormat = BitstreamFormat.find(context, row
                .getIntColumn("bitstream_format_id"));

        if (bitstreamFormat == null)
        {
            // No format: use "Unknown"
            bitstreamFormat = BitstreamFormat.findUnknown(context);

            // Panic if we can't find it
            if (bitstreamFormat == null)
            {
                throw new IllegalStateException("No Unknown bitstream format");
            }
        }

        // Cache ourselves
        context.cache(this, row.getIntColumn("bitstream_id"));

        modified = false;
        modifiedMetadata = false;
        clearDetails();
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
    public static Bitstream find(Context context, int id) throws SQLException
    {
        // First check the cache
        Bitstream bitstream = (Bitstream) context.getDBConnection().get(Bitstream.class, id);

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

    public static Bitstream[] findAll(Context context) throws SQLException
    {
        List<Bitstream> bitstreams = context.getDBConnection().createCriteria(Bitstream.class).list();
        return bitstreams.toArray(new Bitstream[bitstreams.size()]);
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
            throws IOException, SQLException
    {
        // Store the bits
        int bitstreamID = BitstreamStorageManager.store(context, is);

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
        	throws IOException, SQLException
    {
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

    public String getHandle()
    {
        // No Handles for bitstreams
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
    public void setUserFormatDescription(String desc) throws SQLException
    {
        // FIXME: Would be better if this didn't throw an SQLException,
        // but we need to find the unknown format!
        setFormat(null);
        bRow.setColumn("user_format_description", desc);
        modifiedMetadata = true;
        addDetails("UserFormatDescription");
    }

    /**
     * Get the user's format description. Returns null if the format is known by
     * the system.
     *
     * @return the user's format description.
     */
    public String getUserFormatDescription()
    {
        return bRow.getStringColumn("user_format_description");
    }

    /**
     * Get the description of the format - either the user's or the description
     * of the format defined by the system.
     * 
     * @return a description of the format.
     */
    public String getFormatDescription()
    {
        if (bitstreamFormat.getShortDescription().equals("Unknown"))
        {
            // Get user description if there is one
            String desc = bRow.getStringColumn("user_format_description");

            if (desc == null)
            {
                return "Unknown";
            }

            return desc;
        }

        // not null or Unknown
        return bitstreamFormat.getShortDescription();
    }

    /**
     * Get the format of the bitstream
     * 
     * @return the format of this bitstream
     */
    public BitstreamFormat getFormat()
    {
        return bitstreamFormat;
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
    public void setFormat(Context context, BitstreamFormat f) throws SQLException
    {
        // FIXME: Would be better if this didn't throw an SQLException,
        // but we need to find the unknown format!
        if (f == null)
        {
            // Use "Unknown" format
            bitstreamFormat = new BitstreamFormatDAO().findUnknown(bContext);
        }
        else
        {
            bitstreamFormat = f;
        }

        // Remove user type description
        bRow.setColumnNull("user_format_description");

        // Update the ID in the table row
        bRow.setColumn("bitstream_format_id", bitstreamFormat.getID());
        modified = true;
    }

    /**
     * Update the bitstream metadata. Note that the content of the bitstream
     * cannot be changed - for that you need to create a new bitstream.
     * 
     * @throws SQLException
     * @throws AuthorizeException
     */
    public void update() throws SQLException, AuthorizeException
    {
        // Check authorisation
        AuthorizeManager.authorizeAction(bContext, this, Constants.WRITE);

        log.info(LogManager.getHeader(bContext, "update_bitstream",
                "bitstream_id=" + getID()));

        if (modified)
        {
            bContext.addEvent(new Event(Event.MODIFY, Constants.BITSTREAM, getID(), null));
            modified = false;
        }
        if (modifiedMetadata)
        {
            bContext.addEvent(new Event(Event.MODIFY_METADATA, Constants.BITSTREAM, getID(), getDetails()));
            modifiedMetadata = false;
            clearDetails();
        }

        DatabaseManager.update(bContext, bRow);
    }

    /**
     * Delete the bitstream, including any mappings to bundles
     * 
     * @throws SQLException
     */
    void delete(Context context, Bitstream bitstream) throws SQLException
    {
        boolean oracle = false;
        if ("oracle".equals(ConfigurationManager.getProperty("db.name")))
        {
            oracle = true;
        }

        // changed to a check on remove
        // Check authorisation
        //AuthorizeManager.authorizeAction(bContext, this, Constants.DELETE);
        log.info(LogManager.getHeader(context, "delete_bitstream",
                "bitstream_id=" + bitstream.getID()));

        context.addEvent(new Event(Event.DELETE, Constants.BITSTREAM, bitstream.getID(), String.valueOf(bitstream.getSequenceID())));

        // Remove policies
        AuthorizeManager.removeAllPolicies(context, bitstream);

        // Remove references to primary bitstreams in bundle
        String query = "update bundle set primary_bitstream_id = ";
        query += (oracle ? "''" : "Null") + " where primary_bitstream_id = ? ";
        DatabaseManager.updateQuery(bContext,
                query, bRow.getIntColumn("bitstream_id"));

        // Remove bitstream itself
        HibernateQueryUtil.delete(context, bitstream);
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
     * Get the bundles this bitstream appears in
     * 
     * @return array of <code>Bundle</code> s this bitstream appears in
     * @throws SQLException
     */
    public Bundle[] getBundles() throws SQLException
    {
        // Get the bundle table rows
        TableRowIterator tri = DatabaseManager.queryTable(bContext, "bundle",
                "SELECT bundle.* FROM bundle, bundle2bitstream WHERE " + 
                "bundle.bundle_id=bundle2bitstream.bundle_id AND " +
                "bundle2bitstream.bitstream_id= ? ",
                 bRow.getIntColumn("bitstream_id"));

        // Build a list of Bundle objects
        List<Bundle> bundles = new ArrayList<Bundle>();
        try
        {
            while (tri.hasNext())
            {
                TableRow r = tri.next();

                // First check the cache
                Bundle fromCache = (Bundle) bContext.fromCache(Bundle.class, r
                        .getIntColumn("bundle_id"));

                if (fromCache != null)
                {
                    bundles.add(fromCache);
                }
                else
                {
                    bundles.add(new Bundle(bContext, r));
                }
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

        Bundle[] bundleArray = new Bundle[bundles.size()];
        bundleArray = (Bundle[]) bundles.toArray(bundleArray);

        return bundleArray;
    }

    /**
     * return type found in Constants
     *
     * @return int Constants.BITSTREAM
     */
    public int getType()
    {
        return Constants.BITSTREAM;
    }

    /**
     * Determine if this bitstream is registered
     * 
     * @return true if the bitstream is registered, false otherwise
     */
    public boolean isRegisteredBitstream() {
        return BitstreamStorageManager
				.isRegisteredBitstream(bRow.getStringColumn("internal_id"));
    }
    
    /**
     * Get the asset store number where this bitstream is stored
     * 
     * @return the asset store number of the bitstream
     */
    public int getStoreNumber() {
        return bRow.getIntColumn("store_number");
    }

    /**
     * Get the parent object of a bitstream. The parent can be an item if this
     * is a normal bitstream, or it could be a collection or a community if the
     * bitstream is a logo.
     *
     * @return this bitstream's parent.
     * @throws SQLException
     */    
    public DSpaceObject getParentObject(Context context, Bitstream bitstream) throws SQLException
    {
        Bundle[] bundles = getBundles();
        if (bundles != null && (bundles.length > 0 && bundles[0] != null))
        {
            // the ADMIN action is not allowed on Bundle object so skip to the item
            Item item = bundles[0].getItem();
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
        {
            // is the bitstream a logo for a community or a collection?
            TableRow qResult = DatabaseManager.querySingle(bContext,
                       "SELECT collection_id FROM collection " +
                       "WHERE logo_bitstream_id = ?",getID());
            if (qResult != null) 
            {
                return Collection.find(bContext,qResult.getIntColumn("collection_id"));
            }
            else
            {   
                // is the bitstream related to a community?
                qResult = DatabaseManager.querySingle(bContext,
                        "SELECT community_id FROM community " +
                        "WHERE logo_bitstream_id = ?",getID());
    
                if (qResult != null)
                {
                    return Community.find(bContext,qResult.getIntColumn("community_id"));
                }
                else
                {
                    return null;
                }
            }                                   
        }
    }

    @Override
    public void updateLastModified()
    {
        //Also fire a modified event since the bitstream HAS been modified
        bContext.addEvent(new Event(Event.MODIFY, Constants.BITSTREAM, getID(), null));
    }
}