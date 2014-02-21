package org.dspace.eperson;

import org.dspace.AbstractUnitTest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import java.sql.SQLException;

import static org.junit.Assert.*;

/**
 * Created by Roel on 15/02/14.
 */
public class EPersonTest extends AbstractUnitTest {


    public EPersonTest() {
//        System.setProperty("dspace.configuration","E:\\dspaces\\dspace-tst\\config\\dspace.cfg");
    }



    @Before
    public void setUp() throws SQLException, AuthorizeException {
        EPersonDAO ePersonDAO = new EPersonDAO(context);
        EPerson eperson = ePersonDAO.create(context);
        eperson.setEmail("kevin@dspace.org");
        eperson.setFirstName("Kevin");
        eperson.setLastName("Van de Velde");
        eperson.setNetid("1985");
        ePersonDAO.update(eperson);

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
        EPerson ePersonEntity = EPersonDAO.findByEmail(context, "kevin@dspace.org");
        assertNotNull("No eperson retrieved",ePersonEntity);
        assertEquals("Didn't find the expected entity", "kevin@dspace.org", ePersonEntity.getEmail());
    }

    public void testFindByNetid() throws Exception {
        fail("Not yet implemented");
    }

    @Test
    public void testSearch() throws Exception {
        EPerson[] expectedResult = new EPerson[]{EPersonDAO.findByEmail(context, "kevin@dspace.org")};
//        Search first name
        assertArrayEquals("Find by last name", EPersonDAO.search(context, "Velde"), expectedResult);
//        Search last name
        assertArrayEquals("Find by first name", EPersonDAO.search(context, "kevin"), expectedResult);
//        Search email
        assertArrayEquals("Find by email", EPersonDAO.search(context, "kevin@dspace.org"), expectedResult);
    }

    public void testSearchResultCount() throws Exception {
        fail("Not yet implemented");
    }

    public void testFindAll() throws Exception {
        fail("Not yet implemented");
    }

    public void testCreate() throws Exception {
        fail("Not yet implemented");
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

    public void testCheckPassword() throws Exception {
        fail("Not yet implemented");
    }

    public void testUpdate() throws Exception {
        fail("Not yet implemented");
    }

    public void testUpdateLastModified() throws Exception {
        fail("Not yet implemented");
    }
}
