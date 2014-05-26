package org.dspace.workflowbasic.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.workflow.WorkflowService;
import org.dspace.workflowbasic.BasicWorkflowItem;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 26/05/14
 * Time: 12:48
 */
public interface BasicWorkflowService extends WorkflowService<BasicWorkflowItem> {

    public int getWorkflowID(String state);

    public String getWorkflowText(int state);

    public List<BasicWorkflowItem> getOwnedTasks(Context c, EPerson e) throws java.sql.SQLException;

    public void claim(Context c, BasicWorkflowItem wi, EPerson e) throws SQLException, IOException, AuthorizeException;

    public void advance(Context c, BasicWorkflowItem wi, EPerson e)
            throws SQLException, IOException, AuthorizeException;

    public boolean advance(Context c, BasicWorkflowItem wi, EPerson e,
                                  boolean curate, boolean record)
            throws SQLException, IOException, AuthorizeException;

    public void unclaim(Context c, BasicWorkflowItem wi, EPerson e)
            throws SQLException, IOException, AuthorizeException;

    public void notifyOfCuration(Context c, BasicWorkflowItem wi, EPerson[] epa,
               String taskName, String action, String message) throws SQLException, IOException;

    public String getSubmitterName(BasicWorkflowItem wi) throws SQLException;
}
