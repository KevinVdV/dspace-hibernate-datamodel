package org.dspace.content.dao.impl;

import org.dspace.content.MetadataSchema;
import org.dspace.content.dao.MetadataSchemaDAO;
import org.dspace.core.Context;
import org.dspace.dao.AbstractHibernateDAO;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 21/03/14
 * Time: 15:00
 */
public class MetadataSchemaDAOImpl extends AbstractHibernateDAO<MetadataSchema> implements MetadataSchemaDAO {


    /**
     * Get the schema object corresponding to this namespace URI.
     *
     * @param context DSpace context
     * @param namespace namespace URI to match
     * @return metadata schema object or null if none found.
     * @throws java.sql.SQLException
     */
    public MetadataSchema findByNamespace(Context context, String namespace) throws SQLException
    {
        // Grab rows from DB
        Criteria criteria = createCriteria(context, MetadataSchema.class);
        criteria.add(Restrictions.eq("namespace", namespace));
        return uniqueResult(criteria);
    }

    @Override
    public List<MetadataSchema> findAll(Context context, Class clazz) throws SQLException {
        // Get all the metadataschema rows
        Criteria criteria = createCriteria(context, MetadataSchema.class);
        criteria.addOrder(Order.asc("id"));
        return list(criteria);
    }

    /**
     * Return true if and only if the passed name appears within the allowed
     * number of times in the current schema.
     *
     * @param context DSpace context
     * @param namespace namespace URI to match
     * @return true of false
     * @throws SQLException
     */
    //TODO: Bussiness logic, add find & move to service layer
    public boolean uniqueNamespace(Context context, int metadataSchemaId, String namespace) throws SQLException
    {
        Criteria criteria = createCriteria(context, MetadataSchema.class);
        criteria.add(Restrictions.and(
                Restrictions.not(Restrictions.eq("id", metadataSchemaId)),
                Restrictions.eq("namespace", namespace)
        ));
        return uniqueResult(criteria) == null;
    }

    /**
     * Return true if and only if the passed name is unique.
     *
     * @param context DSpace context
     * @param name  short name of schema
     * @return true of false
     * @throws SQLException
     */
    //TODO: Bussiness logic, add find & move to service layer
    public boolean uniqueShortName(Context context, int metadataSchemaId, String name) throws SQLException
    {
        Criteria criteria = createCriteria(context, MetadataSchema.class);
        criteria.add(Restrictions.and(
                Restrictions.not(Restrictions.eq("id", metadataSchemaId)),
                Restrictions.eq("name", name)
        ));

        return uniqueResult(criteria) == null;
    }

    /**
     * Get the schema corresponding with this short name.
     *
     * @param context
     *            context, in case we need to read it in from DB
     * @param shortName
     *            the short name for the schema
     * @return the metadata schema object
     * @throws SQLException
     */
    public MetadataSchema find(Context context, String shortName) throws SQLException
    {
        Criteria criteria = createCriteria(context, MetadataSchema.class);
        criteria.add(
                Restrictions.eq("name", shortName)
        );

        return uniqueResult(criteria);
    }
}
