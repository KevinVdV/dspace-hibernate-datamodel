package org.dspace.content;

import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 24/03/14
 * Time: 09:46
 */
public interface MetadataSchemaRepo {

    public MetadataSchema create(Context context, String name, String namespace) throws SQLException, AuthorizeException, NonUniqueMetadataException;

    public MetadataSchema findByNamespace(Context context, String namespace) throws SQLException;

    public void update(Context context, MetadataSchema metadataSchema) throws SQLException, AuthorizeException, NonUniqueMetadataException;

    public void delete(Context context, MetadataSchema metadataSchema) throws SQLException, AuthorizeException;

    public List<MetadataSchema> findAll(Context context) throws SQLException;

    public MetadataSchema find(Context context, int id) throws SQLException;

    public MetadataSchema find(Context context, String shortName) throws SQLException;
}
