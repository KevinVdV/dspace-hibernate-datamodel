package org.dspace.handle.dao;

import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;
import org.dspace.handle.Handle;

import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 01/04/14
 * Time: 08:48
 * To change this template use File | Settings | File Templates.
 */
public interface HandleDAO extends GenericDAO<Handle> {

    public List<Handle> getHandlesByTypeAndId(Context context, int type, int id) throws SQLException;

    public Handle findByHandle(Context context, String handle)throws SQLException;

    public List<Handle> findByPrefix(Context context, String prefix) throws SQLException;
}
