package org.dspace.content;

import org.dspace.core.Context;
import org.dspace.dao.AbstractHibernateDAO;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 16/03/14
 * Time: 09:43
 * To change this template use File | Settings | File Templates.
 */
public class MetadataFieldDAOImpl extends AbstractHibernateDAO<MetadataField> implements MetadataFieldDAO{

    public MetadataField find(Context context, int metadataFieldId, MetadataSchema metadataSchema, String element,
                           String qualifier) throws SQLException{
        Criteria criteria = context.getDBConnection().createCriteria(MetadataField.class);
        criteria.add(
                Restrictions.and(
                        Restrictions.not(Restrictions.eq("id", metadataFieldId)),
                        Restrictions.eq("metadataSchema", metadataSchema),
                        Restrictions.eq("element", element),
                        Restrictions.eqOrIsNull("qualifier", qualifier)
                )
        );
        return (MetadataField) criteria.uniqueResult();
    }

    public MetadataField findByElement(Context context, MetadataSchema metadataSchema, String element, String qualifier) throws SQLException
    {
        Criteria criteria = context.getDBConnection().createCriteria(MetadataField.class);
        criteria.add(
                Restrictions.and(
                        Restrictions.eq("metadataSchema", metadataSchema),
                        Restrictions.eq("element", element),
                        Restrictions.eqOrIsNull("qualifier", qualifier)
                )
        );
        return (MetadataField) criteria.uniqueResult();
    }

    public List<MetadataField> findAllInSchema(Context context, String schema) throws SQLException {
        // Get all the metadatafieldregistry rows
        Criteria criteria = context.getDBConnection().createCriteria(MetadataField.class);
        criteria.add(Restrictions.eq("metadataSchema.name", schema));
        criteria.addOrder(Order.asc("element")).addOrder(Order.asc("qualifier"));
        @SuppressWarnings("unchecked")
        List<MetadataField> fields = criteria.list();
        return fields;
    }

}
