package org.dspace.content.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataSchema;
import org.dspace.content.NonUniqueMetadataException;
import org.dspace.core.Context;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 20/03/14
 * Time: 10:03
 */
public interface MetadataFieldService {

    public MetadataField create(Context context, MetadataSchema metadataSchema, String element, String qualifier, String scopeNote) throws IOException, AuthorizeException, SQLException, NonUniqueMetadataException;

    public MetadataField findByElement(Context context, MetadataSchema metadataSchema, String element, String qualifier) throws SQLException;

    public MetadataField find(Context context, int id) throws SQLException;

    public List<MetadataField> findAll(Context context) throws SQLException;

    public List<MetadataField> findAllInSchema(Context context, String schema) throws SQLException;

    public void update(Context context, MetadataField metadataField) throws SQLException, AuthorizeException, NonUniqueMetadataException, IOException;

    public void delete(Context context, MetadataField metadataField) throws SQLException, AuthorizeException;

    public String formKey(String schema, String element, String qualifier);
}
