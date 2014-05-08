package org.dspace.eperson.service;

import org.dspace.core.Context;
import org.dspace.eperson.RegistrationData;
import org.dspace.service.DSpaceCRUDService;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 22/04/14
 * Time: 16:45
 */
public interface RegistrationDataService extends DSpaceCRUDService<RegistrationData> {

    public RegistrationData findByToken(Context context, String token) throws SQLException;

    public RegistrationData findByEmail(Context context, String email) throws SQLException;

    public void deleteByToken(Context context, String token) throws SQLException;

}
