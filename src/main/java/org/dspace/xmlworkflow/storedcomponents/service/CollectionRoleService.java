package org.dspace.xmlworkflow.storedcomponents.service;

import org.dspace.content.Collection;
import org.dspace.core.Context;
import org.dspace.xmlworkflow.storedcomponents.CollectionRole;

import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 21/04/14
 * Time: 15:09
 * To change this template use File | Settings | File Templates.
 */
public interface CollectionRoleService {

    public CollectionRole find(Context context, int id) throws SQLException;

    public CollectionRole findByCollectionAndRole(Context context, Collection collection, String role) throws SQLException;

    public List<CollectionRole> findByCollection(Context context, Collection collection) throws SQLException;

    public CollectionRole create(Context context) throws SQLException;

    public void delete(Context context, CollectionRole collectionRole) throws SQLException;

    public void update(Context context, CollectionRole collectionRole) throws SQLException, SQLException;
}
