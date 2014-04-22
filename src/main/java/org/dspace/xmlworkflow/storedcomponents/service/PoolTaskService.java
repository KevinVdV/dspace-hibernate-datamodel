package org.dspace.xmlworkflow.storedcomponents.service;

import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.service.DSpaceCRUDService;
import org.dspace.xmlworkflow.storedcomponents.PoolTask;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 22/04/14
 * Time: 11:18
 */
public interface PoolTaskService extends DSpaceCRUDService<PoolTask>{

    public List<PoolTask> findByEPerson(Context context, EPerson ePerson) throws SQLException;

    public List<PoolTask> findByGroups(Context context, EPerson ePerson, List<Group> groups) throws SQLException;

    public List<PoolTask> findByWorkflowItem(Context context, XmlWorkflowItem workflowItem) throws SQLException;

    public PoolTask findByWorkflowItemAndEPerson(Context context, XmlWorkflowItem workflowItem, EPerson ePerson) throws SQLException;

    public void deleteByWorkflowItem(Context context, XmlWorkflowItem workflowItem) throws SQLException;
}
