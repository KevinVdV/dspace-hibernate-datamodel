package org.dspace.content;

import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 31/03/14
 * Time: 10:26
 */
public interface BitstreamRepo extends DSpaceObjectRepo<Bitstream>{

    public Bitstream find(Context context, int id) throws SQLException;

    public List<Bitstream> findAll(Context context) throws SQLException;

    public Bitstream create(Context context, InputStream is) throws IOException, SQLException, AuthorizeException;

    public Bitstream register(Context context, int assetstore, String bitstreamPath) throws IOException, SQLException, AuthorizeException;

    public void setUserFormatDescription(Context context, Bitstream bitstream, String desc) throws SQLException;

    public String getFormatDescription(Bitstream bitstream);

    public void setFormat(Context context, Bitstream bitstream, BitstreamFormat f) throws SQLException;

    public void delete(Context context, Bitstream bitstream) throws SQLException, AuthorizeException;

    public InputStream retrieve(Context context, Bitstream bitstream) throws IOException, SQLException, AuthorizeException;

    public boolean isRegisteredBitstream(Bitstream bitstream);
}
