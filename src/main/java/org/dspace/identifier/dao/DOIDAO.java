package org.dspace.identifier.dao;

import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;
import org.dspace.identifier.DOI;

import java.sql.SQLException;

/**
 * Created by kevin on 01/05/14.
 */
public interface DoiDAO extends GenericDAO<DOI>
{
    public DOI findByDoi(Context context, String doi) throws SQLException;
}
