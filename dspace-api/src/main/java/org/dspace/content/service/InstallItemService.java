package org.dspace.content.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;
import org.dspace.core.Context;

import java.io.IOException;
import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 29/04/14
 * Time: 11:01
 */
public interface InstallItemService {

    public Item installItem(Context c, InProgressSubmission is)throws SQLException, IOException, AuthorizeException;

    public Item installItem(Context c, InProgressSubmission is, String suppliedHandle) throws SQLException, AuthorizeException;

    public Item restoreItem(Context c, InProgressSubmission is, String suppliedHandle) throws SQLException, AuthorizeException;

    public String getBitstreamProvenanceMessage(Item myitem) throws SQLException;
}
