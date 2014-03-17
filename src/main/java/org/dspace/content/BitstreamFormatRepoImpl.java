/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.core.Context;
import org.dspace.core.LogManager;

/**
 * Class representing a particular bitstream format.
 * <P>
 * Changes to the bitstream format metadata are only written to the database
 * when <code>update</code> is called.
 * 
 * @author Robert Tansley
 * @version $Revision$
 */
public class BitstreamFormatRepoImpl
{
    /** log4j logger */
    private static Logger log = Logger.getLogger(BitstreamFormat.class);

    /**
     * The "unknown" support level - for bitstream formats that are unknown to
     * the system
     */
    public static final int UNKNOWN = 0;

    /**
     * The "known" support level - for bitstream formats that are known to the
     * system, but not fully supported
     */
    public static final int KNOWN = 1;

    /**
     * The "supported" support level - for bitstream formats known to the system
     * and fully supported.
     */
    public static final int SUPPORTED = 2;


    /** translate support-level ID to string.  MUST keep this table in sync
     *  with support level definitions above.
     */
    private static final String supportLevelText[] =
        { "UNKNOWN", "KNOWN", "SUPPORTED" };


    private BitstreamFormatDAO bitstreamFormatDAO = new BitstreamFormatDAOImpl();


    /**
     * Class constructor for creating a BitstreamFormat object based on the
     * contents of a DB table row.
     * 
     * @throws SQLException
     */
    public BitstreamFormatRepoImpl()
    {
    }

    /**
     * Get a bitstream format from the database.
     * 
     * @param context
     *            DSpace context object
     * @param id
     *            ID of the bitstream format
     * 
     * @return the bitstream format, or null if the ID is invalid.
     * @throws SQLException
     */
    public BitstreamFormat find(Context context, int id)
            throws SQLException
    {
        BitstreamFormat bitstreamFormat = bitstreamFormatDAO.findByID(context, BitstreamFormat.class, id);

        if (bitstreamFormat == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug(LogManager.getHeader(context,
                        "find_bitstream_format",
                        "not_found,bitstream_format_id=" + id));
            }

            return null;
        }

        // not null, return format object
        if (log.isDebugEnabled())
        {
            log.debug(LogManager.getHeader(context, "find_bitstream_format",
                    "bitstream_format_id=" + id));
        }

        return bitstreamFormat;
    }

    /**
     * Find a bitstream format by its (unique) MIME type.
     * If more than one bitstream format has the same MIME type, the
     * one returned is unpredictable.
     *
     * @param context
     *            DSpace context object
     * @param mimeType
     *            MIME type value
     *
     * @return the corresponding bitstream format, or <code>null</code> if
     *         there's no bitstream format with the given MIMEtype.
     * @throws SQLException
     */
    public BitstreamFormat findByMIMEType(Context context, String mimeType) throws SQLException
    {
        return bitstreamFormatDAO.findByMIMEType(context, mimeType);
    }

    /**
     * Find a bitstream format by its (unique) short description
     * 
     * @param context
     *            DSpace context object
     * @param desc
     *            the short description
     * 
     * @return the corresponding bitstream format, or <code>null</code> if
     *         there's no bitstream format with the given short description
     * @throws SQLException
     */
    public BitstreamFormat findByShortDescription(Context context,
            String desc) throws SQLException
    {
        return bitstreamFormatDAO.findByMIMEType(context, desc);
    }

    /**
     * Get the generic "unknown" bitstream format.
     * 
     * @param context
     *            DSpace context object
     * 
     * @return the "unknown" bitstream format.
     * @throws SQLException
     * 
     * @throws IllegalStateException
     *             if the "unknown" bitstream format couldn't be found
     */
    public BitstreamFormat findUnknown(Context context)
            throws SQLException
    {
        BitstreamFormat bf = findByShortDescription(context, "Unknown");

        if (bf == null)
        {
            throw new IllegalStateException(
                    "No `Unknown' bitstream format in registry");
        }

        return bf;
    }

    /**
     * Retrieve all bitstream formats from the registry, ordered by ID
     * 
     * @param context
     *            DSpace context object
     * 
     * @return the bitstream formats.
     * @throws SQLException
     */
    public List<BitstreamFormat> findAll(Context context) throws SQLException
    {
        return bitstreamFormatDAO.findAll(context, BitstreamFormat.class);
    }

    /**
     * Retrieve all non-internal bitstream formats from the registry. The
     * "unknown" format is not included, and the formats are ordered by support
     * level (highest first) first then short description.
     * 
     * @param context
     *            DSpace context object
     * 
     * @return the bitstream formats.
     * @throws SQLException
     */
    public List<BitstreamFormat> findNonInternal(Context context)
            throws SQLException
    {

        return bitstreamFormatDAO.findNonInternal(context);
    }

    /**
     * Create a new bitstream format
     * 
     * @param context
     *            DSpace context object
     * @return the newly created BitstreamFormat
     * @throws SQLException
     * @throws AuthorizeException
     */
    public BitstreamFormat create(Context context) throws SQLException,
            AuthorizeException
    {
        // Check authorisation - only administrators can create new formats
        if (!AuthorizeManager.isAdmin(context))
        {
            throw new AuthorizeException(
                    "Only administrators can create bitstream formats");
        }

        // Create a table row
        BitstreamFormat bitstreamFormat = bitstreamFormatDAO.create(context, new BitstreamFormat());


        log.info(LogManager.getHeader(context, "create_bitstream_format",
                "bitstream_format_id="
                        + bitstreamFormat.getID()));

        return bitstreamFormat;
    }

    /**
     * Set the short description of the bitstream format
     * 
     * @param s
     *            the new short description
     */
    public final void setShortDescription(Context context, BitstreamFormat bitstreamFormat, String s)
       throws SQLException
    {
        // You can not reset the unknown's registry's name
        BitstreamFormat unknown = null;
		try {
			unknown = findUnknown(context);
		} catch (IllegalStateException e) {
			// No short_description='Unknown' found in bitstreamformatregistry
			// table. On first load of registries this is expected because it
			// hasn't been inserted yet! So, catch but ignore this runtime 
			// exception thrown by method findUnknown.
		}
		
		// If the exception was thrown, unknown will == null so goahead and 
		// load s. If not, check that the unknown's registry's name is not
		// being reset.
		if (unknown == null || unknown.getID() != bitstreamFormat.getID()) {
            bitstreamFormat.setShortDescription(s);
		}
    }



    /**
     * Get the support level text for this bitstream format - one of
     * <code>UNKNOWN</code>,<code>KNOWN</code> or <code>SUPPORTED</code>.
     *
     * @return the support level
     */
    public String getSupportLevelText(BitstreamFormat bitstreamFormat) {
        return supportLevelText[bitstreamFormat.getSupportLevel()];
    }

    /**
     * Set the support level for this bitstream format - one of
     * <code>UNKNOWN</code>,<code>KNOWN</code> or <code>SUPPORTED</code>.
     * 
     * @param sl
     *            the new support level
     */
    public final void setSupportLevel(BitstreamFormat bitstreamFormat, int sl)
    {
        // Sanity check
        if ((sl < 0) || (sl > 2))
        {
            throw new IllegalArgumentException("Invalid support level");
        }

        bitstreamFormat.setSupportLevel(sl);
    }

    /**
     * Update the bitstream format metadata
     * 
     * @throws SQLException
     * @throws AuthorizeException
     */
    public void update(Context context, BitstreamFormat bitstreamFormat) throws SQLException, AuthorizeException
    {
        // Check authorisation - only administrators can change formats
        if (!AuthorizeManager.isAdmin(context))
        {
            throw new AuthorizeException(
                    "Only administrators can modify bitstream formats");
        }

        log.info(LogManager.getHeader(context, "update_bitstream_format",
                "bitstream_format_id=" + bitstreamFormat.getID()));

        bitstreamFormatDAO.save(context, bitstreamFormat);
    }

    /**
     * Delete this bitstream format. This converts the types of any bitstreams
     * that may have this type to "unknown". Use this with care!
     * 
     * @throws SQLException
     * @throws AuthorizeException
     */
    public void delete(Context context, BitstreamFormat bitstreamFormat) throws SQLException, AuthorizeException
    {
        // Check authorisation - only administrators can delete formats
        if (!AuthorizeManager.isAdmin(context))
        {
            throw new AuthorizeException(
                    "Only administrators can delete bitstream formats");
        }

        // Find "unknown" type
        BitstreamFormat unknown = findUnknown(context);

        if (unknown.getID() == bitstreamFormat.getID())
        {
            throw new IllegalArgumentException("The Unknown bitstream format may not be deleted.");
        }

        // Set bitstreams with this format to "unknown"
        int numberChanged = bitstreamFormatDAO.updateRemovedBitstreamFormat(context, bitstreamFormat, unknown);

        // Delete this format from database
        bitstreamFormatDAO.delete(context, bitstreamFormat);

        log.info(LogManager.getHeader(context, "delete_bitstream_format",
                "bitstream_format_id=" + bitstreamFormat.getID() + ",bitstreams_changed="
                        + numberChanged));
    }

    /**
     * If you know the support level string, look up the corresponding type ID
     * constant.
     *
     * @param slevel
     *            String with the name of the action (must be exact match)
     *
     * @return the corresponding action ID, or <code>-1</code> if the action
     *         string is unknown
     */
    public int getSupportLevelID(String slevel)
    {
        for (int i = 0; i < supportLevelText.length; i++)
        {
            if (supportLevelText[i].equals(slevel))
            {
                return i;
            }
        }

        return -1;
    }
}