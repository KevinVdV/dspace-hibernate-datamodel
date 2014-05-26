package org.dspace.workflowbasic.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.workflowbasic.BasicWorkflowItem;
import org.dspace.workflow.WorkflowItemService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 7/04/14
 * Time: 15:52
 */
public interface BasicWorkflowItemService extends WorkflowItemService<BasicWorkflowItem> {

    public List<BasicWorkflowItem> findByPooledTasks(Context context, EPerson ePerson) throws SQLException;
}
