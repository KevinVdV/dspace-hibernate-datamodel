package org.dspace.authorize.dao.impl;

import org.dspace.authorize.ResourcePolicy;
import org.dspace.authorize.dao.ResourcePolicyDAO;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.dao.AbstractHibernateDAO;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 31/03/14
 * Time: 13:26
 */
public class ResourcePolicyDAOImpl extends AbstractHibernateDAO<ResourcePolicy> implements ResourcePolicyDAO
{

    public List<ResourcePolicy> findByDso(Context context, DSpaceObject dso) throws SQLException {
        Criteria criteria = createCriteria(context, ResourcePolicy.class);
        criteria.add(Restrictions.and(
                Restrictions.eq("resource_type_id", dso.getType()),
                Restrictions.eq("resource_id", dso.getID())
        ));
        return list(criteria);
    }

    public List<ResourcePolicy> findByDsoAndType(Context context, DSpaceObject dso, String type) throws SQLException
    {
        Criteria criteria = createCriteria(context, ResourcePolicy.class);
        criteria.add(Restrictions.and(
                Restrictions.eq("resource_type_id", dso.getType()),
                Restrictions.eq("resource_id", dso.getID()),
                Restrictions.eq("rptype", type)
        ));
        return list(criteria);
    }

    public List<ResourcePolicy> findByGroup(Context context, Group group) throws SQLException {
        Criteria criteria = createCriteria(context, ResourcePolicy.class);
        criteria.add(Restrictions.eq("epersonGroup", group));
        return list(criteria);
    }

    public List<ResourcePolicy> findByDSoAndAction(Context context, DSpaceObject dso, int actionId) throws SQLException
    {
        Criteria criteria = createCriteria(context, ResourcePolicy.class);
        criteria.add(Restrictions.and(
                Restrictions.eq("resource_type_id", dso.getType()),
                Restrictions.eq("resource_id", dso.getID()),
                Restrictions.eq("action_id", actionId)
        ));
        return list(criteria);
    }

    public List<ResourcePolicy> findByTypeIdGroupAction(Context context, int dsoType, int dsoID, Group group, int action, int notPolicyID) throws SQLException {
        Criteria criteria = createCriteria(context, ResourcePolicy.class);
        criteria.add(Restrictions.and(
                Restrictions.eq("resource_type_id", dsoType),
                Restrictions.eq("resource_id", dsoID),
                Restrictions.eq("epersonGroup", group),
                Restrictions.eq("action_id", action)
        ));
        criteria.setMaxResults(1);

        List<ResourcePolicy> results;
        if (notPolicyID != -1)
        {
            criteria.add(Restrictions.and(Restrictions.not(Restrictions.eq("id", action))));
        }

        return list(criteria);
    }

    public void deleteByDso(Context context, DSpaceObject dso) throws SQLException
    {
        String queryString = "delete from ResourcePolicy where resource_type_id= :resourceTypeId AND resource_id = :resourceId";
        Query query = context.getDBConnection().createQuery(queryString);
        query.setInteger("resourceTypeId", dso.getType());
        query.setInteger("resourceId", dso.getID());
        query.executeUpdate();
    }

    public void deleteByDsoAndAction(Context context, DSpaceObject dso, int actionId) throws SQLException {
        String queryString = "delete from ResourcePolicy where resource_type_id= :resourceTypeId AND resource_id = :resourceId AND action_id= :actionId";
        Query query = context.getDBConnection().createQuery(queryString);
        query.setInteger("resourceTypeId", dso.getType());
        query.setInteger("resourceId", dso.getID());
        query.setInteger("actionId", actionId);
        query.executeUpdate();
    }

    public void deleteByDsoAndType(Context context, DSpaceObject dso, String type) throws SQLException {
        String queryString = "delete from ResourcePolicy where resource_type_id= :resourceTypeId AND rptype = :rptype";
        Query query = context.getDBConnection().createQuery(queryString);
        query.setInteger("resourceTypeId", dso.getType());
        query.setString("rptype", type);
        query.executeUpdate();
    }

    public void deleteByGroup(Context context, Group group) throws SQLException {
        String queryString = "delete from ResourcePolicy where epersonGroup= :epersonGroup";
        Query query = context.getDBConnection().createQuery(queryString);
        query.setParameter("epersonGroup", group);
        query.executeUpdate();
    }

    public void deleteByDsoGroupPolicies(Context context, DSpaceObject dso, Group group) throws SQLException {
        String queryString = "delete from ResourcePolicy where resource_type_id= :resourceTypeId AND resource_id = :resourceId AND epersonGroup= :epersonGroup";
        Query query = context.getDBConnection().createQuery(queryString);
        query.setInteger("resourceTypeId", dso.getType());
        query.setInteger("resourceId", dso.getID());
        query.setParameter("epersonGroup", group);
        query.executeUpdate();

    }

    public void deleteByDsoEPersonPolicies(Context context, DSpaceObject dso, EPerson ePerson) throws SQLException {
        String queryString = "delete from ResourcePolicy where resource_type_id= :resourceTypeId AND resource_id = :resourceId AND eperson= :eperson";
        Query query = context.getDBConnection().createQuery(queryString);
        query.setInteger("resourceTypeId", dso.getType());
        query.setInteger("resourceId", dso.getID());
        query.setParameter("eperson", ePerson);
        query.executeUpdate();

    }

    public void deleteByDSOAndTypeNotEqualsTo(Context context, DSpaceObject o, String type) throws SQLException {

        Criteria criteria = createCriteria(context, ResourcePolicy.class);
        criteria.add(Restrictions.and(
                Restrictions.eq("resource_type_id", o.getType()),
                Restrictions.eq("resource_id", o.getID()),
                Restrictions.not(Restrictions.eq("rptype", type))
        ));
        //TODO: test this, prob doesn't work !
        List<ResourcePolicy> list = list(criteria);
        for (ResourcePolicy resourcePolicy : list) {
            context.getDBConnection().delete(resourcePolicy);
        }
    }
}
