package org.dspace.content;

import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 31/03/14
 * Time: 10:37
 */
public interface BitstreamFormatRepo {

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
    public static final String supportLevelText[] =
        { "UNKNOWN", "KNOWN", "SUPPORTED" };


    public BitstreamFormat find(Context context, int id) throws SQLException;

    public BitstreamFormat findByMIMEType(Context context, String mimeType) throws SQLException;

    public BitstreamFormat findByShortDescription(Context context, String desc) throws SQLException;

    public BitstreamFormat findUnknown(Context context) throws SQLException;

    public List<BitstreamFormat> findAll(Context context) throws SQLException;

    public List<BitstreamFormat> findNonInternal(Context context) throws SQLException;

    public BitstreamFormat create(Context context) throws SQLException, AuthorizeException;

    public void setShortDescription(Context context, BitstreamFormat bitstreamFormat, String s) throws SQLException;

    public String getSupportLevelText(BitstreamFormat bitstreamFormat);

    public void setSupportLevel(BitstreamFormat bitstreamFormat, int sl);

    public void update(Context context, BitstreamFormat bitstreamFormat) throws SQLException, AuthorizeException;

    public void delete(Context context, BitstreamFormat bitstreamFormat) throws SQLException, AuthorizeException;

    public int getSupportLevelID(String slevel);
}
