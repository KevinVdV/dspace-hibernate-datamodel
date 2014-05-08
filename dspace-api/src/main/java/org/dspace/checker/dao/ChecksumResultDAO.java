package org.dspace.checker.dao;

import org.dspace.checker.ChecksumResult;
import org.dspace.checker.ChecksumResultCode;
import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 23/04/14
 * Time: 15:30
 */
public interface ChecksumResultDAO extends GenericDAO<ChecksumResult> {

    public ChecksumResult findByCode(Context context, ChecksumResultCode code) throws SQLException;

}
