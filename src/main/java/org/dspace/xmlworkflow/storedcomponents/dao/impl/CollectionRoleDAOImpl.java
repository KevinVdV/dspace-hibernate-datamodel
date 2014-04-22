package org.dspace.xmlworkflow.storedcomponents.dao.impl;

import org.dspace.content.Collection;
import org.dspace.core.Context;
import org.dspace.dao.AbstractHibernateDAO;
import org.dspace.xmlworkflow.storedcomponents.CollectionRole;
import org.dspace.xmlworkflow.storedcomponents.dao.CollectionRoleDAO;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 21/04/14
 * Time: 15:05
 * To change this template use File | Settings | File Templates.
 */
public class CollectionRoleDAOImpl extends AbstractHibernateDAO<CollectionRole> implements CollectionRoleDAO {

    public List<CollectionRole> findByCollection(Context context, Collection collection) throws SQLException {
        Criteria criteria = createCriteria(context, CollectionRole.class);
        criteria.add(Restrictions.eq("collection", collection));

        return list(criteria);
    }

    public CollectionRole findByCollectionAndRole(Context context, Collection collection, String role) throws SQLException {
        Criteria criteria = createCriteria(context, CollectionRole.class);
        criteria.add(Restrictions.and(
                Restrictions.eq("collection", collection),
                Restrictions.eq("role", role)
            )
        );

        return uniqueResult(criteria);

    }
}
