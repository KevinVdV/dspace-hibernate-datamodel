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

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.dao.MetadataFieldDAO;
import org.dspace.content.service.MetadataFieldService;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.springframework.beans.factory.annotation.Autowired;

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
public class MetadataFieldServiceImpl implements MetadataFieldService
{

    /** log4j logger */
    private static Logger log = Logger.getLogger(MetadataFieldServiceImpl.class);

    @Autowired(required = true)
    protected MetadataFieldDAO metadataFieldDAO;

    @Autowired(required = true)
    protected AuthorizeService authorizeService;

    /**
     * Default constructor.
     */
    public MetadataFieldServiceImpl()
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
    @Override
    public MetadataField create(Context context, MetadataSchema metadataSchema, String element, String qualifier, String scopeNote) throws IOException, AuthorizeException,
            SQLException, NonUniqueMetadataException
    {
        // Check authorisation: Only admins may create DC types
        if (!authorizeService.isAdmin(context))
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
        metadataField = metadataFieldDAO.create(context, metadataField);
        metadataFieldDAO.save(context, metadataField);

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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public void update(Context context, MetadataField metadataField) throws SQLException,
            AuthorizeException, NonUniqueMetadataException, IOException
    {
        // Check authorisation: Only admins may update the metadata registry
        if (!authorizeService.isAdmin(context))
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
    @Override
    public void delete(Context context, MetadataField metadataField) throws SQLException, AuthorizeException
    {
        // Check authorisation: Only admins may create DC types
        if (!authorizeService.isAdmin(context))
        {
            throw new AuthorizeException(
                    "Only administrators may modify the metadata registry");
        }

        log.info(LogManager.getHeader(context, "delete_metadata_field",
                "metadata_field_id=" + metadataField.getFieldID()));

        metadataFieldDAO.delete(context, metadataField);
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
    protected boolean hasElement(Context context, int fieldId, MetadataSchema metadataSchema, String element, String qualifier) throws SQLException
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
    @Override
    public String formKey(String schema, String element, String qualifier)
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
    @Override
    public MetadataField find(Context context, int id) throws SQLException
    {
        return metadataFieldDAO.findByID(context, MetadataField.class, id);
    }


}