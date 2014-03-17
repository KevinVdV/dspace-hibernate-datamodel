package org.dspace.content;

import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.dao.AbstractDSpaceObjectDao;
import org.dspace.dao.GenericDAO;
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
public interface ItemDAO extends GenericDAO<Item> {

    public Iterator<Item> findAll(Context context, boolean archived) throws SQLException;

    public Iterator<Item> findAll(Context context, boolean archived, boolean withdrawn) throws SQLException;

    public Iterator<Item> findBySubmitter(Context context, EPerson eperson) throws SQLException;

    public Iterator<Item> findByMetadataField(Context context, MetadataField metadataField, String value, boolean inArchive) throws SQLException;

    public Iterator<Item> findByAuthorityValue(Context context, MetadataField metadataField, String authority, boolean inArchive) throws SQLException;
}