package org.dspace.eperson.dao;

import org.dspace.content.dao.DSpaceObjectDAO;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 31/03/14
 * Time: 12:03
 */
public interface GroupDAO extends DSpaceObjectDAO<Group> {

    public Group findByName(Context context, String name) throws SQLException;

    public List<Group> search(Context context, String query, int offset, int limit) throws SQLException;

    public int searchResultCount(Context context, String query) throws SQLException;

    public List<Group> findAll(Context context, String sortColumn) throws SQLException;

    public List<Group> findByEPerson(Context context, EPerson ePerson) throws SQLException;
}
