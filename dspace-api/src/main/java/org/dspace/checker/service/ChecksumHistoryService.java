package org.dspace.checker.service;

import org.dspace.checker.ChecksumResultCode;
import org.dspace.checker.MostRecentChecksum;
import org.dspace.content.Bitstream;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 28/04/14
 * Time: 10:07
 */
public interface ChecksumHistoryService {

    public void updateMissingBitstreams(Context context) throws SQLException;

    public void addHistory(Context context, MostRecentChecksum mostRecentChecksum) throws SQLException;

    public int deleteByDateAndCode(Context context, Date retentionDate, ChecksumResultCode result) throws SQLException;

    public void deleteByBitstream(Context context, Bitstream bitstream) throws SQLException;

    /**
     * Prune the history records from the database.
     *
     * @param interests
     *            set of results and the duration of time before they are
     *            removed from the database
     *
     * @return number of bitstreams deleted
     */
    public int prune(Context context, Map<ChecksumResultCode, Long> interests) throws SQLException;
}
