package org.dspace.content.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.core.Context;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 31/03/14
 * Time: 10:26
 */
public interface BitstreamService extends DSpaceObjectService<Bitstream> {

    public List<Bitstream> findAll(Context context) throws SQLException;

    //TODO: add collecton, community here ?
    public Bitstream create(Context context, InputStream is) throws IOException, SQLException, AuthorizeException;

    public Bitstream create(Context context, Bundle bundle, InputStream is) throws AuthorizeException, IOException, SQLException;

    public Bitstream register(Context context, Bundle bundle, int assetstore, String bitstreamPath) throws AuthorizeException, IOException, SQLException;

    public Bitstream register(Context context, int assetstore, String bitstreamPath) throws IOException, SQLException, AuthorizeException;

    public List<Bitstream> findDeletedBitstreams(Context context) throws SQLException;

    public List<Bitstream> findDuplicateInternalIdentifier(Context context, Bitstream bitstream) throws SQLException;

    public Iterator<Bitstream> findAllInCommunity(Context context, Community community) throws SQLException;

    public Iterator<Bitstream> findAllInCollection(Context context, Collection collection) throws SQLException;

    public Iterator<Bitstream> findAllInItem(Context context, Item item) throws SQLException;

    public void setUserFormatDescription(Context context, Bitstream bitstream, String desc) throws SQLException;

    public String getFormatDescription(Bitstream bitstream);

    public void setFormat(Context context, Bitstream bitstream, BitstreamFormat f) throws SQLException;

    public void delete(Context context, Bitstream bitstream) throws SQLException, AuthorizeException;

    public void expunge(Context context, Bitstream bitstream) throws SQLException, AuthorizeException;

    public InputStream retrieve(Context context, Bitstream bitstream) throws IOException, SQLException, AuthorizeException;

    public boolean isRegisteredBitstream(Bitstream bitstream);

    /**
     * Find all bitstreams that the checksum checker is currently not aware of
     *
     * @return a List of DSpaceBitstreamInfo objects
     */
    public List<Bitstream> findBitstreamsWithNoRecentChecksum(Context context) throws SQLException;

}
