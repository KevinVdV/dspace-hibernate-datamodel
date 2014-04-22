package org.dspace.xmlworkflow.storedcomponents.dao;

import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.xmlworkflow.storedcomponents.PoolTask;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;

import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 21/04/14
 * Time: 15:54
 * To change this template use File | Settings | File Templates.
 */
public interface PoolTaskDAO extends GenericDAO<PoolTask> {

    public List<PoolTask> findByEPerson(Context context, EPerson ePerson) throws SQLException;

    public List<PoolTask> findByGroup(Context context, Group group) throws SQLException;

    public List<PoolTask> findByWorkflowItem(Context context, XmlWorkflowItem workflowItem) throws SQLException;

    public PoolTask findByWorkflowItemAndEPerson(Context context, XmlWorkflowItem workflowItem, EPerson ePerson) throws SQLException;

    public PoolTask findByWorkflowItemAndGroup(Context context, Group group, XmlWorkflowItem workflowItem) throws SQLException;
}
