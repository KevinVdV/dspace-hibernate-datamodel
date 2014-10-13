package org.dspace.content.dao.impl;

import org.dspace.content.Site;
import org.dspace.content.dao.SiteDAO;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;
import org.hibernate.Criteria;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 18/07/14
 * Time: 16:08
 */
public class SiteDAOImpl extends AbstractHibernateDAO<Site> implements SiteDAO {

    @Override
    public Site findSite(Context context) throws SQLException {
        Criteria criteria = createCriteria(context, Site.class);
        return uniqueResult(criteria);
    }
}
