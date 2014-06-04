package org.dspace.content.dao;

import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.eperson.Group;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 14/03/14
 * Time: 15:46
 */
public interface CollectionDAO extends DSpaceObjectDAO<Collection> {

    public List<Collection> findAll(Context context, String order) throws SQLException;

    public List<Collection> findAll(Context context, String order, Integer limit, Integer offset) throws SQLException;

    public Collection findByTemplateItem(Context context, Item item) throws SQLException;

    public Collection findByGroup(Context context, Group group) throws SQLException;
}
