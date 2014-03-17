package org.dspace.content;

import org.dspace.core.Context;
import org.dspace.dao.AbstractDSpaceObjectDao;
import org.dspace.dao.GenericDAO;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 14/03/14
 * Time: 15:46
 */
public interface CollectionDAO extends GenericDAO<Collection> {

    public List<Collection> findAll(Context context, String order) throws SQLException;

    public List<Collection> findAll(Context context, String order, Integer limit, Integer offset) throws SQLException;

    public Iterator<Item> getItems(Context context, Collection collection, boolean inArchive) throws SQLException;

    public Iterator<Item> getItems(Context context, Collection collection, boolean inArchive, Integer limit, Integer offset) throws SQLException;

    public Iterator<Item> getAllItems(Context context, Collection collection) throws SQLException;

    public Collection findByTemplateItem(Context context, Item item) throws SQLException;
}
