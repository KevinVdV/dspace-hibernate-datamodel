package org.dspace.content.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.WorkspaceItem;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

import java.io.IOException;
import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 10/03/14
 * Time: 09:44
 */
public interface InProgressSubmissionService<T extends InProgressSubmission> {

    /**
     * Deletes submission wrapper, doesn't delete item contents
     */
    public void deleteWrapper(Context context, T inProgressSubmission) throws SQLException, AuthorizeException;

    public void delete(Context context, T inProgressSubmission) throws SQLException, AuthorizeException, IOException;

    /**
     * Update the submission, including the unarchived item.
     */
    public void update(Context context, T inProgressSubmission) throws SQLException, AuthorizeException;
}
