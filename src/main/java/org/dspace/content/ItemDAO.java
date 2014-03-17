package org.dspace.content;

import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.dao.AbstractDSpaceObjectDao;
import org.dspace.eperson.EPerson;

import java.sql.SQLException;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 16/03/14
 * Time: 09:10
 * To change this template use File | Settings | File Templates.
 */
public abstract class ItemDAO extends AbstractDSpaceObjectDao<Item> {

    public abstract Iterator<Item> findAll(Context context, boolean archived) throws SQLException;

    public abstract Iterator<Item> findAll(Context context, boolean archived, boolean withdrawn) throws SQLException;

    public abstract Iterator<Item> findBySubmitter(Context context, EPerson eperson) throws SQLException;

    public abstract Iterator<Item> findByMetadataField(Context context, MetadataField metadataField, String value, boolean inArchive) throws SQLException;

    public abstract Iterator<Item> findByAuthorityValue(Context context, MetadataField metadataField, String authority, boolean inArchive) throws SQLException;
}