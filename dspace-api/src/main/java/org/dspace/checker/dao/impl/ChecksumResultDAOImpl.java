package org.dspace.checker.dao.impl;

import org.dspace.checker.ChecksumResult;
import org.dspace.checker.ChecksumResultCode;
import org.dspace.checker.dao.ChecksumResultDAO;
import org.dspace.core.Context;
import org.dspace.core.AbstractHibernateDAO;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 23/04/14
 * Time: 15:33
 */
public class ChecksumResultDAOImpl extends AbstractHibernateDAO<ChecksumResult> implements ChecksumResultDAO
{

    @Override
    public ChecksumResult findByCode(Context context, ChecksumResultCode code) throws SQLException {
        Criteria criteria = createCriteria(context, ChecksumResult.class);
        criteria.add(Restrictions.eq("resultCode", code));
        return uniqueResult(criteria);
    }
}
