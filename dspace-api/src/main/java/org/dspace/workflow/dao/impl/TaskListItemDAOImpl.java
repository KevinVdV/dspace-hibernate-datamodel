package org.dspace.workflow.dao.impl;

import org.dspace.core.Context;
import org.dspace.dao.AbstractHibernateDAO;
import org.dspace.workflow.TaskListItem;
import org.dspace.workflow.WorkflowItem;
import org.dspace.workflow.dao.TaskListItemDAO;
import org.hibernate.Query;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 8/04/14
 * Time: 09:11
 */
public class TaskListItemDAOImpl extends AbstractHibernateDAO<TaskListItem> implements TaskListItemDAO
{

    @Override
    public void deleteByWorkflowItem(Context context, WorkflowItem workflowItem) throws SQLException {
        String queryString = "delete from TaskListItem where workflowItem = :workflowItem";
        Query query = createQuery(context, queryString);
        query.setParameter("workflowItem", workflowItem);
        query.executeUpdate();
    }
}
