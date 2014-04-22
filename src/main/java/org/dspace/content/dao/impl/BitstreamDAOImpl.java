package org.dspace.content.dao.impl;

import org.dspace.content.Bitstream;
import org.dspace.content.dao.BitstreamDAO;
import org.dspace.core.Context;
import org.dspace.dao.AbstractHibernateDAO;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 17/03/14
 * Time: 11:42
 */
public class BitstreamDAOImpl extends AbstractHibernateDAO<Bitstream> implements BitstreamDAO {

    public List<Bitstream> findDeletedBitstreams(Context context) throws SQLException {
        Criteria criteria = createCriteria(context, Bitstream.class);
        criteria.add(Restrictions.eq("deleted", true));

        return list(criteria);

    }

    public List<Bitstream> findDuplicateInternalIdentifier(Context context, Bitstream bitstream) throws SQLException {
        Criteria criteria = createCriteria(context, Bitstream.class);
        criteria.add(Restrictions.and(
                Restrictions.eq("internalId", bitstream.getInternalId()),
                Restrictions.not(Restrictions.eq("id", bitstream.getID()))
        ));

        return list(criteria);
    }


}
