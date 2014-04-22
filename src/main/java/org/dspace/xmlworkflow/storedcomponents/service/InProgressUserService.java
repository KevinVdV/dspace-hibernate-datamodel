package org.dspace.xmlworkflow.storedcomponents.service;

import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.xmlworkflow.storedcomponents.InProgressUser;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 22/04/14
 * Time: 10:40
 */
public interface InProgressUserService {

    public InProgressUser find(Context context, int id) throws SQLException;

    public InProgressUser findByWorkflowItemAndEPerson(Context context, XmlWorkflowItem workflowItem, EPerson ePerson) throws SQLException;

    public List<InProgressUser> findByEperson(Context context, EPerson ePerson) throws SQLException;

    public List<InProgressUser> findByWorkflowItem(Context context, XmlWorkflowItem workflowItem) throws SQLException;

    public int getNumberOfInProgressUsers(Context context, XmlWorkflowItem workflowItem) throws SQLException;

    public int getNumberOfFinishedUsers(Context context, XmlWorkflowItem workflowItem) throws SQLException;

    public InProgressUser create(Context context) throws SQLException;

    public void delete(Context context, InProgressUser inProgressUser) throws SQLException;

    public void update(Context context, InProgressUser inProgressUser) throws SQLException;
}
