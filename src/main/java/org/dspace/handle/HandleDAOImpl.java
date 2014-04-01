package org.dspace.handle;

import org.dspace.core.Context;
import org.dspace.dao.AbstractHibernateDAO;
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
    @Override
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

    @Override
    public Handle findByHandle(Context context, String handle) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(Handle.class);
        criteria.add(Restrictions.eq("handle", handle));
        @SuppressWarnings("unchecked")
        Handle result = (Handle) criteria.uniqueResult();
        return result;
    }

    @Override
    public List<Handle> findByPrefix(Context context, String prefix) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(Handle.class);
        criteria.add(Restrictions.like("handle", prefix + "%"));

        @SuppressWarnings("unchecked")
        List<Handle> result = criteria.list();
        return result;
    }
}
