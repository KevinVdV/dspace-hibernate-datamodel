package org.dspace.xmlworkflow.storedcomponents.dao;

import org.dspace.content.Collection;
import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;
import org.dspace.xmlworkflow.storedcomponents.CollectionRole;

import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 21/04/14
 * Time: 14:58
 * To change this template use File | Settings | File Templates.
 */
public interface CollectionRoleDAO extends GenericDAO<CollectionRole> {

    public List<CollectionRole> findByCollection(Context context, Collection collection) throws SQLException;

    public CollectionRole findByCollectionAndRole(Context context, Collection collection, String role) throws SQLException;

    public void deleteByCollection(Context context, Collection collection) throws SQLException;
}
