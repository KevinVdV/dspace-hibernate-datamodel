package org.dspace.content.dao.impl;

import org.dspace.content.MetadataValue;
import org.dspace.content.dao.MetadataValueDAO;
import org.dspace.core.Context;
import org.dspace.core.AbstractHibernateDAO;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 21/03/14
 * Time: 10:34
 */
public class MetadataValueDAOImpl extends AbstractHibernateDAO<MetadataValue> implements MetadataValueDAO {


    @Override
    public List<MetadataValue> findByField(Context context, int fieldId) throws SQLException
    {
        Criteria criteria = createCriteria(context, MetadataValue.class);
        criteria.add(
                Restrictions.eq("metadataField.id", fieldId)
        );
        return list(criteria);
    }


}
