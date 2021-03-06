package org.dspace.handle.dao.impl;

import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.core.AbstractHibernateDAO;
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

    @Override
    public List<Handle> getHandlesByTypeAndDSpaceObject(Context context, int type, DSpaceObject dso) throws SQLException {
        Criteria criteria = createCriteria(context, Handle.class);
        criteria.add(Restrictions.and(
                Restrictions.eq("resourceTypeId", type),
                Restrictions.eq("dspaceObject", dso)
        ));
        return list(criteria);
    }

    @Override
    public Handle findByHandle(Context context, String handle) throws SQLException {
        Criteria criteria = createCriteria(context, Handle.class);
        criteria.add(Restrictions.eq("handle", handle));
        return uniqueResult(criteria);
    }

    @Override
    public List<Handle> findByPrefix(Context context, String prefix) throws SQLException {
        Criteria criteria = createCriteria(context, Handle.class);
        criteria.add(Restrictions.like("handle", prefix + "%"));
        return list(criteria);
    }
}
