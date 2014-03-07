/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.hibernate.HibernateQueryUtil;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Class representing a schema in DSpace.
 * <p>
 * The schema object exposes a name which can later be used to generate
 * namespace prefixes in RDF or XML, e.g. the core DSpace Dublin Core schema
 * would have a name of <code>'dc'</code>.
 * </p>
 *
 * @author Martin Hald
 * @version $Revision$
 * @see org.dspace.content.MetadataValue
 * @see org.dspace.content.MetadataField
 */
public class MetadataSchemaDAO
{
    /** log4j logger */
    private static Logger log = Logger.getLogger(MetadataSchema.class);

    // cache of schema by ID (Integer)
    private static Map<Integer, MetadataSchema> id2schema = null;

    // cache of schema by short name
    private static Map<String, MetadataSchema> name2schema = null;


    /**
     * Default constructor.
     */
    public MetadataSchemaDAO()
    {
    }


    /**
     * Creates a new metadata schema in the database, out of this object.
     *
     * @param context
     *            DSpace context object
     * @throws SQLException
     * @throws AuthorizeException
     * @throws NonUniqueMetadataException
     */
    public void create(Context context, String name, String namespace) throws SQLException,
            AuthorizeException, NonUniqueMetadataException
    {
        // Check authorisation: Only admins may create metadata schemas
        if (!AuthorizeManager.isAdmin(context))
        {
            throw new AuthorizeException(
                    "Only administrators may modify the metadata registry");
        }

        // Ensure the schema name is unique
        if (!uniqueShortName(context,-1, name))
        {
            throw new NonUniqueMetadataException("Please make the name " + name
                    + " unique");
        }
        
        // Ensure the schema namespace is unique
        if (!uniqueNamespace(context,-1, namespace))
        {
            throw new NonUniqueMetadataException("Please make the namespace " + namespace
                    + " unique");
        }


        // Create a table row and update it with the values
        MetadataSchema metadataSchema = new MetadataSchema();
        metadataSchema.setNamespace(namespace);
        metadataSchema.setName(name);
        HibernateQueryUtil.update(context, metadataSchema);

        // invalidate our fast-find cache.
        decache();
        log.info(LogManager.getHeader(context, "create_metadata_schema",
                "metadata_schema_id="
                        + metadataSchema.getSchemaID()));
    }

    /**
     * Get the schema object corresponding to this namespace URI.
     *
     * @param context DSpace context
     * @param namespace namespace URI to match
     * @return metadata schema object or null if none found.
     * @throws SQLException
     */
    public MetadataSchema findByNamespace(Context context,
            String namespace) throws SQLException
    {
        // Grab rows from DB
        Criteria criteria = context.getDBConnection().createCriteria(MetadataSchema.class);
        criteria.add(Restrictions.eq("namespace", namespace));
        return (MetadataSchema) criteria.uniqueResult();
    }

    /**
     * Update the metadata schema in the database.
     *
     * @param context DSpace context
     * @throws SQLException
     * @throws AuthorizeException
     * @throws NonUniqueMetadataException
     */
    public void update(Context context, MetadataSchema metadataSchema, String namespace, String name) throws SQLException,
            AuthorizeException, NonUniqueMetadataException
    {
        // Check authorisation: Only admins may update the metadata registry
        if (!AuthorizeManager.isAdmin(context))
        {
            throw new AuthorizeException(
                    "Only administrators may modify the metadata registry");
        }

        // Ensure the schema name is unique
        if (!uniqueShortName(context, metadataSchema.getSchemaID(), name))
        {
            throw new NonUniqueMetadataException("Please make the name " + name
                    + " unique");
        }

        // Ensure the schema namespace is unique
        if (!uniqueNamespace(context, metadataSchema.getSchemaID(), namespace))
        {
            throw new NonUniqueMetadataException("Please make the namespace " + namespace
                    + " unique");
        }
        HibernateQueryUtil.update(context, metadataSchema);

        decache();

        log.info(LogManager.getHeader(context, "update_metadata_schema",
                "metadata_schema_id=" + metadataSchema.getSchemaID() + "namespace="
                        + metadataSchema.getNamespace() + "name=" + metadataSchema.getName()));
    }

    /**
     * Delete the metadata schema.
     *
     * @param context DSpace context
     * @throws SQLException
     * @throws AuthorizeException
     */
    public void delete(Context context, MetadataSchema metadataSchema) throws SQLException, AuthorizeException
    {
        // Check authorisation: Only admins may create DC types
        if (!AuthorizeManager.isAdmin(context))
        {
            throw new AuthorizeException(
                    "Only administrators may modify the metadata registry");
        }

        log.info(LogManager.getHeader(context, "delete_metadata_schema",
                "metadata_schema_id=" + metadataSchema.getSchemaID()));

        HibernateQueryUtil.delete(context, metadataSchema);
        decache();
    }

    /**
     * Return all metadata schemas.
     *
     * @param context DSpace context
     * @return array of metadata schemas
     * @throws SQLException
     */
    public MetadataSchema[] findAll(Context context) throws SQLException
    {
        List<MetadataSchema> schemas = new ArrayList<MetadataSchema>();

        // Get all the metadataschema rows
        Criteria criteria = context.getDBConnection().createCriteria(MetadataSchema.class);
        criteria.addOrder(Order.asc("metadata_schema_id"));
        schemas = criteria.list();
        return schemas.toArray(new MetadataSchema[schemas.size()]);
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
    private boolean uniqueNamespace(Context context, int metadataSchemaId, String namespace)
            throws SQLException
    {
        Criteria criteria = context.getDBConnection().createCriteria(MetadataSchema.class);
        criteria.add(Restrictions.and(
                Restrictions.not(Restrictions.eq("metadata_schema_id", metadataSchemaId)),
                Restrictions.not(Restrictions.eq("namespace", namespace))
        ));

        return criteria.uniqueResult() == null;
    }

    /**
     * Return true if and only if the passed name is unique.
     *
     * @param context DSpace context
     * @param name  short name of schema
     * @return true of false
     * @throws SQLException
     */
    private boolean uniqueShortName(Context context, int metadataSchemaId, String name)
            throws SQLException
    {
        Criteria criteria = context.getDBConnection().createCriteria(MetadataSchema.class);
        criteria.add(Restrictions.and(
                Restrictions.not(Restrictions.eq("metadata_schema_id", metadataSchemaId)),
                Restrictions.not(Restrictions.eq("short_id", name))
        ));

        return criteria.uniqueResult() == null;
    }

    /**
     * Get the schema corresponding with this numeric ID.
     * The ID is a database key internal to DSpace.
     *
     * @param context
     *            context, in case we need to read it in from DB
     * @param id
     *            the schema ID
     * @return the metadata schema object
     * @throws SQLException
     */
    public MetadataSchema find(Context context, int id)
            throws SQLException
    {
        if (!isCacheInitialized())
        {
            initCache(context);
        }
        
        Integer iid = Integer.valueOf(id);

        // sanity check
        if (!id2schema.containsKey(iid))
        {
            return null;
        }

        return id2schema.get(iid);
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
    public MetadataSchema find(Context context, String shortName)
        throws SQLException
    {
        // If we are not passed a valid schema name then return
        if (shortName == null)
        {
            return null;
        }

        if (!isCacheInitialized())
        {
            initCache(context);
        }

        if (!name2schema.containsKey(shortName))
        {
            return null;
        }

        return name2schema.get(shortName);
    }

    // invalidate the cache e.g. after something modifies DB state.
    private static void decache()
    {
        id2schema = null;
        name2schema = null;
    }

    private static boolean isCacheInitialized()
    {
        return (id2schema != null && name2schema != null);
    }

    // load caches if necessary
    private static synchronized void initCache(Context context) throws SQLException
    {
        if (!isCacheInitialized())
        {
            log.info("Loading schema cache for fast finds");
            Map<Integer, MetadataSchema> new_id2schema = new HashMap<Integer, MetadataSchema>();
            Map<String, MetadataSchema> new_name2schema = new HashMap<String, MetadataSchema>();

            List<MetadataSchema> schemas = context.getDBConnection().createCriteria(MetadataSchema.class).list();

            for (int i = 0; i < schemas.size(); i++) {
                MetadataSchema metadataSchema = schemas.get(i);
                new_id2schema.put(metadataSchema.getSchemaID(), metadataSchema);
                new_name2schema.put(metadataSchema.getName(), metadataSchema);
            }
            id2schema = new_id2schema;
            name2schema = new_name2schema;
        }
    }
}