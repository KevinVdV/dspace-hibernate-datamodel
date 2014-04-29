package org.dspace.checker;

import org.dspace.checker.dao.ChecksumResultDAO;
import org.dspace.checker.service.ChecksumResultService;
import org.dspace.content.Bitstream;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.*;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 23/04/14
 * Time: 15:28
 */
public class ChecksumResultServiceImpl implements ChecksumResultService {

    @Autowired(required = true)
    private ChecksumResultDAO checksumResultDAO;

    /**
     * Get the result description for the given result code
     *
     * @param code
     *            to get the description for.
     * @return the found description.
     */
    public ChecksumResult findByCode(Context context, ChecksumResultCode code) throws SQLException
    {
        return checksumResultDAO.findByCode(context, code);
    }

    /**
     * Get a list of all the possible result codes.
     *
     * @return a list of all the result codes
     */
    public List<ChecksumResult> findAll(Context context) throws SQLException {
        return checksumResultDAO.findAll(context, ChecksumResult.class);
    }
}
