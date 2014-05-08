package org.dspace.checker.dao;

import org.dspace.checker.ChecksumResultCode;
import org.dspace.checker.MostRecentChecksum;
import org.dspace.content.Bitstream;
import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 24/04/14
 * Time: 10:12
 */
public interface MostRecentChecksumDAO extends GenericDAO<MostRecentChecksum>
{

    public List<MostRecentChecksum> findByNotProcessedInDateRange(Context context, Date startDate, Date endDate) throws SQLException;

    public List<MostRecentChecksum> findByResultTypeInDateRange(Context context, Date startDate, Date endDate, ChecksumResultCode resultCode) throws SQLException;

    public List<Bitstream> findNotLinkedBitstreams(Context context) throws SQLException;

    public void deleteByBitstream(Context context, Bitstream bitstream) throws SQLException;

    public MostRecentChecksum getOldestRecord(Context context) throws SQLException;

    public MostRecentChecksum getOldestRecord(Context context, Date lessThanDate) throws SQLException;

    public List<MostRecentChecksum> findNotInHistory(Context context) throws SQLException;

    public MostRecentChecksum findByBitstream(Context context, Bitstream bitstream) throws SQLException;
}
