package org.dspace.eperson.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

import javax.mail.MessagingException;
import java.io.IOException;
import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 28/05/14
 * Time: 08:41
 */
public interface AccountService {

    public void sendRegistrationInfo(Context context, String email) throws SQLException, IOException, MessagingException, AuthorizeException;

    public void sendForgotPasswordInfo(Context context, String email) throws SQLException, IOException, MessagingException, AuthorizeException;

    public EPerson getEPerson(Context context, String token)
                throws SQLException, AuthorizeException;


    public String getEmail(Context context, String token)
                throws SQLException;

    public void deleteToken(Context context, String token)
                throws SQLException;
}
