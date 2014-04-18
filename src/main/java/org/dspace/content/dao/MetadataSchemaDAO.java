package org.dspace.content.dao;

import org.dspace.content.MetadataSchema;
import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 21/03/14
 * Time: 11:41
 */
public interface MetadataSchemaDAO extends GenericDAO<MetadataSchema> {

    /**
     * Get the schema object corresponding to this namespace URI.
     *
     * @param context DSpace context
     * @param namespace namespace URI to match
     * @return metadata schema object or null if none found.
     * @throws java.sql.SQLException
     */
    public MetadataSchema findByNamespace(Context context, String namespace) throws SQLException;

    public boolean uniqueNamespace(Context context, int metadataSchemaId, String namespace) throws SQLException;

    public boolean uniqueShortName(Context context, int metadataSchemaId, String name) throws SQLException;

    public MetadataSchema find(Context context, String shortName) throws SQLException;
}
