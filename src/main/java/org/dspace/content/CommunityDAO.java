package org.dspace.content;

import org.dspace.core.Context;
import org.dspace.dao.AbstractDSpaceObjectDao;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 14/03/14
 * Time: 15:13
 */
public abstract class CommunityDAO extends AbstractDSpaceObjectDao<Community>  {

    public abstract List<Community> findAll(Context context, String order) throws SQLException;
}
