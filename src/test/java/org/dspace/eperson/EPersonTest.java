package org.dspace.eperson;

import org.dspace.AbstractUnitTest;
import org.dspace.authorize.AuthorizeException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;

/**
 * Created by Roel on 15/02/14.
 */
public class EPersonTest extends AbstractUnitTest {


    public EPersonTest() {
//        System.setProperty("dspace.configuration","E:\\dspaces\\dspace-tst\\config\\dspace.cfg");
    }



    @Before
    public void setUp() throws SQLException, AuthorizeException {
        EPerson eperson = ePersonManager.create(context);
        eperson.setEmail("kevin@dspace.org");
        eperson.setFirstName("Kevin");
        eperson.setLastName("Van de Velde");
        eperson.setNetid("1985");
        eperson.setPassword("test");
        ePersonManager.update(context, eperson);

    }

    @After
    public void tearDown() throws Exception {
       context.complete();
    }

    //TODO: HIBERNATE determine how best to check ann identifier match ?
//    @Test
//    public void testFind() throws Exception {
//        EPerson ePersonEntity = ePersonRepo.find(context, 1);
//        assertEquals("Didn't find the expected email", "Keyshawn@queenie.org", ePersonEntity.getEmail());
//    }

    @Test
    public void testFindByEmail() throws Exception {
        EPerson ePersonEntity = ePersonManager.findByEmail(context, "kevin@dspace.org");
        assertNotNull("No eperson retrieved",ePersonEntity);
        assertEquals("Didn't find the expected entity", "kevin@dspace.org", ePersonEntity.getEmail());
    }

    public void testFindByNetid() throws Exception {
        fail("Not yet implemented");
    }

    @Test
    public void testSearch() throws Exception {
        EPerson[] expectedResult = new EPerson[]{ePersonManager.findByEmail(context, "kevin@dspace.org")};
//        Search first name
        List<EPerson> searchResult = ePersonManager.search(context, "Velde");
        assertArrayEquals("Find by last name", searchResult.toArray(new EPerson[searchResult.size()]), expectedResult);
//        Search last name
        searchResult = ePersonManager.search(context, "kevin");
        assertArrayEquals("Find by first name", searchResult.toArray(new EPerson[searchResult.size()]), expectedResult);
//        Search email
        searchResult = ePersonManager.search(context, "kevin@dspace.org");
        assertArrayEquals("Find by email", searchResult.toArray(new EPerson[searchResult.size()]), expectedResult);
    }

    @Test
    public void testSearchResultCount() throws Exception {
//        Search first name
        assertEquals("Count by last name", ePersonManager.searchResultCount(context, "Velde"), 1);
//        Search last name
        assertEquals("Count by first name", ePersonManager.searchResultCount(context, "kevin"), 1);
//        Search email
        assertEquals("Count by email", ePersonManager.searchResultCount(context, "kevin@dspace.org"), 1);
    }

    public void testFindAll() throws Exception {
        fail("Not yet implemented");
    }

    @Test
    public void testCreate() throws Exception {
        EPerson eperson = ePersonManager.create(context);
        EPerson ePersonEntity = ePersonManager.findByEmail(context, "kevin@dspace.org");
        //Ensure that the identifier sequence increments by one
        assertEquals("verify identifier sequence", eperson.getID(), ePersonEntity.getID() - 1);
    }

    public void testDelete() throws Exception {
        fail("Not yet implemented");
    }

    public void testSetPassword() throws Exception {
        fail("Not yet implemented");
    }

    public void testSetPasswordHash() throws Exception {
        fail("Not yet implemented");
    }

    public void testGetPasswordHash() throws Exception {
        fail("Not yet implemented");
    }

    @Test
    public void testCheckPassword() throws Exception {
        EPerson eperson = ePersonManager.findByEmail(context, "kevin@mire.be");
        ePersonManager.checkPassword(context, eperson, "test");
    }

    public void testUpdate() throws Exception {
        fail("Not yet implemented");
    }

    public void testUpdateLastModified() throws Exception {
        fail("Not yet implemented");
    }
}
