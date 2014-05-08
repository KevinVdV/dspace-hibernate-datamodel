package org.dspace.checker.service;

import org.dspace.checker.ChecksumResult;
import org.dspace.checker.ChecksumResultCode;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 24/04/14
 * Time: 13:38
 */
public interface ChecksumResultService {

    public ChecksumResult findByCode(Context context, ChecksumResultCode code) throws SQLException;

    public List<ChecksumResult> findAll(Context context) throws SQLException;
}
