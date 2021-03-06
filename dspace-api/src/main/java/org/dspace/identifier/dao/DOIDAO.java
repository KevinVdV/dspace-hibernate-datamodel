package org.dspace.identifier.dao;

import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;
import org.dspace.identifier.DOI;

import java.sql.SQLException;

/**
 * Created by kevin on 01/05/14.
 */
public interface DOIDAO extends GenericDAO<DOI>
{
    public DOI findByDoi(Context context, String doi) throws SQLException;

    public DOI findDOIByDSpaceObject(Context context, DSpaceObject dso) throws SQLException;
}
