package org.dspace.checker.service;

import org.dspace.checker.ChecksumResultCode;
import org.dspace.checker.MostRecentChecksum;
import org.dspace.content.Bitstream;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 24/04/14
 * Time: 13:51
 */
public interface MostRecentChecksumService {

    public MostRecentChecksum getNonPersistedObject();

    public MostRecentChecksum findByBitstream(Context context, Bitstream bitstream) throws SQLException;

    public List<MostRecentChecksum> findNotProcessedBitstreamsReport(Context context, Date startDate, Date endDate) throws SQLException;

    public List<MostRecentChecksum> findBitstreamResultTypeReport(Context context, Date startDate, Date endDate, ChecksumResultCode resultCode) throws SQLException;

    public void updateMissingBitstreams(Context context) throws SQLException;

    public void deleteByBitstream(Context context, Bitstream bitstream) throws SQLException;

    public MostRecentChecksum findOldestRecord(Context context) throws SQLException;

    public MostRecentChecksum findOldestRecord(Context context, Date lessThanDate) throws SQLException;

    public List<MostRecentChecksum> findNotInHistory(Context context) throws SQLException;

    public void update(Context context, MostRecentChecksum mostRecentChecksum) throws SQLException;
}
