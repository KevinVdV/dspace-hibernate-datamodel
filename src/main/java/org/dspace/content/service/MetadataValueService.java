package org.dspace.content.service;

import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataValue;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 21/03/14
 * Time: 10:42
 */
public interface MetadataValueService {

    public MetadataValue create(Context context, Item item, MetadataField metadataField) throws SQLException;

    public MetadataValue find(Context context, int valueId) throws SQLException;

    public List<MetadataValue> findByField(Context context, int fieldId) throws SQLException;

    public void update(Context context, MetadataValue metadataValue) throws SQLException;

    public void delete(Context context, MetadataValue metadataValue) throws SQLException;
}
