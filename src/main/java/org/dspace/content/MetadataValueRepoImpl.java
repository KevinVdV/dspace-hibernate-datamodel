package org.dspace.content;

import org.apache.log4j.Logger;
import org.dspace.core.Context;
import org.dspace.core.LogManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 28/02/14
 * Time: 09:33
 */
public class MetadataValueRepoImpl implements MetadataValueRepo {

    /** log4j logger */
    private static Logger log = Logger.getLogger(MetadataValueRepoImpl.class);
    private MetadataValueDAO metadataValueDAO = new MetadataValueDAOImpl();

    /**
     * Creates a new metadata value.
     *
     * @param context
     *            DSpace context object
     * @throws SQLException
     */
    public MetadataValue create(Context context, Item item, MetadataField metadataField) throws SQLException {
        MetadataValue metadataValue = metadataValueDAO.create(context, new MetadataValue());
        metadataValue.setMetadataField(metadataField);
        metadataValue.setItem(item);
        metadataValueDAO.save(context, metadataValue);

        return metadataValue;
    }

    /**
     * Retrieves the metadata value from the database.
     *
     * @param context dspace context
     * @param valueId database key id of value
     * @return recalled metadata value
     * @throws SQLException
     */
    public MetadataValue find(Context context, int valueId) throws SQLException
    {
        // Grab rows from DB
        return metadataValueDAO.findByID(context, MetadataValue.class, valueId);
    }

    /**
     * Retrieves the metadata values for a given field from the database.
     *
     * @param context dspace context
     * @param fieldId field whose values to look for
     * @return a collection of metadata values
     * @throws SQLException
     */
    public List<MetadataValue> findByField(Context context, int fieldId) throws SQLException
    {
        return metadataValueDAO.findByField(context, fieldId);
    }

    /**
     * Update the metadata value in the database.
     *
     * @param context dspace context
     * @throws SQLException
     */
    public void update(Context context, MetadataValue metadataValue) throws SQLException {
        metadataValueDAO.save(context, metadataValue);

        log.info(LogManager.getHeader(context, "update_metadatavalue",
                "metadata_value_id=" + metadataValue.getFieldId()));

    }

    /**
     * Delete the metadata field.
     *
     * @param context dspace context
     * @throws SQLException
     */
    public void delete(Context context, MetadataValue metadataValue) throws SQLException {
        log.info(LogManager.getHeader(context, "delete_metadata_value",
                " metadata_value_id=" + metadataValue.getFieldId()));
        metadataValueDAO.delete(context, metadataValue);
    }
}
