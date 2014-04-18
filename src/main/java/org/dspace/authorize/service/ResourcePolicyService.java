package org.dspace.authorize.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 31/03/14
 * Time: 13:32
 */
public interface ResourcePolicyService {

    public ResourcePolicy find(Context context, int id) throws SQLException;

    public ResourcePolicy create(Context context) throws SQLException, AuthorizeException;

    public List<ResourcePolicy> find(Context c, DSpaceObject o) throws SQLException;

    public List<ResourcePolicy> find(Context c, DSpaceObject o, String type) throws SQLException;

    public List<ResourcePolicy> find(Context c, DSpaceObject o, int actionId) throws SQLException;

    public List<ResourcePolicy> find(Context c, int dsoType, int dsoID, Group group, int action, int notPolicyID) throws SQLException;

    public void delete(Context context, ResourcePolicy resourcePolicy) throws SQLException, AuthorizeException;

    public void setResource(ResourcePolicy resourcePolicy, DSpaceObject o);

    public String getActionText(ResourcePolicy resourcePolicy);

    public boolean isDateValid(ResourcePolicy resourcePolicy);

    public void removeAllPolicies(Context c, DSpaceObject o) throws SQLException, AuthorizeException;

    public void removePolicies(Context c, DSpaceObject o, int actionId) throws SQLException, AuthorizeException;

    public void removePolicies(Context c, DSpaceObject o, String type) throws SQLException, AuthorizeException;

    public void removeDsoGroupPolicies(Context context, DSpaceObject dso, Group group) throws SQLException, AuthorizeException;

    public void removeDsoEPersonPolicies(Context context, DSpaceObject dso, EPerson ePerson) throws SQLException, AuthorizeException;

    public void removeGroupPolicies(Context c, Group group) throws SQLException;

    public void removeDsoAndTypeNotEqualsToPolicies(Context c, DSpaceObject o, String type) throws SQLException, AuthorizeException;

    public void update(Context context, ResourcePolicy resourcePolicy) throws SQLException, AuthorizeException;
}
