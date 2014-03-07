package org.dspace.eperson;

import org.dspace.AbstractUnitTest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import java.sql.SQLException;

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
        EPersonDAO ePersonDAO = new EPersonDAO();
        EPerson eperson = ePersonDAO.create(context);
        eperson.setEmail("kevin@dspace.org");
        eperson.setFirstName("Kevin");
        eperson.setLastName("Van de Velde");
        eperson.setNetid("1985");
        eperson.setPassword("test");
        ePersonDAO.update(context, eperson);

    }

    @After
    public void tearDown() throws Exception {
       context.complete();
    }

    //TODO: HIBERNATE determine how best to check ann identifier match ?
//    @Test
//    public void testFind() throws Exception {
//        EPerson ePersonEntity = EPersonDAO.find(context, 1);
//        assertEquals("Didn't find the expected email", "Keyshawn@queenie.org", ePersonEntity.getEmail());
//    }

    @Test
    public void testFindByEmail() throws Exception {
        EPersonDAO ePersonDAO = new EPersonDAO();
        EPerson ePersonEntity = ePersonDAO.findByEmail(context, "kevin@dspace.org");
        assertNotNull("No eperson retrieved",ePersonEntity);
        assertEquals("Didn't find the expected entity", "kevin@dspace.org", ePersonEntity.getEmail());
    }

    public void testFindByNetid() throws Exception {
        fail("Not yet implemented");
    }

    @Test
    public void testSearch() throws Exception {
        EPersonDAO ePersonDAO = new EPersonDAO();
        EPerson[] expectedResult = new EPerson[]{ePersonDAO.findByEmail(context, "kevin@dspace.org")};
//        Search first name
        assertArrayEquals("Find by last name", ePersonDAO.search(context, "Velde"), expectedResult);
//        Search last name
        assertArrayEquals("Find by first name", ePersonDAO.search(context, "kevin"), expectedResult);
//        Search email
        assertArrayEquals("Find by email", ePersonDAO.search(context, "kevin@dspace.org"), expectedResult);
    }

    @Test
    public void testSearchResultCount() throws Exception {
        EPersonDAO ePersonDAO = new EPersonDAO();
//        Search first name
        assertEquals("Count by last name", ePersonDAO.searchResultCount(context, "Velde"), 1);
//        Search last name
        assertEquals("Count by first name", ePersonDAO.searchResultCount(context, "kevin"), 1);
//        Search email
        assertEquals("Count by email", ePersonDAO.searchResultCount(context, "kevin@dspace.org"), 1);
    }

    public void testFindAll() throws Exception {
        fail("Not yet implemented");
    }

    @Test
    public void testCreate() throws Exception {
        EPersonDAO ePersonDAO = new EPersonDAO();
        EPerson eperson = ePersonDAO.create(context);
        EPerson ePersonEntity = ePersonDAO.findByEmail(context, "kevin@dspace.org");
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
        EPerson eperson = new EPersonDAO().findByEmail(context, "kevin@mire.be");
        new EPersonDAO().checkPassword(context, eperson, "test");
    }

    public void testUpdate() throws Exception {
        fail("Not yet implemented");
    }

    public void testUpdateLastModified() throws Exception {
        fail("Not yet implemented");
    }
}
