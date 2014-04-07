package org.dspace.authorize;

import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 31/03/14
 * Time: 13:26
 */
public interface ResourcePolicyDAO extends GenericDAO<ResourcePolicy>{


    public List<ResourcePolicy> findByDso(Context context, DSpaceObject dso) throws SQLException;

    public List<ResourcePolicy> findByDsoAndType(Context context, DSpaceObject dSpaceObject, String type) throws SQLException;

    public List<ResourcePolicy> findByGroup(Context context, Group group) throws SQLException;

    public List<ResourcePolicy> findByDSoAndAction(Context context, DSpaceObject dso, int actionId) throws SQLException;

    public List<ResourcePolicy> findByTypeIdGroupAction(Context context, int dsoType, int dsoID, Group group, int action, int notPolicyID) throws SQLException;

    public void deleteByDso(Context context, DSpaceObject dso) throws SQLException;

    public void deleteByDsoAndAction(Context context, DSpaceObject dso, int actionId) throws SQLException;

    public void deleteByDsoAndType(Context context, DSpaceObject dSpaceObject, String type) throws SQLException;

    public void deleteByGroup(Context context, Group group) throws SQLException;

    public void deleteByDsoGroupPolicies(Context context, DSpaceObject dso, Group group) throws SQLException;

    public void deleteByDsoEPersonPolicies(Context context, DSpaceObject dso, EPerson ePerson) throws SQLException;

    public void deleteByDSOAndTypeNotEqualsTo(Context c, DSpaceObject o, String type) throws SQLException;
}
