package org.dspace.xmlworkflow.storedcomponents;

import org.dspace.content.Collection;
import org.dspace.core.Context;
import org.dspace.xmlworkflow.storedcomponents.dao.CollectionRoleDAO;
import org.dspace.xmlworkflow.storedcomponents.service.CollectionRoleService;

import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 21/04/14
 * Time: 14:59
 * To change this template use File | Settings | File Templates.
 */
public class CollectionRoleServiceImpl implements CollectionRoleService
{
    protected CollectionRoleDAO collectionRoleDAO;

    public CollectionRoleServiceImpl()
    {
    }

    @Override
    public CollectionRole find(Context context, int id) throws SQLException
    {
        return collectionRoleDAO.findByID(context, CollectionRole.class, id);
    }

    @Override
    public CollectionRole findByCollectionAndRole(Context context, Collection collection, String role) throws SQLException {
        return collectionRoleDAO.findByCollectionAndRole(context, collection, role);
    }

    @Override
    public List<CollectionRole> findByCollection(Context context, Collection collection) throws SQLException {
        return collectionRoleDAO.findByCollection(context, collection);
    }

    @Override
    public void deleteByCollection(Context context, Collection collection) throws SQLException {
        collectionRoleDAO.deleteByCollection(context, collection);
    }

    @Override
    public CollectionRole create(Context context) throws SQLException {
        return collectionRoleDAO.create(context, new CollectionRole());
    }

    @Override
    public void delete(Context context, CollectionRole collectionRole) throws SQLException
    {
        collectionRoleDAO.delete(context, collectionRole);
    }

    @Override
    public void update(Context context, CollectionRole collectionRole) throws SQLException, SQLException {
        collectionRoleDAO.save(context, collectionRole);
    }
}
