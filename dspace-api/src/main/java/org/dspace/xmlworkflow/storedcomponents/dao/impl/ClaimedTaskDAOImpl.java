package org.dspace.xmlworkflow.storedcomponents.dao.impl;

import org.dspace.core.Context;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.eperson.EPerson;
import org.dspace.xmlworkflow.storedcomponents.ClaimedTask;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;
import org.dspace.xmlworkflow.storedcomponents.dao.ClaimedTaskDAO;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 19/04/14
 * Time: 14:27
 * To change this template use File | Settings | File Templates.
 */
public class ClaimedTaskDAOImpl extends AbstractHibernateDAO<ClaimedTask>  implements ClaimedTaskDAO {

    @Override
    public List<ClaimedTask> findByWorkflowItem(Context context, XmlWorkflowItem workflowItem) throws SQLException {
        Criteria criteria = createCriteria(context, ClaimedTask.class);
        criteria.add(Restrictions.eq("workflowItem", workflowItem));

        return list(criteria);

    }

    @Override
    public ClaimedTask findByWorkflowItemAndEPerson(Context context, XmlWorkflowItem workflowItem, EPerson ePerson) throws SQLException {
        Criteria criteria = createCriteria(context, ClaimedTask.class);
        criteria.add(Restrictions.and(
                Restrictions.eq("workflowItem", workflowItem),
                Restrictions.eq("owner", ePerson)
        ));

        return uniqueResult(criteria);
    }

    @Override
    public List<ClaimedTask> findByEperson(Context context, EPerson ePerson) throws SQLException {
        Criteria criteria = createCriteria(context, ClaimedTask.class);
        criteria.add(Restrictions.eq("owner", ePerson));

        return list(criteria);
    }

    @Override
    public List<ClaimedTask> findByWorkflowItemAndStepId(Context context, XmlWorkflowItem workflowItem, String stepID) throws SQLException {
        Criteria criteria = createCriteria(context, ClaimedTask.class);
        criteria.add(Restrictions.and(
                Restrictions.eq("workflowItem", workflowItem),
                Restrictions.eq("stepId", stepID)
        ));

        return list(criteria);
    }

    @Override
    public ClaimedTask findByEPersonAndWorkflowItemAndStepIdAndActionId(Context context, EPerson ePerson, XmlWorkflowItem workflowItem, String stepID, String actionID) throws SQLException {
        Criteria criteria = createCriteria(context, ClaimedTask.class);
        criteria.add(Restrictions.and(
                Restrictions.eq("workflowItem", workflowItem),
                Restrictions.eq("owner", ePerson),
                Restrictions.eq("stepId", stepID),
                Restrictions.eq("actionId", actionID)
        ));

        return uniqueResult(criteria);
    }

    @Override
    public List<ClaimedTask> findByWorkflowItemAndStepIdAndActionId(Context context, XmlWorkflowItem workflowItem, String stepID, String actionID) throws SQLException {
        Criteria criteria = createCriteria(context, ClaimedTask.class);
        criteria.add(Restrictions.and(
                Restrictions.eq("workflowItem", workflowItem),
                Restrictions.eq("stepId", stepID),
                Restrictions.eq("actionId", actionID)
        ));

        return list(criteria);
    }

    @Override
    public List<ClaimedTask> findByStep(Context context, String stepID) throws SQLException {
        Criteria criteria = createCriteria(context, ClaimedTask.class);
        criteria.add(Restrictions.eq("stepId", stepID));

        return list(criteria);
    }
}
