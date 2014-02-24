package org.dspace.local;

import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.eperson.*;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 17/02/14
 * Time: 14:26
 * To change this template use File | Settings | File Templates.
 */
public class TestStuff {

    public static void main(String[] args) throws SQLException, AuthorizeException, EPersonDeletionException {
        Context context = new Context();
        GroupDAO groupManager = new GroupDAO();
        Group groupEntity = groupManager.create(context);
        groupEntity.setName("TEST-GROUP");
        Group childGroup = groupManager.create(context);
        childGroup.setName("CHILD-GROUP");
        groupManager.update(context, childGroup);


        Set<EPerson> people = createPeople(context);

        for(EPerson person : people)
        {
            groupManager.addMember(context, groupEntity, person);
        }
        groupManager.addMember(context, groupEntity, childGroup);
        groupManager.update(context, groupEntity);
        context.commit();
        for(EPerson person : people)
        {
            new EPersonDAO().delete(context, person);
        }


        context.commit();
        groupManager.delete(context, childGroup);
        groupManager.delete(context, groupEntity);
        context.commit();
        context.complete();
    }

    private static Set<EPerson> createPeople(Context context) throws SQLException, AuthorizeException {
        Set<EPerson> result = new HashSet<EPerson>();

        result.add(createPerson(context, "test0@test.be"));
        result.add(createPerson(context, "test1@test.be"));
        return result;
    }

    private static EPerson createPerson(Context context, String mail) throws SQLException, AuthorizeException {
        EPersonDAO ePersonDAO = new EPersonDAO();
        EPerson ePersonEntity = ePersonDAO.findByEmail(context, mail);
        if(ePersonEntity == null)
        {
            ePersonEntity = ePersonDAO.create(context);
            ePersonEntity.setEmail(mail);
            ePersonDAO.update(context, ePersonEntity);
        }
        return ePersonEntity;
    }
}
