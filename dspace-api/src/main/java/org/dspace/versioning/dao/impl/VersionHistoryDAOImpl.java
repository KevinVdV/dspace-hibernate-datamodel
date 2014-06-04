package org.dspace.versioning.dao.impl;

import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.versioning.VersionHistory;
import org.dspace.versioning.dao.VersionHistoryDAO;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 8/05/14
 * Time: 11:15
 */
public class VersionHistoryDAOImpl extends AbstractHibernateDAO<VersionHistory> implements VersionHistoryDAO {

    @Override
    public VersionHistory findByItem(Context context, Item item) throws SQLException {
        Criteria criteria = createCriteria(context, VersionHistory.class);
        criteria.createAlias("versions", "v");
        criteria.add(Restrictions.eq("v.item", item));
        return uniqueResult(criteria);
    }
}
