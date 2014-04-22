package org.dspace.xmlworkflow.storedcomponents.dao.impl;

import org.dspace.core.Context;
import org.dspace.dao.AbstractHibernateDAO;
import org.dspace.eperson.EPerson;
import org.dspace.xmlworkflow.storedcomponents.InProgressUser;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;
import org.dspace.xmlworkflow.storedcomponents.dao.InProgressUserDAO;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 22/04/14
 * Time: 11:32
 */
public class InProgressUserDAOImpl extends AbstractHibernateDAO<InProgressUser> implements InProgressUserDAO {

    public InProgressUser findByWorkflowItemAndEPerson(Context context, XmlWorkflowItem workflowItem, EPerson ePerson) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(InProgressUser.class);
        criteria.add(
                Restrictions.and(
                        Restrictions.eq("workflowItem", workflowItem),
                        Restrictions.eq("ePerson", ePerson)
                )
        );

        @SuppressWarnings("unchecked")
        InProgressUser inProgressUser = (InProgressUser) criteria.uniqueResult();
        return inProgressUser;
    }

    public List<InProgressUser> findByEperson(Context context, EPerson ePerson) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(InProgressUser.class);
        criteria.add(Restrictions.eq("ePerson", ePerson));

        @SuppressWarnings("unchecked")
        List<InProgressUser> inProgressUsers = (List<InProgressUser>) criteria.list();
        return inProgressUsers;
    }

    public List<InProgressUser> findByWorkflowItem(Context context, XmlWorkflowItem workflowItem) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(InProgressUser.class);
        criteria.add(Restrictions.eq("workflowItem", workflowItem));

        @SuppressWarnings("unchecked")
        List<InProgressUser> inProgressUsers = (List<InProgressUser>) criteria.list();
        return inProgressUsers;
    }

    public int countInProgressUsers(Context context, XmlWorkflowItem workflowItem) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(InProgressUser.class);
        criteria.add(
                Restrictions.and(
                        Restrictions.eq("workflowItem", workflowItem),
                        Restrictions.eq("finished", false)
                )
        );

        return ((Long) criteria.setProjection(Projections.rowCount()).uniqueResult()).intValue();
    }

    public int countFinishedUsers(Context context, XmlWorkflowItem workflowItem) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(InProgressUser.class);
        criteria.add(
                Restrictions.and(
                        Restrictions.eq("workflowItem", workflowItem),
                        Restrictions.eq("finished", true)
                )
        );
        return ((Long) criteria.setProjection(Projections.rowCount()).uniqueResult()).intValue();
    }
}
