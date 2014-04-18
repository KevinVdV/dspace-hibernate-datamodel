package org.dspace.handle.dao.impl;

import org.dspace.core.Context;
import org.dspace.dao.AbstractHibernateDAO;
import org.dspace.handle.Handle;
import org.dspace.handle.dao.HandleDAO;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 01/04/14
 * Time: 08:48
 * To change this template use File | Settings | File Templates.
 */
public class HandleDAOImpl extends AbstractHibernateDAO<Handle> implements HandleDAO {

    public List<Handle> getHandlesByTypeAndId(Context context, int type, int id) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(Handle.class);
        criteria.add(Restrictions.and(
                Restrictions.eq("resource_type_id", type),
                Restrictions.eq("resource_id", id)
        ));

        @SuppressWarnings("unchecked")
        List<Handle> result = criteria.list();
        return result;
    }

    public Handle findByHandle(Context context, String handle) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(Handle.class);
        criteria.add(Restrictions.eq("handle", handle));
        @SuppressWarnings("unchecked")
        Handle result = (Handle) criteria.uniqueResult();
        return result;
    }

    public List<Handle> findByPrefix(Context context, String prefix) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(Handle.class);
        criteria.add(Restrictions.like("handle", prefix + "%"));

        @SuppressWarnings("unchecked")
        List<Handle> result = criteria.list();
        return result;
    }
}
