package org.dspace.content;

import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 17/03/14
 * Time: 11:42
 */
public interface BitstreamDAO extends GenericDAO<Bitstream> {

    public List<Bitstream> findDeletedBitstreams(Context context) throws SQLException;

    public List<Bitstream> findDuplicateInternalIdentifier(Context context, Bitstream bitstream) throws SQLException;
}
