package org.dspace.content.dao;

import org.dspace.content.Bitstream;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 17/03/14
 * Time: 11:42
 */
public interface BitstreamDAO extends DSpaceObjectDAO<Bitstream> {

    public List<Bitstream> findDeletedBitstreams(Context context) throws SQLException;

    public List<Bitstream> findDuplicateInternalIdentifier(Context context, Bitstream bitstream) throws SQLException;

    public List<Bitstream> findBitstreamsWithNoRecentChecksum(Context context) throws SQLException;

    public Iterator<Bitstream> findByCommunity(Context context, Community community) throws SQLException;

    public Iterator<Bitstream> findByCollection(Context context, Collection collection) throws SQLException;

    public Iterator<Bitstream> findByItem(Context context, Item item) throws SQLException;
}
