package org.dspace.content;

import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 17/03/14
 * Time: 12:54
 */
public interface BitstreamFormatDAO extends GenericDAO<BitstreamFormat>
{

    public BitstreamFormat findByMIMEType(Context context, String mimeType) throws SQLException;

    public BitstreamFormat findByShortDescription(Context context, String desc) throws SQLException;

    public int updateRemovedBitstreamFormat(Context context, BitstreamFormat deletedBitstreamFormat, BitstreamFormat newBitstreamFormat) throws SQLException;

    public List<BitstreamFormat> findNonInternal(Context context) throws SQLException;
}
