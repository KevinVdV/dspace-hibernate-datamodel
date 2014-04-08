package org.dspace.workflow;

import org.dspace.core.Context;
import org.dspace.dao.AbstractHibernateDAO;
import org.hibernate.Query;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 8/04/14
 * Time: 09:11
 */
public class TaskListItemDAOImpl extends AbstractHibernateDAO<TaskListItem> implements TaskListItemDAO
{

    public void deleteByWorkflowItem(Context context, WorkflowItem workflowItem) throws SQLException {
        String queryString = "delete from TaskListItem where workflowItem = :workflowItem";
        Query query = context.getDBConnection().createQuery(queryString);
        query.setParameter("workflowItem", workflowItem);
        query.executeUpdate();
    }
}
