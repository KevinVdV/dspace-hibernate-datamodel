package org.dspace.workflowbasic.dao;

import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;
import org.dspace.eperson.EPerson;
import org.dspace.workflowbasic.BasicWorkflowItem;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 7/04/14
 * Time: 16:05
 */
public interface BasicWorkflowItemDAO extends GenericDAO<BasicWorkflowItem> {

    public BasicWorkflowItem findByItem(Context context, Item i) throws SQLException;

    public List<BasicWorkflowItem> findBySubmitter(Context context, EPerson ep) throws SQLException;

    public List<BasicWorkflowItem> findByCollection(Context context, Collection c) throws SQLException;

    public List<BasicWorkflowItem> findByPooledTasks(Context context, EPerson ePerson) throws SQLException;
}
