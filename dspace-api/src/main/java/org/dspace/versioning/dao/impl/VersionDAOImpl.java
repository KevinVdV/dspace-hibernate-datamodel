package org.dspace.versioning.dao.impl;

import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.dao.AbstractHibernateDAO;
import org.dspace.versioning.Version;
import org.dspace.versioning.dao.VersionDAO;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 8/05/14
 * Time: 10:53
 */
public class VersionDAOImpl extends AbstractHibernateDAO<Version> implements VersionDAO {

    @Override
    public Version findByItem(Context context, Item item) throws SQLException {
        Criteria criteria = createCriteria(context, Version.class);
        criteria.add(Restrictions.eq("item", item));
        return uniqueResult(criteria);
    }
}
