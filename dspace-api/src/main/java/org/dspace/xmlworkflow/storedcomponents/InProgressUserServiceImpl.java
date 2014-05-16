package org.dspace.xmlworkflow.storedcomponents;

import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.xmlworkflow.storedcomponents.dao.InProgressUserDAO;
import org.dspace.xmlworkflow.storedcomponents.service.InProgressUserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 22/04/14
 * Time: 10:28
 */
public class InProgressUserServiceImpl implements InProgressUserService {

    @Autowired(required = true)
    protected InProgressUserDAO inProgressUserDAO;

    public InProgressUserServiceImpl() {
    }

    @Override
    public InProgressUser find(Context context, int id) throws SQLException
    {
        return inProgressUserDAO.findByID(context, InProgressUser.class, id);
    }

    @Override
    public InProgressUser findByWorkflowItemAndEPerson(Context context, XmlWorkflowItem workflowItem, EPerson ePerson) throws SQLException {
        return inProgressUserDAO.findByWorkflowItemAndEPerson(context, workflowItem, ePerson);

    }

    @Override
    public List<InProgressUser> findByEperson(Context context, EPerson ePerson) throws SQLException {
        return inProgressUserDAO.findByEperson(context, ePerson);
    }

    @Override
    public List<InProgressUser> findByWorkflowItem(Context context, XmlWorkflowItem workflowItem) throws SQLException {
        return inProgressUserDAO.findByWorkflowItem(context, workflowItem);

    }

    @Override
    public int getNumberOfInProgressUsers(Context context, XmlWorkflowItem workflowItem) throws SQLException {
        return inProgressUserDAO.countInProgressUsers(context, workflowItem);

    }

    @Override
    public int getNumberOfFinishedUsers(Context context, XmlWorkflowItem workflowItem) throws SQLException {
        return inProgressUserDAO.countFinishedUsers(context, workflowItem);
    }

    @Override
    public InProgressUser create(Context context) throws SQLException {
        return inProgressUserDAO.create(context, new InProgressUser());
    }


    @Override
    public void delete(Context context, InProgressUser inProgressUser) throws SQLException {
        inProgressUserDAO.delete(context, inProgressUser);
    }


    @Override
    public void update(Context context, InProgressUser inProgressUser) throws SQLException {
        inProgressUserDAO.save(context, inProgressUser);
    }
}
