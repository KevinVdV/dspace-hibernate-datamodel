package org.dspace.eperson.dao;

import org.dspace.content.dao.DSpaceObjectDAO;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 31/03/14
 * Time: 11:16
 */
public interface EPersonDAO extends DSpaceObjectDAO<EPerson> {

    public EPerson findByEmail(Context context, String email) throws SQLException;

    public EPerson findByNetid(Context context, String netid) throws SQLException;

    public List<EPerson> search(Context context, String query, int offset, int limit) throws SQLException;

    public int searchResultCount(Context context, String query) throws SQLException;

    public List<EPerson> findAll(Context context, String sortField) throws SQLException;

    public List<EPerson> findByGroups(Context context, Set<Group> groups) throws SQLException;
}
