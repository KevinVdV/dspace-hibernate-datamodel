package org.dspace.content;

import org.dspace.core.Context;
import org.dspace.dao.AbstractDSpaceObjectDao;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 14/03/14
 * Time: 15:46
 */
public abstract class CollectionDAO extends AbstractDSpaceObjectDao<Collection> {

    public abstract List<Collection> findAll(Context context, String order) throws SQLException;

    public abstract List<Collection> findAll(Context context, String order, Integer limit, Integer offset) throws SQLException;

    public abstract Iterator<Item> getItems(Context context, Collection collection, boolean inArchive) throws SQLException;

    public abstract Iterator<Item> getItems(Context context, Collection collection, boolean inArchive, Integer limit, Integer offset) throws SQLException;

    public abstract Iterator<Item> getAllItems(Context context, Collection collection) throws SQLException;

    public abstract Collection findByTemplateItem(Context context, Item item) throws SQLException;
}
