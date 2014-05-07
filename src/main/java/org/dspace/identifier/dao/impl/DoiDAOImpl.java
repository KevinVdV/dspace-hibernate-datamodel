package org.dspace.identifier.dao.impl;

import org.dspace.core.Context;
import org.dspace.dao.AbstractHibernateDAO;
import org.dspace.identifier.DOI;
import org.dspace.identifier.dao.DoiDAO;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;

/**
 * Created by kevin on 01/05/14.
 */
public class DoiDAOImpl extends AbstractHibernateDAO<DOI> implements DoiDAO {
    @Override
    public DOI findByDoi(Context context, String doi) throws SQLException {
        Criteria criteria = createCriteria(context, DOI.class);
        criteria.add(Restrictions.eq("doi", doi));
        return uniqueResult(criteria);
    }
}
