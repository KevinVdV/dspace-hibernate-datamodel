package org.dspace.eperson;

import org.dspace.AbstractUnitTest;
import org.dspace.core.Context;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;

/**
 * Created by Roel on 15/02/14.
 */
public class EPersonTest extends AbstractUnitTest {


    public EPersonTest() {
//        System.setProperty("dspace.configuration","E:\\dspaces\\dspace-tst\\config\\dspace.cfg");
    }



    @Before
    public void setUp()
    {
    }

    @After
    public void tearDown() throws Exception {
       context.complete();
    }

    @Test
    public void testFind() throws Exception {
        EPerson ePersonEntity = EPersonDAO.find(context, 1);
        assertEquals("Didn't find the expected email", "Keyshawn@queenie.org", ePersonEntity.getEmail());
    }

    @Test
    public void testFindByEmail() throws Exception {
        EPerson ePersonEntity = EPersonDAO.findByEmail(context, "Keyshawn@queenie.org");
        assertNotNull("No eperson retrieved",ePersonEntity);
        assertEquals("Didn't find the expected entity", 1, ePersonEntity.getID());
    }

    public void testFindByNetid() throws Exception {
        fail("Not yet implemented");
    }

    @Test
    public void testSearch() throws Exception {
        EPerson[] expectedResult = new EPerson[]{EPersonDAO.findByEmail(context, "Sydnie@delaney.org")};
//        Search first name
        assertArrayEquals(EPersonDAO.search(context, "Fabiola"), expectedResult);
//        Search last name
        assertArrayEquals(EPersonDAO.search(context, "Fisher"), expectedResult);
//        Search identifier
        assertArrayEquals(EPersonDAO.search(context, "3"), expectedResult);
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
