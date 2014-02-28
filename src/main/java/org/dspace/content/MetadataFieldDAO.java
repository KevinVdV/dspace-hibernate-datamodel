/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
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

/**
 * DSpace object that represents a metadata field, which is
 * defined by a combination of schema, element, and qualifier.  Every
 * metadata element belongs in a field.
 *
 * @author Martin Hald
 * @version $Revision$
 * @see org.dspace.content.MetadataValue
 * @see org.dspace.content.MetadataSchema
 */
public class MetadataFieldDAO
{

    /** log4j logger */
    private static Logger log = Logger.getLogger(MetadataFieldDAO.class);

    // cache of field by ID (Integer)
    private static Map<Integer, MetadataField> id2field = null;


    /**
     * Default constructor.
     */
    public MetadataFieldDAO()
    {
    }

    /**
     * Creates a new metadata field.
     *
     * @param context
     *            DSpace context object
     * @throws IOException
     * @throws AuthorizeException
     * @throws SQLException
     * @throws NonUniqueMetadataException
     */
    public void create(Context context, MetadataSchema metadataSchema, String element, String qualifier, String scopeNote) throws IOException, AuthorizeException,
            SQLException, NonUniqueMetadataException
    {
        // Check authorisation: Only admins may create DC types
        if (!AuthorizeManager.isAdmin(context))
        {
            throw new AuthorizeException(
                    "Only administrators may modify the metadata registry");
        }

        // Ensure the element and qualifier are unique within a given schema.
        if (!unique(context, -1, metadataSchema.getSchemaID(), element, qualifier))
        {
            throw new NonUniqueMetadataException("Please make " + element + "."
                    + qualifier + " unique within schema #" + metadataSchema.getSchemaID());
        }

        // Create a table row and update it with the values
        MetadataField metadataField = new MetadataField();
        metadataField.setElement(element);
        metadataField.setQualifier(qualifier);
        metadataField.setScopeNote(scopeNote);
        metadataField.setMetadataSchema(metadataSchema);
        HibernateQueryUtil.update(context, metadataField);
        decache();

        log.info(LogManager.getHeader(context, "create_metadata_field",
                "metadata_field_id=" + metadataField.getFieldID()));
    }

    /**
     * Retrieves the metadata field from the database.
     *
     * @param context dspace context
     * @param schemaID schema by ID
     * @param element element name
     * @param qualifier qualifier (may be ANY or null)
     * @return recalled metadata field
     * @throws SQLException
     * @throws AuthorizeException
     */
    public MetadataField findByElement(Context context, int schemaID,
            String element, String qualifier) throws SQLException,
            AuthorizeException
    {
        Criteria criteria = context.getDBConnection().createCriteria(MetadataField.class);
        criteria.add(
                Restrictions.and(
                        Restrictions.eq("metadata_schema_id", schemaID),
                        Restrictions.eq("element", element),
                        Restrictions.eq("qualifier", qualifier)
                )
        );
        return (MetadataField) criteria.uniqueResult();
    }

    /**
     * Retrieve all Dublin Core types from the registry
     *
     * @param context dspace context
     * @return an array of all the Dublin Core types
     * @throws SQLException
     */
    public MetadataField[] findAll(Context context) throws SQLException
    {
        List<MetadataField> fields = new ArrayList<MetadataField>();
        Criteria criteria = context.getDBConnection().createCriteria(MetadataField.class);
        List<MetadataField> list = criteria.list();
        return list.toArray(new MetadataField[list.size()]);
    }

    /**
     * Return all metadata fields that are found in a given schema.
     *
     * @param context dspace context
     * @param schemaID schema by db ID
     * @return array of metadata fields
     * @throws SQLException
     */
    public MetadataField[] findAllInSchema(Context context, int schemaID)
            throws SQLException
    {
        // Get all the metadatafieldregistry rows
        Criteria criteria = context.getDBConnection().createCriteria(MetadataField.class);
        criteria.add(Restrictions.eq("metadata_schema_id", schemaID));
        criteria.addOrder(Order.asc("element")).addOrder(Order.asc("qualifier"));
        List<MetadataField> fields = criteria.list();

        // Convert list into an array
        return fields.toArray(new MetadataField[fields.size()]);
    }

    /**
     * Update the metadata field in the database.
     *
     * @param context dspace context
     * @throws SQLException
     * @throws AuthorizeException
     * @throws NonUniqueMetadataException
     * @throws IOException
     */
    public void update(Context context, MetadataField metadataField) throws SQLException,
            AuthorizeException, NonUniqueMetadataException, IOException
    {
        // Check authorisation: Only admins may update the metadata registry
        if (!AuthorizeManager.isAdmin(context))
        {
            throw new AuthorizeException(
                    "Only administrators may modiffy the Dublin Core registry");
        }

        // Check to see if the schema ID was altered. If is was then we will
        // query to ensure that there is not already a duplicate name field.
        if (hasElement(context, metadataField.getMetadataSchema().getSchemaID(), metadataField.getElement(), metadataField.getQualifier()))
        {
            throw new NonUniqueMetadataException(
                    "Duplcate field name found in target schema");
        }

        // Ensure the element and qualifier are unique within a given schema.
        if (!unique(context, metadataField.getFieldID(), metadataField.getMetadataSchema().getSchemaID(), metadataField.getElement(), metadataField.getQualifier()))
        {
            throw new NonUniqueMetadataException("Please make " + metadataField.getElement() + "."
                    + metadataField.getQualifier());
        }

        HibernateQueryUtil.update(context, metadataField);
        decache();

        log.info(LogManager.getHeader(context, "update_metadatafieldregistry",
                "metadata_field_id=" + metadataField.getFieldID() + "element=" + metadataField.getElement()
                        + "qualifier=" + metadataField.getQualifier()));
    }

    /**
     * Return true if and only if the schema has a field with the given element
     * and qualifier pair.
     *
     * @param context dspace context
     * @param schemaID schema by ID
     * @param element element name
     * @param qualifier qualifier name
     * @return true if the field exists
     * @throws SQLException
     * @throws AuthorizeException
     */
    private boolean hasElement(Context context, int schemaID,
            String element, String qualifier) throws SQLException,
            AuthorizeException
    {
        return findByElement(context, schemaID, element,
                qualifier) != null;
    }

    /**
     * Delete the metadata field.
     *
     * @param context dspace context
     * @throws SQLException
     * @throws AuthorizeException
     */
    public void delete(Context context, MetadataField metadataField) throws SQLException, AuthorizeException
    {
        // Check authorisation: Only admins may create DC types
        if (!AuthorizeManager.isAdmin(context))
        {
            throw new AuthorizeException(
                    "Only administrators may modify the metadata registry");
        }

        log.info(LogManager.getHeader(context, "delete_metadata_field",
                "metadata_field_id=" + metadataField.getFieldID()));

        HibernateQueryUtil.delete(context, metadataField);
        decache();
    }

    /**
     * A sanity check that ensures a given element and qualifier are unique
     * within a given schema. The check happens in code as we cannot use a
     * database constraint.
     *
     * @param context dspace context
     * @param schemaID
     * @param element
     * @param qualifier
     * @return true if unique
     * @throws AuthorizeException
     * @throws SQLException
     * @throws IOException
     */
    private boolean unique(Context context, int metadataFieldId, int schemaID, String element,
            String qualifier) throws IOException, SQLException,
            AuthorizeException
    {
        int count = 0;
        String qualifierClause = "";

        if (qualifier == null)
        {
            qualifierClause = "and qualifier is null";
        }
        else
        {
            qualifierClause = "and qualifier = ?";
        }
        Criteria criteria = context.getDBConnection().createCriteria(MetadataField.class);
        criteria.add(
                Restrictions.and(
                        Restrictions.not(Restrictions.eq("metadata_field_id", metadataFieldId)),
                        Restrictions.eq("metadata_schema_id", schemaID),
                        Restrictions.eq("element", element),
                        Restrictions.eq("qualifier", qualifier)
                )
        );

        return criteria.uniqueResult() == null;
    }

    /**
     * Return the HTML FORM key for the given field.
     *
     * @param schema
     * @param element
     * @param qualifier
     * @return HTML FORM key
     */
    public static String formKey(String schema, String element, String qualifier)
    {
        if (qualifier == null)
        {
            return schema + "_" + element;
        }
        else
        {
            return schema + "_" + element + "_" + qualifier;
        }
    }

    /**
     * Find the field corresponding to the given numeric ID.  The ID is
     * a database key internal to DSpace.
     *
     * @param context
     *            context, in case we need to read it in from DB
     * @param id
     *            the metadata field ID
     * @return the metadata field object
     * @throws SQLException
     */
    public MetadataField find(Context context, int id)
            throws SQLException
    {
        if (!isCacheInitialized())
        {
            initCache(context);
        }

        // 'sanity check' first.
        Integer iid = Integer.valueOf(id);
        if (!id2field.containsKey(iid))
        {
            return null;
        }

        return id2field.get(iid);
    }

    // invalidate the cache e.g. after something modifies DB state.
    private static void decache()
    {
        id2field = null;
    }

    private static boolean isCacheInitialized()
    {
        return id2field != null;
    }
    
    // load caches if necessary
    private static synchronized void initCache(Context context) throws SQLException
    {
        if (!isCacheInitialized())
        {
            Map<Integer, MetadataField> new_id2field = new HashMap<Integer, MetadataField>();
            log.info("Loading MetadataField elements into cache.");

            // Grab rows from DB
            Criteria criteria = context.getDBConnection().createCriteria(MetadataField.class);
            List<MetadataField> metadataFields = criteria.list();
            for (MetadataField metadataField : metadataFields) {
                new_id2field.put(metadataField.getFieldID(), metadataField);
            }

            id2field = new_id2field;
        }
    }

}