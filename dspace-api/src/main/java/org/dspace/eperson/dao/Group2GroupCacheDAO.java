package org.dspace.eperson.dao;

import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;
import org.dspace.eperson.Group;
import org.dspace.eperson.Group2Group;
import org.dspace.eperson.Group2GroupCache;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 16/05/14
 * Time: 10:26
 */
public interface Group2GroupCacheDAO extends GenericDAO<Group2GroupCache> {

    public List<Group2GroupCache> findByParent(Context context, Group group) throws SQLException;

    public List<Group2GroupCache> findByChildren(Context context, Set<Group> groups) throws SQLException;

    public void deleteAll(Context context) throws SQLException;
}
