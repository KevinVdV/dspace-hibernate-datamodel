package org.dspace.eperson.dao;

import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;
import org.dspace.eperson.Group;
import org.dspace.eperson.Group2Group;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 16/05/14
 * Time: 10:15
 */
public interface Group2GroupDAO extends GenericDAO<Group2Group> {

    public void deleteByChild(Context context, Group child) throws SQLException;

    public void deleteByParent(Context context, Group parent) throws SQLException;

}
