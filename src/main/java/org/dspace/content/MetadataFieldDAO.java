package org.dspace.content;

import org.dspace.core.Context;
import org.dspace.dao.AbstractHibernateDAO;

import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 16/03/14
 * Time: 09:43
 * To change this template use File | Settings | File Templates.
 */
public abstract class MetadataFieldDAO extends AbstractHibernateDAO<MetadataField> {

    public abstract MetadataField find(Context context, int metadataFieldId, MetadataSchema metadataSchema, String element,
                                       String qualifier) throws SQLException;

    public abstract MetadataField findByElement(Context context, MetadataSchema metadataSchema, String element, String qualifier) throws SQLException;

    public abstract List<MetadataField> findAllInSchema(Context context, String schema) throws SQLException;

}
