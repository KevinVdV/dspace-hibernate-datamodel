package org.dspace.content.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 20/03/14
 * Time: 10:50
 */
public interface DSpaceObjectService<T extends DSpaceObject> {

    public T find(Context context, int id) throws SQLException;

    /**
     * Get a proper name for the object. This may return <code>null</code>.
     * Name should be suitable for display in a user interface.
     *
     * @return Name for the object, or <code>null</code> if it doesn't have
     *         one
     */
    public abstract String getName(T dso);

    public DSpaceObject getAdminObject(Context context, T dso, int action) throws SQLException;

    public DSpaceObject getParentObject(T dso) throws SQLException;

    public String getTypeText(DSpaceObject dso);

    public void updateLastModified(Context context, T dso) throws SQLException, AuthorizeException;

    public void update(Context context, T dso) throws SQLException, AuthorizeException;
}