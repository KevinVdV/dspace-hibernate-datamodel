package org.dspace.storage.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.core.Context;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 27/05/14
 * Time: 11:40
 */
public interface BitstreamStorageService {

    public int store(Context context, Bitstream bitstream, InputStream is)
            throws SQLException, IOException, AuthorizeException;

    public void register(Context context, Bitstream bitstream, int assetstore,
    				String bitstreamPath) throws SQLException, IOException, AuthorizeException;

    public boolean isRegisteredBitstream(String internalId);

    public InputStream retrieve(Context context, int id)
            throws SQLException, IOException;

    public void cleanup(boolean deleteDbRecords, boolean verbose) throws SQLException, IOException, AuthorizeException;

    public int clone(Context context, int id) throws SQLException;
}
