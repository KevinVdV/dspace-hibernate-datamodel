package org.dspace.eperson.dao;

import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;
import org.dspace.eperson.RegistrationData;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 22/04/14
 * Time: 16:53
 */
public interface RegistrationDataDAO extends GenericDAO<RegistrationData> {

    public RegistrationData findByEmail(Context context, String email) throws SQLException;

    public RegistrationData findByToken(Context context, String token) throws SQLException;

    public void deleteByToken(Context context, String token) throws SQLException;
}
