package org.dspace.content;

import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 21/03/14
 * Time: 10:42
 */
public interface MetadataValueManager {

    public MetadataValue create(Context context, Item item, MetadataField metadataField) throws SQLException;

    public MetadataValue find(Context context, int valueId) throws SQLException;

    public List<MetadataValue> findByField(Context context, int fieldId) throws SQLException;

    public void update(Context context, MetadataValue metadataValue) throws SQLException;

    public void delete(Context context, MetadataValue metadataValue) throws SQLException;
}
