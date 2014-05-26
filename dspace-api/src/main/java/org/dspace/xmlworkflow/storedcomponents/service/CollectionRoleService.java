package org.dspace.xmlworkflow.storedcomponents.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.core.Context;
import org.dspace.eperson.Group;
import org.dspace.service.DSpaceCRUDService;
import org.dspace.xmlworkflow.WorkflowConfigurationException;
import org.dspace.xmlworkflow.storedcomponents.CollectionRole;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 21/04/14
 * Time: 15:09
 * To change this template use File | Settings | File Templates.
 */
public interface CollectionRoleService
{
    public CollectionRole create(Context context, Collection collection, String roleId, Group group) throws SQLException, AuthorizeException;

    public CollectionRole findByCollectionAndRole(Context context, Collection collection, String role) throws SQLException;

    public CollectionRole find(Context context, int id) throws SQLException;

    public List<CollectionRole> findByCollection(Context context, Collection collection) throws SQLException;

    public void deleteByCollection(Context context, Collection collection) throws SQLException;

    public void update(Context context, CollectionRole collectionRole) throws SQLException, AuthorizeException;

    public void deleteByCollectionAndRole(Context context, Collection collection, String roleId) throws SQLException, IOException, WorkflowConfigurationException;

    public void delete(Context context, CollectionRole collectionRole) throws SQLException, AuthorizeException;

}
