package org.dspace.content.dao;

import org.dspace.content.Community;
import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 14/03/14
 * Time: 15:13
 */
public interface CommunityDAO extends GenericDAO<Community> {

    public List<Community> findAll(Context context, String order) throws SQLException;
}
