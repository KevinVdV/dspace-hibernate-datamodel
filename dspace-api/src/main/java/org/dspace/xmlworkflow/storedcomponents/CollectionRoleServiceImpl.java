package org.dspace.xmlworkflow.storedcomponents;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.core.Context;
import org.dspace.eperson.Group;
import org.dspace.eperson.service.GroupService;
import org.dspace.xmlworkflow.Role;
import org.dspace.xmlworkflow.WorkflowConfigurationException;
import org.dspace.xmlworkflow.factory.XmlWorkflowFactory;
import org.dspace.xmlworkflow.state.Workflow;
import org.dspace.xmlworkflow.storedcomponents.dao.CollectionRoleDAO;
import org.dspace.xmlworkflow.storedcomponents.service.CollectionRoleService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
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

    @Autowired(required = true)
    protected XmlWorkflowFactory workflowFactory;
    @Autowired(required = true)
    protected GroupService groupService;

    public CollectionRoleServiceImpl()
    {
    }

    @Override
    public CollectionRole find(Context context, int id) throws SQLException
    {
        return collectionRoleDAO.findByID(context, CollectionRole.class, id);
    }

    @Override
    public CollectionRole create(Context context, Collection collection, String roleId, Group group) throws SQLException, AuthorizeException {
        CollectionRole collectionRole = collectionRoleDAO.create(context, new CollectionRole());
        collectionRole.setCollection(collection);
        collectionRole.setRoleId(roleId);
        collectionRole.setGroup(group);
        update(context, collectionRole);
        return collectionRole;
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
    public void deleteByCollectionAndRole(Context context, Collection collection, String roleId) throws SQLException, IOException, WorkflowConfigurationException {
        Workflow workflow = workflowFactory.getWorkflow(context, collection);
        Role role = workflow.getRoles().get(roleId);
        if(role.getScope() == Role.Scope.COLLECTION){
            CollectionRole collectionRole = findByCollectionAndRole(context, collection, roleId);
            delete(context, collectionRole);
        }
    }

    public Group getRoleGroupForCollection(Context context, Collection collection, Role role) throws SQLException {
        if(role.getScope() == Role.Scope.REPOSITORY){
            return groupService.findByName(context, role.getName());
        }else
        if(role.getScope() == Role.Scope.COLLECTION){
            CollectionRole collectionRole = findByCollectionAndRole(context, collection, role.getId());
        if(collectionRole == null)
            return null;

            return collectionRole.getGroup();
        }else
        if(role.getScope() == Role.Scope.ITEM){

        }
        return null;
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
