package org.dspace.content.dao;

import org.dspace.content.MetadataValue;
import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 21/03/14
 * Time: 10:33
 */
public interface MetadataValueDAO extends GenericDAO<MetadataValue> {

    public List<MetadataValue> findByField(Context context, int fieldId) throws SQLException;
}
