package org.dspace.eperson;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObjectManager;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 31/03/14
 * Time: 11:38
 */
public interface EPersonManager extends DSpaceObjectManager<EPerson> {

    public EPerson find(Context context, int id) throws SQLException;

    public EPerson findByEmail(Context context, String email) throws SQLException;

    public EPerson findByNetid(Context context, String netid) throws SQLException;

    public List<EPerson> search(Context context, String query) throws SQLException;

    public List<EPerson> search(Context context, String query, int offset, int limit) throws SQLException;

    public int searchResultCount(Context context, String query) throws SQLException;

    public List<EPerson> findAll(Context context, int sortField) throws SQLException;

    public EPerson create(Context context) throws SQLException, AuthorizeException;

    public void delete(Context context, EPerson ePersonEntity) throws SQLException, AuthorizeException, EPersonDeletionException;

    public void setPassword(EPerson epersonEntity, String s);

    public void setPasswordHash(EPerson epersonEntity, PasswordHash password);

    public PasswordHash getPasswordHash(EPerson ePersonEntity);

    public boolean checkPassword(Context context, EPerson ePersonEntity, String attempt);
}
