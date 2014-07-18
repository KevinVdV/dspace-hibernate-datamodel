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
        EPerson eperson = ePersonService.create(context);
        eperson.setEmail("kevin@dspace.org");
        eperson.setFirstName("Kevin");
        eperson.setLastName("Van de Velde");
        eperson.setNetid("1985");
        eperson.setPassword("test");
        ePersonService.update(context, eperson);

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
        EPerson ePersonEntity = ePersonService.findByEmail(context, "kevin@dspace.org");
        assertNotNull("No eperson retrieved",ePersonEntity);
        assertEquals("Didn't find the expected entity", "kevin@dspace.org", ePersonEntity.getEmail());
    }

    public void testFindByNetid() throws Exception {
        fail("Not yet implemented");
    }

    @Test
    public void testSearch() throws Exception {
        EPerson[] expectedResult = new EPerson[]{ePersonService.findByEmail(context, "kevin@dspace.org")};
//        Search first name
        List<EPerson> searchResult = ePersonService.search(context, "Velde");
        assertArrayEquals("Find by last name", searchResult.toArray(new EPerson[searchResult.size()]), expectedResult);
//        Search last name
        searchResult = ePersonService.search(context, "kevin");
        assertArrayEquals("Find by first name", searchResult.toArray(new EPerson[searchResult.size()]), expectedResult);
//        Search email
        searchResult = ePersonService.search(context, "kevin@dspace.org");
        assertArrayEquals("Find by email", searchResult.toArray(new EPerson[searchResult.size()]), expectedResult);
    }

    @Test
    public void testSearchResultCount() throws Exception {
//        Search first name
        assertEquals("Count by last name", ePersonService.searchResultCount(context, "Velde"), 1);
//        Search last name
        assertEquals("Count by first name", ePersonService.searchResultCount(context, "kevin"), 1);
//        Search email
        assertEquals("Count by email", ePersonService.searchResultCount(context, "kevin@dspace.org"), 1);
    }

    public void testFindAll() throws Exception {
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

    @Test
    public void testCheckPassword() throws Exception {
        EPerson eperson = ePersonService.findByEmail(context, "kevin@mire.be");
        ePersonService.checkPassword(context, eperson, "test");
    }

    public void testUpdate() throws Exception {
        fail("Not yet implemented");
    }

    public void testUpdateLastModified() throws Exception {
        fail("Not yet implemented");
    }
}
