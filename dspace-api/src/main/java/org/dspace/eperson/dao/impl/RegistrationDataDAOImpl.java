package org.dspace.eperson.dao.impl;

import org.dspace.core.Context;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.eperson.RegistrationData;
import org.dspace.eperson.dao.RegistrationDataDAO;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 22/04/14
 * Time: 16:56
 */
public class RegistrationDataDAOImpl extends AbstractHibernateDAO<RegistrationData> implements RegistrationDataDAO {

    @Override
    public RegistrationData findByEmail(Context context, String email) throws SQLException {
        Criteria criteria = createCriteria(context, RegistrationData.class);
        criteria.add(Restrictions.eq("email", email));
        return uniqueResult(criteria);
    }

    @Override
    public RegistrationData findByToken(Context context, String token) throws SQLException {
        Criteria criteria = createCriteria(context, RegistrationData.class);
        criteria.add(Restrictions.eq("token", token));
        return uniqueResult(criteria);
    }

    @Override
    public void deleteByToken(Context context, String token) throws SQLException {
        String hql = "delete from RegistrationData where token=:token";
        Query query = createQuery(context, hql);
        query.executeUpdate();
    }
}
