package org.dspace.eperson.factory;

import org.dspace.eperson.service.AccountService;
import org.dspace.eperson.service.EPersonService;
import org.dspace.eperson.service.GroupService;
import org.dspace.eperson.service.RegistrationDataService;
import org.dspace.utils.DSpace;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 28/05/14
 * Time: 08:47
 */
public abstract class EPersonServiceFactory {

    public abstract EPersonService getEPersonService();

    public abstract GroupService getGroupService();

    public abstract RegistrationDataService getRegistrationDataService();

    public abstract AccountService getAccountService();

    public static EPersonServiceFactory getInstance(){
        return new DSpace().getServiceManager().getServiceByName("ePersonServiceFactory", EPersonServiceFactory.class);
    }
}
