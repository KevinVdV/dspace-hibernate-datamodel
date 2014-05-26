package org.dspace.workflow;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.WorkspaceItem;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

import java.io.IOException;
import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 23/05/14
 * Time: 10:22
 */
public interface WorkflowService<T extends WorkflowItem> {

    public void addInitialWorkspaceItemPolicies(Context context, WorkspaceItem workspaceItem) throws SQLException, AuthorizeException;

    public T start(Context c, WorkspaceItem wsi) throws SQLException, AuthorizeException, IOException, WorkflowException;

    public T startWithoutNotify(Context c, WorkspaceItem wsi) throws SQLException, AuthorizeException, IOException, WorkflowException;

    public Item archive(Context context, T workflowItem) throws SQLException, IOException, AuthorizeException;

    /**
     * abort() aborts a workflow, completely deleting it (administrator do this)
     * (it will basically do a reject from any state - the item ends up back in
     * the user's PersonalWorkspace
     *
     * @param c
     *            Context
     * @param wi
     *            WorkflowItem to operate on
     * @param e
     *            EPerson doing the operation
     */
    public WorkspaceItem abort(Context c, T wi, EPerson e) throws SQLException, AuthorizeException, IOException;

    public WorkspaceItem sendWorkflowItemBackSubmission(Context c, T workflowItem, EPerson e, String provenance, String rejection_message) throws SQLException, AuthorizeException, IOException;

    public String getMyDSpaceLink();

    public void deleteCollection(Context context, Collection collection) throws SQLException, IOException, AuthorizeException;
}
