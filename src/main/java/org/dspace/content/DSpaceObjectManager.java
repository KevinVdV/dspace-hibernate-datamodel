package org.dspace.content;

import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 20/03/14
 * Time: 10:50
 */
public interface DSpaceObjectManager<T extends DSpaceObject> {

    public T find(Context context, int id) throws SQLException;

    public DSpaceObject getAdminObject(Context context, T dso, int action) throws SQLException;

    public DSpaceObject getParentObject(Context context, T dso) throws SQLException;

    public void update(Context context, T dso) throws SQLException, AuthorizeException;
}