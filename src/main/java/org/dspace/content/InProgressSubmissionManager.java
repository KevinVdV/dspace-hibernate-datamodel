package org.dspace.content;

import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

import java.io.IOException;
import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 10/03/14
 * Time: 09:44
 */
public interface InProgressSubmissionManager<T extends InProgressSubmission> {

    /**
     * Get the submitter
     *
     * @return the submitting e-person
     */
    EPerson getSubmitter(T inProgressSubmission) throws SQLException;


    /**
     * Deletes submission wrapper, doesn't delete item contents
     */
    void deleteWrapper(Context context, T inProgressSubmission) throws SQLException, IOException, AuthorizeException;

    /**
     * Update the submission, including the unarchived item.
     */
    void update(Context context, T inProgressSubmission) throws SQLException, AuthorizeException;

}
