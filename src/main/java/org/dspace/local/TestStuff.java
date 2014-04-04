package org.dspace.local;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.CollectionManagerImpl;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.eperson.*;
import org.dspace.handle.HandleManager;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
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
        context.turnOffAuthorisationSystem();
        GroupManagerImpl groupManager = new GroupManagerImpl();
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
            new EPersonManagerImpl().delete(context, person);
        }


        context.commit();
        groupManager.delete(context, childGroup);
        groupManager.delete(context, groupEntity);
        //Lookup collection items to test query
        CollectionManagerImpl collectionDAO = new CollectionManagerImpl();
        Collection collection = (Collection) HandleManager.resolveToObject(context, "10986/2117");
        Iterator<Item> allItems = collectionDAO.getAllItems(context, collection);
        while (allItems.hasNext()) {
            Item item = allItems.next();
            System.out.println("Found item: " + item.getID());
        }


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
        EPersonManagerImpl ePersonDAO = new EPersonManagerImpl();
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
