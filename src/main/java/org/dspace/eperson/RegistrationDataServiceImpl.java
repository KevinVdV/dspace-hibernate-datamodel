package org.dspace.eperson;

import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.eperson.dao.RegistrationDataDAO;
import org.dspace.eperson.service.RegistrationDataService;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 22/04/14
 * Time: 16:51
 */
public class RegistrationDataServiceImpl implements RegistrationDataService
{
    @Autowired(required = true)
    protected RegistrationDataDAO registrationDataDAO;

    @Override
    public RegistrationData create(Context context) throws SQLException, AuthorizeException {
        return registrationDataDAO.create(context, new RegistrationData());
    }


    @Override
    public RegistrationData findByToken(Context context, String token) throws SQLException {
        return registrationDataDAO.findByToken(context, token);
    }

    @Override
    public RegistrationData findByEmail(Context context, String email) throws SQLException {
        return registrationDataDAO.findByEmail(context, email);
    }

    @Override
    public void deleteByToken(Context context, String token) throws SQLException {
        registrationDataDAO.deleteByToken(context, token);

    }

    @Override
    public RegistrationData find(Context context, int id) throws SQLException {
        return registrationDataDAO.findByID(context, RegistrationData.class, id);
    }

    @Override
    public void update(Context context, RegistrationData registrationData) throws SQLException, AuthorizeException {
        registrationDataDAO.save(context, registrationData);

    }

    @Override
    public void delete(Context context, RegistrationData registrationData) throws SQLException, AuthorizeException {
        registrationDataDAO.delete(context, registrationData);
    }
}
