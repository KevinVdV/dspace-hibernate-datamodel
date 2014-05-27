package org.dspace.authorize.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 27/05/14
 * Time: 08:50
 */
public interface AuthorizeService {

    public void authorizeAnyOf(Context c, DSpaceObject o, int[] actions) throws AuthorizeException, SQLException;

    public void authorizeAction(Context c, DSpaceObject o, int action) throws AuthorizeException, SQLException;

    public void authorizeAction(Context c, DSpaceObject o, int action, boolean useInheritance) throws AuthorizeException, SQLException;

    public boolean authorizeActionBoolean(Context c, DSpaceObject o, int a) throws SQLException;

    public boolean authorizeActionBoolean(Context c, DSpaceObject o, int a, boolean useInheritance) throws SQLException;

    public boolean isAdmin(Context c, DSpaceObject o) throws SQLException;

    public boolean isAdmin(Context c) throws SQLException;

    public void addPolicy(Context c, DSpaceObject o, int actionID, EPerson e) throws SQLException, AuthorizeException;

    public void addPolicy(Context context, DSpaceObject o, int actionID, EPerson e, String type) throws SQLException, AuthorizeException;

    public void addPolicy(Context c, DSpaceObject o, int actionID, Group g) throws SQLException, AuthorizeException;

    public void addPolicy(Context c, DSpaceObject o, int actionID, Group g, String type) throws SQLException, AuthorizeException;

    public List<ResourcePolicy> getPolicies(Context c, DSpaceObject o) throws SQLException;

    public List<ResourcePolicy> findPoliciesByDsoAndType(Context c, DSpaceObject o, String type) throws SQLException;

    public List<ResourcePolicy> getPoliciesForGroup(Context c, Group g) throws SQLException;

    public List<ResourcePolicy> getPoliciesActionFilter(Context c, DSpaceObject o, int actionID) throws SQLException;

    public void inheritPolicies(Context c, DSpaceObject src, DSpaceObject dest) throws SQLException, AuthorizeException;

    public void addPolicies(Context c, List<ResourcePolicy> policies, DSpaceObject dest) throws SQLException, AuthorizeException;

    public void removeAllPolicies(Context c, DSpaceObject o) throws SQLException, AuthorizeException;

    public void removeAllPoliciesByDsoAndTypeNotEqualsTo(Context c, DSpaceObject o, String type) throws SQLException, AuthorizeException;

    public void removeAllPoliciesByDsoAndType(Context c, DSpaceObject o, String type) throws SQLException, AuthorizeException;

    public void removePoliciesActionFilter(Context context, DSpaceObject dso, int actionID) throws SQLException, AuthorizeException;

    public void removeGroupPolicies(Context c, Group group) throws SQLException;

    public void removeGroupPolicies(Context c, DSpaceObject o, Group g) throws SQLException, AuthorizeException;

    public void removeEPersonPolicies(Context c, DSpaceObject o, EPerson e) throws SQLException, AuthorizeException;

    public List<Group> getAuthorizedGroups(Context c, DSpaceObject o, int actionID) throws java.sql.SQLException;

    public boolean isAnIdenticalPolicyAlreadyInPlace(Context c, DSpaceObject o, ResourcePolicy rp) throws SQLException;

    public boolean isAnIdenticalPolicyAlreadyInPlace(Context c, DSpaceObject o, Group groupID, int action, int policyID) throws SQLException;

    public boolean isAnIdenticalPolicyAlreadyInPlace(Context c, int dsoType, int dsoID, Group group, int action, int policyID) throws SQLException;

    public ResourcePolicy findByTypeIdGroupAction(Context c, int dsoType, int dsoID, Group group, int action, int policyID) throws SQLException;

    public void generateAutomaticPolicies(Context context, Date embargoDate, String reason, DSpaceObject dso, org.dspace.content.Collection owningCollection) throws SQLException, AuthorizeException;

    public void createResourcePolicy(Context context, DSpaceObject dso, Group group, EPerson eperson, int type) throws SQLException, AuthorizeException;

    public ResourcePolicy createOrModifyPolicy(ResourcePolicy policy, Context context, String name, Group group, EPerson ePerson, Date embargoDate, int action, String reason, DSpaceObject dso) throws AuthorizeException, SQLException;


}
