package org.dspace.xmlworkflow.storedcomponents.dao;

import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;
import org.dspace.eperson.EPerson;
import org.dspace.xmlworkflow.storedcomponents.InProgressUser;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 22/04/14
 * Time: 10:33
 */
public interface InProgressUserDAO extends GenericDAO<InProgressUser> {

    public InProgressUser findByWorkflowItemAndEPerson(Context context, XmlWorkflowItem workflowItem, EPerson ePerson) throws SQLException;

    public List<InProgressUser> findByEperson(Context context, EPerson ePerson) throws SQLException;

    public List<InProgressUser> findByWorkflowItem(Context context, XmlWorkflowItem workflowItem) throws SQLException;

    public int countInProgressUsers(Context context, XmlWorkflowItem workflowItem) throws SQLException;

    public int countFinishedUsers(Context context, XmlWorkflowItem workflowItem) throws SQLException;
}
