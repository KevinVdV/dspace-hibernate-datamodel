package org.dspace.eperson;

import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 31/03/14
 * Time: 12:03
 */
public interface GroupDAO extends GenericDAO<Group> {

    public Group findByName(Context context, String name) throws SQLException;

    public List<Group> search(Context context, String query, int offset, int limit) throws SQLException;

    public int searchResultCount(Context context, String query) throws SQLException;

    public List<Group> findAll(Context context, String sortColumn) throws SQLException;

    public List<Group> findByEPerson(Context context, EPerson ePerson) throws SQLException;
}
