package org.dspace.xmlworkflow.storedcomponents.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.service.InProgressSubmissionService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.workflow.WorkflowItemService;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 21/04/14
 * Time: 09:50
 * To change this template use File | Settings | File Templates.
 */
public interface XmlWorkflowItemService extends WorkflowItemService<XmlWorkflowItem> {


    public List<XmlWorkflowItem> findAll(Context context, int page, int pagesize) throws SQLException;

    public List<XmlWorkflowItem> findAllInCollection(Context context, int page, int pagesize, Collection collection) throws SQLException;

    public int countAll(Context context) throws SQLException;

    public int countAllInCollection(Context context, Collection collection) throws SQLException;
}
