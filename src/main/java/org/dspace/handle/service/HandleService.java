package org.dspace.handle.service;

import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 18/04/14
 * Time: 15:20
 */
public interface HandleService {

    public String resolveToURL(Context context, String handle) throws SQLException;

    public String resolveUrlToHandle(Context context, String url) throws SQLException;

    public String getCanonicalForm(String handle);

    public String createHandle(Context context, DSpaceObject dso) throws SQLException;

    public String createHandle(Context context, DSpaceObject dso,
            String suppliedHandle) throws SQLException, IllegalStateException;

    public void unbindHandle(Context context, DSpaceObject dso) throws SQLException;

    public DSpaceObject resolveToObject(Context context, String handle) throws IllegalStateException, SQLException;

    public String findHandle(Context context, DSpaceObject dso) throws SQLException;

    public List<String> getHandlesForPrefix(Context context, String prefix) throws SQLException;

    public String getPrefix();

}
