/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.core.Context;
import org.dspace.core.LogManager;

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
public class MetadataFieldReoImpl
{

    /** log4j logger */
    private static Logger log = Logger.getLogger(MetadataFieldReoImpl.class);

    // cache of field by ID (Integer)
    private static Map<Integer, MetadataField> id2field = null;

    private MetadataFieldDAO metadataFieldDAO = new MetadataFieldDAOImpl();

    /**
     * Default constructor.
     */
    public MetadataFieldReoImpl()
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
    //TODO: HIBERNATE: REWRITE THIS METHOD TO NOT CONTAIN ELEMENT, QUALIFIER, .....
    public MetadataField create(Context context, MetadataSchema metadataSchema, String element, String qualifier, String scopeNote) throws IOException, AuthorizeException,
            SQLException, NonUniqueMetadataException
    {
        // Check authorisation: Only admins may create DC types
        if (!AuthorizeManager.isAdmin(context))
        {
            throw new AuthorizeException(
                    "Only administrators may modify the metadata registry");
        }

        // Ensure the element and qualifier are unique within a given schema.
        if (hasElement(context, -1, metadataSchema, element, qualifier))
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
        metadataFieldDAO.save(context, metadataField);
        decache();

        log.info(LogManager.getHeader(context, "create_metadata_field",
                "metadata_field_id=" + metadataField.getFieldID()));
        return metadataField;
    }

    /**
     * Retrieves the metadata field from the database.
     *
     * @param context dspace context
     * @param metadataSchema metadata schema
     * @param element element name
     * @param qualifier qualifier (may be ANY or null)
     * @return recalled metadata field
     * @throws SQLException
     */
    public MetadataField findByElement(Context context, MetadataSchema metadataSchema,
            String element, String qualifier) throws SQLException
    {
        return metadataFieldDAO.findByElement(context, metadataSchema, element, qualifier);
    }

    /**
     * Retrieve all Dublin Core types from the registry
     *
     * @param context dspace context
     * @return an array of all the Dublin Core types
     * @throws SQLException
     */
    public List<MetadataField> findAll(Context context) throws SQLException
    {
        return metadataFieldDAO.findAll(context, MetadataField.class);
    }

    /**
     * Return all metadata fields that are found in a given schema.
     *
     * @param context dspace context
     * @param schema schema by db
     * @return array of metadata fields
     * @throws SQLException
     */
    public List<MetadataField> findAllInSchema(Context context, String schema)
            throws SQLException
    {
        return metadataFieldDAO.findAllInSchema(context, schema);
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

        // Ensure the element and qualifier are unique within a given schema.
        if (hasElement(context, metadataField.getFieldID(), metadataField.getMetadataSchema(), metadataField.getElement(), metadataField.getQualifier()))
        {
            throw new NonUniqueMetadataException("Please make " + metadataField.getMetadataSchema().getName() + "." + metadataField.getElement() + "."
                    + metadataField.getQualifier());
        }

        metadataFieldDAO.save(context, metadataField);
        decache();

        log.info(LogManager.getHeader(context, "update_metadatafieldregistry",
                "metadata_field_id=" + metadataField.getFieldID() + "element=" + metadataField.getElement()
                        + "qualifier=" + metadataField.getQualifier()));
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

        metadataFieldDAO.delete(context, metadataField);
        decache();
    }

    /**
     * A sanity check that ensures a given element and qualifier are unique
     * within a given schema. The check happens in code as we cannot use a
     * database constraint.
     *
     * @param context dspace context
     * @param metadataSchema metadataSchema
     * @param element
     * @param qualifier
     * @return true if unique
     * @throws SQLException
     */
    private boolean hasElement(Context context, int fieldId, MetadataSchema metadataSchema, String element, String qualifier) throws SQLException
    {
        return metadataFieldDAO.find(context, fieldId, metadataSchema, element, qualifier) != null;
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
    //TODO: REMOVE CACHE !
    private synchronized void initCache(Context context) throws SQLException
    {
        if (!isCacheInitialized())
        {
            Map<Integer, MetadataField> new_id2field = new HashMap<Integer, MetadataField>();
            log.info("Loading MetadataField elements into cache.");

            // Grab rows from DB
            List<MetadataField> metadataFields = metadataFieldDAO.findAll(context, MetadataField.class);
            for (MetadataField metadataField : metadataFields) {
                new_id2field.put(metadataField.getFieldID(), metadataField);
            }

            id2field = new_id2field;
        }
    }

}