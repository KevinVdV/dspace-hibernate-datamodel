package org.dspace.eperson.dao.impl;

import org.dspace.core.Context;
import org.dspace.dao.AbstractHibernateDAO;
import org.dspace.eperson.Group;
import org.dspace.eperson.Group2GroupCache;
import org.dspace.eperson.dao.Group2GroupCacheDAO;
import org.hibernate.Criteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 16/05/14
 * Time: 11:55
 */
public class Group2GroupCacheDAOImpl extends AbstractHibernateDAO<Group2GroupCache> implements Group2GroupCacheDAO {

    @Override
    public List<Group2GroupCache> findByParent(Context context, Group group) throws SQLException {
        Criteria criteria = createCriteria(context, Group2GroupCache.class);
        criteria.add(Restrictions.eq("parent", group));
        return list(criteria);
    }

    @Override
    public List<Group2GroupCache> findByChildren(Context context, Set<Group> groups) throws SQLException {
        Criteria criteria = createCriteria(context, Group2GroupCache.class);

        Disjunction orDisjunction = Restrictions.or();
        for(Group group : groups)
        {
            orDisjunction.add(Restrictions.eq("child", group));
        }

        criteria.add(orDisjunction);
        return list(criteria);
    }

    @Override
    public void deleteAll(Context context) throws SQLException {
        createQuery(context, "delete from Group2GroupCache").executeUpdate();
    }
}
