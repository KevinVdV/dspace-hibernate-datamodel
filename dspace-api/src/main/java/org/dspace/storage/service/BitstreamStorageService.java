package org.dspace.storage.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.core.Context;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.UUID;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 27/05/14
 * Time: 11:40
 */
public interface BitstreamStorageService {

    public UUID store(Context context, Bitstream bitstream, InputStream is)
            throws SQLException, IOException, AuthorizeException;

    public void register(Context context, Bitstream bitstream, int assetstore,
    				String bitstreamPath) throws SQLException, IOException, AuthorizeException;

    public boolean isRegisteredBitstream(String internalId);

    public InputStream retrieve(Context context, UUID id)
            throws SQLException, IOException;

    public void cleanup(boolean deleteDbRecords, boolean verbose) throws SQLException, IOException, AuthorizeException;

    public UUID clone(Context context, UUID id) throws SQLException;
}
