package org.dspace.eperson.factory;

import org.dspace.eperson.service.AccountService;
import org.dspace.eperson.service.EPersonService;
import org.dspace.eperson.service.GroupService;
import org.dspace.eperson.service.RegistrationDataService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 28/05/14
 * Time: 08:48
 */
public class EPersonServiceFactoryImpl extends EPersonServiceFactory {

    @Autowired(required = true)
    private GroupService groupService;
    @Autowired(required = true)
    private EPersonService epersonService;
    @Autowired(required = true)
    private RegistrationDataService registrationDataService;
    @Autowired(required = true)
    private AccountService accountService;

    @Override
    public EPersonService getEPersonService() {
        return epersonService;
    }

    @Override
    public GroupService getGroupService() {
        return groupService;
    }

    @Override
    public RegistrationDataService getRegistrationDataService() {
        return registrationDataService;
    }

    @Override
    public AccountService getAccountService() {
        return accountService;
    }
}
