package org.dspace.xmlworkflow.storedcomponents.service;

import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.xmlworkflow.storedcomponents.PoolTask;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 22/04/14
 * Time: 11:18
 */
public interface PoolTaskService {

    public PoolTask find(Context context, int id) throws SQLException;

    public List<PoolTask> findByEPerson(Context context, EPerson ePerson) throws SQLException;

    public List<PoolTask> findByGroups(Context context, EPerson ePerson, List<Group> groups) throws SQLException;

    public List<PoolTask> findByWorkflowItem(Context context, XmlWorkflowItem workflowItem) throws SQLException;

    public PoolTask findByWorkflowItemAndEPerson(Context context, XmlWorkflowItem workflowItem, EPerson ePerson) throws SQLException;

    public PoolTask create(Context context) throws SQLException;

    public void delete(Context context, PoolTask poolTask) throws SQLException;

    public void update(Context context, PoolTask poolTask) throws SQLException;

    public void deleteByWorkflowItem(Context context, XmlWorkflowItem workflowItem) throws SQLException;
}
