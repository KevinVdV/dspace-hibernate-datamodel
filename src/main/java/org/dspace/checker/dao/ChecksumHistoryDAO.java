package org.dspace.checker.dao;

import org.dspace.checker.ChecksumHistory;
import org.dspace.checker.ChecksumResultCode;
import org.dspace.content.Bitstream;
import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;

import java.sql.SQLException;
import java.util.Date;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 25/04/14
 * Time: 12:43
 */
public interface ChecksumHistoryDAO extends GenericDAO<ChecksumHistory> {
    public int deleteByDateAndCode(Context context, Date retentionDate, ChecksumResultCode checksumResultCode) throws SQLException;

    public void deleteByBitstream(Context context, Bitstream bitstream) throws SQLException;
}
