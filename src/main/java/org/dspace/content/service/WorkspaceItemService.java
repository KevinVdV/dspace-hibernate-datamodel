package org.dspace.content.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 31/03/14
 * Time: 11:04
 */
public interface WorkspaceItemService extends InProgressSubmissionService<WorkspaceItem> {

    public WorkspaceItem find(Context context, int id) throws SQLException;

    public WorkspaceItem create(Context c, Collection coll, boolean template) throws AuthorizeException, SQLException, IOException, IllegalAccessException;

    public WorkspaceItem create(Context c, Collection coll, InProgressSubmission workflowItem) throws AuthorizeException, SQLException, IOException;

    public List<WorkspaceItem> findByEPerson(Context context, EPerson ep) throws SQLException;

    public List<WorkspaceItem> findByCollection(Context context, Collection c) throws SQLException;

    public WorkspaceItem findByItem(Context context, Item i) throws SQLException;

    public List<WorkspaceItem> findAll(Context context) throws SQLException;

    public void deleteAll(Context context, WorkspaceItem workspaceItem) throws SQLException, AuthorizeException, IOException;
}
