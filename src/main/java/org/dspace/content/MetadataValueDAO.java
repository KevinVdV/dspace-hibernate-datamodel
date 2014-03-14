package org.dspace.content;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.hibernate.HibernateQueryUtil;
import org.dspace.hibernate.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 28/02/14
 * Time: 09:33
 */
public class MetadataValueDAO {

    /** log4j logger */
    private static Logger log = Logger.getLogger(MetadataValueDAO.class);


    /**
     * Creates a new metadata value.
     *
     * @param context
     *            DSpace context object
     * @throws SQLException
     * @throws org.dspace.authorize.AuthorizeException
     */
    public MetadataValue create(Context context, Item item, MetadataField metadataField) throws SQLException {
        MetadataValue metadataValue = new MetadataValue();
        metadataValue.setMetadataField(metadataField);
        metadataValue.setItem(item);
        HibernateQueryUtil.update(context, metadataValue);

        return metadataValue;
    }

    /**
     * Retrieves the metadata value from the database.
     *
     * @param context dspace context
     * @param valueId database key id of value
     * @return recalled metadata value
     * @throws java.io.IOException
     * @throws SQLException
     * @throws org.dspace.authorize.AuthorizeException
     */
    public MetadataValue find(Context context, int valueId)
            throws IOException, SQLException, AuthorizeException
    {
        // Grab rows from DB
        return (MetadataValue) context.getDBConnection().get(MetadataValue.class, valueId);
    }

    /**
     * Retrieves the metadata values for a given field from the database.
     *
     * @param context dspace context
     * @param fieldId field whose values to look for
     * @return a collection of metadata values
     * @throws IOException
     * @throws SQLException
     * @throws AuthorizeException
     */
    public List<MetadataValue> findByField(Context context, int fieldId)
            throws IOException, SQLException, AuthorizeException
    {
        Criteria criteria = context.getDBConnection().createCriteria(MetadataValue.class);
        criteria.add(
                Restrictions.eq("metadata_field_id", fieldId)
        );
        return criteria.list();
    }

    /**
     * Update the metadata value in the database.
     *
     * @param context dspace context
     * @throws SQLException
     * @throws AuthorizeException
     */
    public void update(Context context, MetadataValue metadataValue) throws SQLException {
        HibernateQueryUtil.update(context, metadataValue);

        log.info(LogManager.getHeader(context, "update_metadatavalue",
                "metadata_value_id=" + metadataValue.getFieldId()));

    }

    /**
     * Delete the metadata field.
     *
     * @param context dspace context
     * @throws SQLException
     * @throws AuthorizeException
     */
    public void delete(Context context, MetadataValue metadataValue) throws SQLException {
        log.info(LogManager.getHeader(context, "delete_metadata_value",
                " metadata_value_id=" + metadataValue.getFieldId()));
        HibernateQueryUtil.delete(context, metadataValue);
    }

}
