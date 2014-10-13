package org.dspace.identifier.dao.impl;

import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.identifier.DOI;
import org.dspace.identifier.dao.DOIDAO;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;

/**
 * Created by kevin on 01/05/14.
 */
public class DOIDAOImpl extends AbstractHibernateDAO<DOI> implements DOIDAO {
    @Override
    public DOI findByDoi(Context context, String doi) throws SQLException {
        Criteria criteria = createCriteria(context, DOI.class);
        criteria.add(Restrictions.eq("doi", doi));
        return uniqueResult(criteria);
    }

    @Override
    public DOI findDOIByDSpaceObject(Context context, DSpaceObject dso) throws SQLException {
        //SELECT * FROM Doi WHERE resource_type_id = ? AND resource_id = ? AND ((status != DOI.TO_BE_DELETED AND status != DOI.DELETED) OR status IS NULL)
        Criteria criteria = createCriteria(context, DOI.class);
        criteria.add(
                Restrictions.and(
                        Restrictions.eq("dSpaceObject", dso),
                        Restrictions.or(
                                Restrictions.and(
                                        Restrictions.not(Restrictions.eq("status", DOI.DELETED)),
                                        Restrictions.not(Restrictions.eq("status", DOI.TO_BE_DELETED))
                                ),
                                Restrictions.isNull("status")
                        )

                )
        );
        return uniqueResult(criteria);
    }
}
