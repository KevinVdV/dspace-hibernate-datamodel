package org.dspace.content;

import org.dspace.core.Context;
import org.dspace.dao.AbstractHibernateDAO;
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


    public List<MetadataValue> findByField(Context context, int fieldId) throws SQLException
    {
        Criteria criteria = context.getDBConnection().createCriteria(MetadataValue.class);
        criteria.add(
                Restrictions.eq("metadataField.id", fieldId)
        );
        @SuppressWarnings("unchecked")
        List<MetadataValue> result = criteria.list();
        return result;
    }


}
