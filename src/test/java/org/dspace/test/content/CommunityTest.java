/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.test.content;

import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.List;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.core.Context;
import org.dspace.eperson.Group;
import org.junit.*;
import static org.junit.Assert.* ;
import static org.hamcrest.CoreMatchers.*;
import mockit.*;
import org.apache.log4j.Logger;
import org.dspace.app.util.AuthorizeUtil;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.core.Constants;

/**
 * Unit Tests for class Community
 * @author pvillega
 */
public class CommunityTest extends AbstractDSpaceObjectTest
{

    /** log4j category */
    private static final Logger log = Logger.getLogger(CommunityTest.class);

    /**
     * Community instance for the tests
     */
    private Community community;

    /**
     * Community DAO for the test, the same one can be used for all our actions
     */
    private CommunityDAO communityDAO = new CommunityDAO();

    private CollectionDAO collectionDAO = new CollectionDAO();

    /**
     * This method will be run before every test as per @Before. It will
     * initialize resources required for the tests.
     *
     * Other methods can be annotated with @Before here or in subclasses
     * but no execution order is guaranteed
     */
    @Before
    @Override
    public void init()
    {
        super.init();
        try
        {   
            //we have to create a new community in the database
            context.turnOffAuthorisationSystem();
            this.community = communityDAO.create(null, context);
            this.dspaceObject = community;
            //we need to commit the changes so we don't block the table for testing
            context.restoreAuthSystemState();
            context.commit();
        }       
        catch (AuthorizeException ex)
        {
            log.error("Authorization Error in init", ex);
            fail("Authorization Error in init: " + ex.getMessage());
        }
        catch (SQLException ex)
        {
            log.error("SQL Error in init", ex);
            fail("SQL Error in init: " + ex.getMessage());
        }
    }

    /**
     * This method will be run after every test as per @After. It will
     * clean resources initialized by the @Before methods.
     *
     * Other methods can be annotated with @After here or in subclasses
     * but no execution order is guaranteed
     */
    @After
    @Override
    public void destroy()
    {
        community = null;
        super.destroy();
    }

    /**
     * Test of find method, of class Community.
     */
    @Test
    public void testCommunityFind() throws Exception
    {
        int id = community.getID();
        Community found =  communityDAO.find(context, id);
        assertThat("testCommunityFind 0", found, notNullValue());
        assertThat("testCommunityFind 1", found.getID(), equalTo(id));
        //the community created by default has no name
        assertThat("testCommunityFind 2", found.getName(), equalTo(""));
    }

    /**
     * Test of create method, of class Community.
     */
    @Test
    public void testCreateAuth() throws Exception
    {

        //Default to Community-Admin Rights (but not full Admin rights)
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = true;
                AuthorizeManager.isAdmin((Context) any); result = false;
            }
        };

        // test that a Community Admin can create a Community with parent (Sub-Community)
        Community son = communityDAO.create(community, context);
        //the item created by default has no name set
        assertThat("testCreate 2", son, notNullValue());        
        assertThat("testCreate 3", son.getName(), equalTo(""));        
        assertTrue("testCreate 4", communityDAO.getAllParents(son).length == 1);
        assertThat("testCreate 5", communityDAO.getAllParents(son)[0], equalTo(community));
    }


     /**
     * Test of create method, of class Community.
     */
    @Test
    public void testCreateAuth2() throws Exception
    {
        //Default to Admin Rights, but NOT Community Admin Rights
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = false;
                AuthorizeManager.isAdmin((Context) any); result = true;
            }
        };

        //Test that a full Admin can create a Community without a parent (Top-Level Community)
        Community created = communityDAO.create(null, context);
        //the item created by default has no name set
        assertThat("testCreate 0", created, notNullValue());
        assertThat("testCreate 1", created.getName(), equalTo(""));

        //Test that a full Admin can also create a Community with a parent (Sub-Community)
        Community son = communityDAO.create(created, context);
        //the item created by default has no name set
        assertThat("testCreate 2", son, notNullValue());
        assertThat("testCreate 3", son.getName(), equalTo(""));
        assertTrue("testCreate 4", communityDAO.getAllParents(son).length == 1);
        assertThat("testCreate 5", communityDAO.getAllParents(son)[0], equalTo(created));
    }

    /**
     * Test of create method, of class Community.
     */
    @Test(expected=AuthorizeException.class)
    public void testCreateNoAuth() throws Exception
    {
        //Default to NO Admin Rights, and NO Community Admin Rights
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = false;
                AuthorizeManager.isAdmin((Context) any); result = false;
            }
        };

        // test creating community with no parent (as a non-admin & non-Community Admin)
        // this should throw an exception
        Community created = communityDAO.create(null, context);
        fail("Exception expected");
    }

    /**
     * Test of create method (with specified valid handle), of class Community.
     */
    @Test
    public void testCreateWithValidHandle() throws Exception
    {
        //Default to Community Admin Rights, but NO full-Admin rights
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = false;
                AuthorizeManager.isAdmin((Context) any); result = true;
            }
        };

        // test creating community with a specified handle which is NOT already in use
        // (this handle should not already be used by system, as it doesn't start with "1234567689" prefix)
        Community created = communityDAO.create(null, context, "987654321/100");

        // check that community was created, and that its handle was set to proper value
        assertThat("testCreateWithValidHandle 0", created, notNullValue());
        assertThat("testCreateWithValidHandle 1", created.getHandle(context), equalTo("987654321/100"));
    }
    
    
     /**
     * Test of create method (with specified invalid handle), of class Community.
     */
    @Test(expected=IllegalStateException.class)
    public void testCreateWithInvalidHandle() throws Exception
    {
        //Default to Community Admin Rights, but NO full-Admin rights
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = false;
                AuthorizeManager.isAdmin((Context) any); result = true;
            }
        };

        //get handle of our default created community
        String inUseHandle = community.getHandle(context);

        // test creating community with a specified handle which IS already in use
        // This should throw an exception
        Community created = communityDAO.create(null, context, inUseHandle);
        fail("Exception expected");
    }

    /**
     * Test of create method, of class Community.
     */
    @Test(expected=AuthorizeException.class)
    public void testCreateNoAuth2() throws Exception
    {
        //Default to Community Admin Rights, but NO full-Admin rights
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = true;
                AuthorizeManager.isAdmin((Context) any); result = false;
            }
        };

        // test creating community with no parent (as a non-admin, but with Community Admin rights)
        // this should throw an exception, as only admins can create Top Level communities
        Community created = communityDAO.create(null, context);
        fail("Exception expected");
    }

    /**
     * Test of findAll method, of class Community.
     */
    @Test
    public void testFindAll() throws Exception
    {
        Community[] all = communityDAO.findAll(context);
        assertThat("testFindAll 0", all, notNullValue());
        assertTrue("testFindAll 1", all.length >= 1);

        boolean added = false;
        for(Community cm: all)
        {
            if(cm.equals(community))
            {
                added = true;
            }
        }
        assertTrue("testFindAll 2",added);
    }

    /**
     * Test of findAllTop method, of class Community.
     */
    @Test
    public void testFindAllTop() throws Exception
    {
        List<Community> all = communityDAO.findAllTop(context);
        assertThat("testFindAllTop 0", all, notNullValue());
        assertTrue("testFindAllTop 1", all.size() >= 1);
        for(Community cm: all)
        {
            assertThat("testFindAllTop for", communityDAO.getAllParents(cm).length, equalTo(0));
        }

        boolean added = false;
        for(Community cm: all)
        {
            if(cm.equals(community))
            {
                added = true;
            }
        }
        assertTrue("testFindAllTop 2",added);
    }

    /**
     * Test of getID method, of class Community.
     */
    @Test
    @Override
    public void testGetID()
    {
        assertTrue("testGetID 0", community.getID() >= 1);
    }

    /**
     * Test of getHandle method, of class Community.
     */
    @Test
    @Override
    public void testGetHandle() throws Exception {
        //default instance has a random handle
        assertTrue("testGetHandle 0", community.getHandle(context).contains("123456789/"));
    }

    /**
     * Test of getMetadata method, of class Community.
     */
    @Test
    public void testGetMetadata()
    {
        //by default all empty values will return ""
        assertThat("testGetMetadata 0", community.getName(), equalTo(""));
        assertThat("testGetMetadata 1", community.getShortDescription(), equalTo(""));
        assertThat("testGetMetadata 2", community.getIntroductoryText(), equalTo(""));
        assertThat("testGetMetadata 3", community.getLogo(), nullValue());
        assertThat("testGetMetadata 4", community.getCopyrightText(), equalTo(""));
        assertThat("testGetMetadata 5", community.getSideBarText(), equalTo(""));
    }

    /**
     * Test of setMetadata method, of class Community.
     */
    @Test
    public void testSetMetadata() throws Exception
    {
        String name = "name";
        String sdesc = "short description";
        String itext = "introductory text";
        String copy = "copyright declaration";
        String sidebar = "side bar text";

        communityDAO.setName(community, name);
        community.setShortDescription(sdesc);
        community.setIntroductoryText(itext);

        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream logo = communityDAO.setLogo(context, community, new FileInputStream(f));
        community.setCopyrightText(copy);
        community.setSideBarText(sidebar);

        assertThat("testSetMetadata 0", community.getName(), equalTo(name));
        assertThat("testSetMetadata 1", community.getShortDescription(), equalTo(sdesc));
        assertThat("testSetMetadata 2", community.getIntroductoryText(), equalTo(itext));
        assertThat("testSetMetadata 3", community.getLogo(), equalTo(logo));
        assertThat("testSetMetadata 4", community.getCopyrightText(), equalTo(copy));
        assertThat("testSetMetadata 5", community.getSideBarText(), equalTo(sidebar));
    }

    /**
     * Test of getName method, of class Community.
     */
    @Test
    @Override
    public void testGetName()
    {
        //by default is empty
        assertThat("testGetName 0", community.getName(), equalTo(""));
    }

    /**
     * Test of getLogo method, of class Community.
     */
    @Test
    public void testGetLogo()
    {
       //by default is empty
       assertThat("testGetLogo 0", community.getLogo(), nullValue());
    }

    /**
     * Test of setLogo method, of class Community.
     */
    @Test
    public void testSetLogoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = true;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE); result = true;
                 AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.WRITE); result = null;
            }
        };

        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream logo = communityDAO.setLogo(context, community, new FileInputStream(f));
        assertThat("testSetLogoAuth 0", community.getLogo(), equalTo(logo));

        communityDAO.setLogo(context, community, null);
        assertThat("testSetLogoAuth 1", community.getLogo(), nullValue());
    }

    /**
     * Test of setLogo method, of class Community.
     */
    @Test
    public void testSetLogoAuth2() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = false;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE); result = true;
                 AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.WRITE); result = null;
            }
        };

        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream logo = communityDAO.setLogo(context, community, new FileInputStream(f));
        assertThat("testSetLogoAuth2 0", community.getLogo(), equalTo(logo));

        communityDAO.setLogo(context, community, null);
        assertThat("testSetLogoAuth2 1", community.getLogo(), nullValue());
    }

    /**
     * Test of setLogo method, of class Community.
     */
    @Test
    public void testSetLogoAuth3() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = true;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE); result = false;
                 AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.WRITE); result = null;
            }
        };

        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream logo = communityDAO.setLogo(context, community, new FileInputStream(f));
        assertThat("testSetLogoAuth3 0", community.getLogo(), equalTo(logo));

        communityDAO.setLogo(context, community, null);
        assertThat("testSetLogoAuth3 1", community.getLogo(), nullValue());
    }

    /**
     * Test of setLogo method, of class Community.
     */
    @Test
    public void testSetLogoAuth4() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = false;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE); result = false;
                 AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.WRITE); result = null;
            }
        };

        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream logo = communityDAO.setLogo(context, community, new FileInputStream(f));
        assertThat("testSetLogoAuth4 0", community.getLogo(), equalTo(logo));

        communityDAO.setLogo(context, community, null);
        assertThat("testSetLogoAuth4 1", community.getLogo(), nullValue());
    }

    /**
     * Test of setLogo method, of class Community.
     */
    @Test(expected=AuthorizeException.class)
    public void testSetLogoNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = false;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE); result = false;
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.WRITE); result = new AuthorizeException();
            }
        };

        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream logo = communityDAO.setLogo(context, community, new FileInputStream(f));
        fail("EXception expected");
    }

    /**
     * Test of update method, of class Community.
     */
    @Test(expected=AuthorizeException.class)
    public void testUpdateNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.WRITE); result = new AuthorizeException();
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.ADD); result = new AuthorizeException();
            }
        };

        //TODO: we need to verify the update, how?
        communityDAO.update(context, community);
        fail("Exception must be thrown");
    }

    /**
     * Test of update method, of class Community.
     */
    @Test
    public void testUpdateAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.WRITE); result = null;
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.ADD); result = null;
            }
        };

        //TODO: we need to verify the update, how?
        communityDAO.update(context, community);
    }

    /**
     * Test of createAdministrators method, of class Community.
     */
    @Test
    public void testCreateAdministratorsAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeUtil authManager;
            {
                AuthorizeUtil.authorizeManageAdminGroup((Context) any, (Community) any);
                result = null;                
            }
        };

        Group result = communityDAO.createAdministrators(context, community);
        assertThat("testCreateAdministratorsAuth 0", community.getAdministrators(), notNullValue());
        assertThat("testCreateAdministratorsAuth 1", community.getAdministrators(), equalTo(result));
    }

    /**
     * Test of createAdministrators method, of class Community.
     */
    @Test(expected=AuthorizeException.class)
    public void testCreateAdministratorsNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeUtil authManager;
            {
                AuthorizeUtil.authorizeManageAdminGroup((Context) any, (Community) any);
                result = new AuthorizeException();
            }
        };

        Group result = communityDAO.createAdministrators(context, community);
        fail("Exception should have been thrown");
    }


    /**
     * Test of removeAdministrators method, of class Community.
     */
    @Test
    public void testRemoveAdministratorsAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeUtil authManager;
            {
                AuthorizeUtil.authorizeManageAdminGroup((Context) any, (Community) any);
                result = null;
            }
        };

        Group result = communityDAO.createAdministrators(context, community);
        assertThat("testRemoveAdministratorsAuth 0", community.getAdministrators(), notNullValue());
        assertThat("testRemoveAdministratorsAuth 1", community.getAdministrators(), equalTo(result));
        communityDAO.removeAdministrators(context, community);
        assertThat("testRemoveAdministratorsAuth 2", community.getAdministrators(), nullValue());
    }

    /**
     * Test of removeAdministrators method, of class Community.
     */
    @Test(expected=AuthorizeException.class)
    public void testRemoveAdministratorsNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeUtil authManager;
            {
                AuthorizeUtil.authorizeRemoveAdminGroup((Context) any, (Community) any);
                    result = new AuthorizeException();
            }
        };

        communityDAO.removeAdministrators(context, community);
        fail("Should have thrown exception");
    }

    /**
     * Test of getAdministrators method, of class Community.
     */
    @Test
    public void testGetAdministrators() 
    {
        //null by default
        assertThat("testGetAdministrators 0", community.getAdministrators(), nullValue());
    }

    /**
     * Test of getCollections method, of class Community.
     */
    @Test
    public void testGetCollections() throws Exception
    {
         new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.WRITE); result = null;
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.ADD); result = null;
            }
        };

        //empty by default
        assertThat("testGetCollections 0", community.getCollections(), notNullValue());
        assertTrue("testGetCollections 1", community.getCollections().size() == 0);

        Collection result = communityDAO.createCollection(context, community);
        assertThat("testGetCollections 2", community.getCollections(), notNullValue());
        assertTrue("testGetCollections 3", community.getCollections().size() == 1);
        assertThat("testGetCollections 4", community.getCollections().iterator().next(), equalTo(result));
    }

    /**
     * Test of getSubcommunities method, of class Community.
     */
    @Test
    public void testGetSubcommunities() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.ADD); result = null;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = true;
            }
        };

        //empty by default
        assertThat("testGetSubcommunities 0", community.getSubCommunities(), notNullValue());
        assertTrue("testGetSubcommunities 1", community.getSubCommunities().size() == 0);

        //community with  parent
        Community son = communityDAO.create(community, context);
        assertThat("testGetSubcommunities 2", community.getSubCommunities(), notNullValue());
        assertTrue("testGetSubcommunities 3", community.getSubCommunities().size() == 1);
        assertThat("testGetSubcommunities 4", community.getSubCommunities().iterator().next(), equalTo(son));
    }

    /**
     * Test of getParentCommunity method, of class Community.
     */
    @Test
    public void testGetParentCommunity() throws Exception 
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.ADD); result = null;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = true;
            }
        };

        //null by default
        assertThat("testGetParentCommunity 0", community.getParentCommunity(), nullValue());

        //community with  parent
        Community son = communityDAO.create(community, context);
        assertThat("testGetParentCommunity 1",son.getParentCommunity(), notNullValue());
        assertThat("testGetParentCommunity 2", son.getParentCommunity(), equalTo(community));
    }

    /**
     * Test of getAllParents method, of class Community.
     */
    @Test
    public void testGetAllParents() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.ADD); result = null;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = true;
            }
        };

        //empty by default
        assertThat("testGetAllParents 0",communityDAO.getAllParents(community), notNullValue());
        assertTrue("testGetAllParents 1", communityDAO.getAllParents(community).length == 0);

        //community with  parent
        Community son = communityDAO.create(community, context);
        assertThat("testGetAllParents 2",communityDAO.getAllParents(son), notNullValue());
        assertTrue("testGetAllParents 3", communityDAO.getAllParents(son).length == 1);
        assertThat("testGetAllParents 4", communityDAO.getAllParents(son)[0], equalTo(community));
    }

    /**
     * Test of getAllCollections method, of class Community.
     */
    @Test
    public void testGetAllCollections() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.ADD); result = null;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = true;
            }
        };

        //empty by default
        assertThat("testGetAllCollections 0",communityDAO.getAllCollections(community), notNullValue());
        assertTrue("testGetAllCollections 1", communityDAO.getAllCollections(community).length == 0);

        //community has a collection and a subcommunity, subcommunity has a collection
        Collection collOfC = communityDAO.createCollection(context, community);
        Community sub = communityDAO.create(community, context);
        Collection collOfSub = communityDAO.createCollection(context, sub);
        assertThat("testGetAllCollections 2",communityDAO.getAllCollections(community), notNullValue());
        assertTrue("testGetAllCollections 3", communityDAO.getAllCollections(community).length == 2);
        assertThat("testGetAllCollections 4", communityDAO.getAllCollections(community)[0], equalTo(collOfSub));
        assertThat("testGetAllCollections 5", communityDAO.getAllCollections(community)[1], equalTo(collOfC));
    }

    /**
     * Test of createCollection method, of class Community.
     */
    @Test
    public void testCreateCollectionAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.ADD); result = null;
            }
        };

        Collection result = communityDAO.createCollection(context, community);
        assertThat("testCreateCollectionAuth 0", result, notNullValue());
        assertThat("testCreateCollectionAuth 1", community.getCollections(), notNullValue());
        assertThat("testCreateCollectionAuth 2", community.getCollections().iterator().next(), equalTo(result));
    }

    /**
     * Test of createCollection method, of class Community.
     */
    @Test(expected=AuthorizeException.class)
    public void testCreateCollectionNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.ADD); result = new AuthorizeException();
            }
        };

        Collection result = communityDAO.createCollection(context, community);
        fail("Exception expected");
    }

    /**
     * Test of addCollection method, of class Community.
     */
    @Test
    public void testAddCollectionAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.ADD); result = null;
            }
        };

        Collection col = communityDAO.createCollection(context, community);
        assertThat("testAddCollectionAuth 0", community.getCollections(), notNullValue());
        assertThat("testAddCollectionAuth 1", community.getCollections().iterator().next(), equalTo(col));
    }

    /**
     * Test of addCollection method, of class Community.
     */
    @Test(expected=AuthorizeException.class)
    public void testAddCollectionNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.ADD); result = new AuthorizeException();
            }
        };

        Collection col = communityDAO.createCollection(context, community);
        communityDAO.addCollection(context, community, col);
        fail("Exception expected");
    }

    /**
     * Test of createSubcommunity method, of class Community.
     */
    @Test
    public void testCreateSubcommunityAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.ADD); result = null;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = true;
            }
        };

        Community result = communityDAO.createSubcommunity(context, community);
        assertThat("testCreateSubcommunityAuth 0", community.getSubCommunities(), notNullValue());
        assertTrue("testCreateSubcommunityAuth 1", community.getSubCommunities().size() == 1);
        assertThat("testCreateSubcommunityAuth 2", community.getSubCommunities().iterator().next(), equalTo(result));
    }

    /**
     * Test of createSubcommunity method, of class Community.
     */
    @Test(expected=AuthorizeException.class)
    public void testCreateSubcommunityNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.ADD); result = new AuthorizeException();
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = true;
            }
        };

        Community result = communityDAO.createSubcommunity(context, community);
        fail("Exception expected");
    }

    /**
     * Test of addSubcommunity method, of class Community.
     */
    @Test
    public void testAddSubcommunityAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.ADD); result = null;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = true;
                AuthorizeManager.isAdmin((Context) any); result = true;
            }
        };

        Community result = communityDAO.create(null, context);
        communityDAO.addSubcommunity(context, community, result);
        assertThat("testAddSubcommunityAuth 0", community.getSubCommunities(), notNullValue());
        assertTrue("testAddSubcommunityAuth 1", community.getSubCommunities().size() == 1);
        assertThat("testAddSubcommunityAuth 2", community.getSubCommunities().iterator().next(), equalTo(result));
    }

    /**
     * Test of addSubcommunity method, of class Community.
     */
    @Test(expected=AuthorizeException.class)
    public void testAddSubcommunityNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.ADD); result = new AuthorizeException();
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = true;
            }
        };

        Community result = communityDAO.create(null, context);
        communityDAO.addSubcommunity(context, community, result);
        fail("Exception expected");
    }

    /**
     * Test of removeCollection method, of class Community.
     */
    @Test
    public void testRemoveCollectionAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            AuthorizeUtil authUtil;
            {
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.ADD); result = null;
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.REMOVE); result = null;
                AuthorizeUtil.authorizeManageTemplateItem((Context) any, (Collection) any);
                        result = null;
            }
        };

        Collection col = communityDAO.createCollection(context, community);
        communityDAO.addCollection(context, community, col);
        assertThat("testRemoveCollectionAuth 0", community.getCollections(), notNullValue());
        assertTrue("testRemoveCollectionAuth 1", community.getCollections().size() == 1);
        assertThat("testRemoveCollectionAuth 2", community.getCollections().iterator().next(), equalTo(col));
        
        communityDAO.removeCollection(context, community, col);
        assertThat("testRemoveCollectionAuth 3", community.getCollections(), notNullValue());
        assertTrue("testRemoveCollectionAuth 4", community.getCollections().size() == 0);
    }

    /**
     * Test of removeCollection method, of class Community.
     */
    @Test(expected=AuthorizeException.class)
    public void testRemoveCollectionNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.ADD); result = null;
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.REMOVE); result = new AuthorizeException();
            }
        };

        Collection col = communityDAO.createCollection(context, community);
        assertThat("testRemoveCollectionNoAuth 0", community.getCollections(), notNullValue());
        assertTrue("testRemoveCollectionNoAuth 1", community.getCollections().size() == 1);
        assertThat("testRemoveCollectionNoAuth 2", community.getCollections().iterator().next(), equalTo(col));

        communityDAO.removeCollection(context, community, col);
        fail("Exception expected");
    }

    /**
     * Test of removeSubcommunity method, of class Community.
     */
    @Test
    public void testRemoveSubcommunityAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.ADD); result = null;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = true;
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.REMOVE); result = null;
                AuthorizeManager.isAdmin((Context) any); result = true;
            }
        };

        Community com = communityDAO.create(null,context);
        communityDAO.addSubcommunity(context, community, com);
        assertThat("testRemoveSubcommunityAuth 0", community.getSubCommunities(), notNullValue());
        assertTrue("testRemoveSubcommunityAuth 1", community.getSubCommunities().size() == 1);
        assertThat("testRemoveSubcommunityAuth 2", community.getSubCommunities().iterator().next(), equalTo(com));

        communityDAO.removeSubcommunity(context, community, com);
        assertThat("testRemoveSubcommunityAuth 3", community.getSubCommunities(), notNullValue());
        assertTrue("testRemoveSubcommunityAuth 4", community.getSubCommunities().size() == 0);
    }

    /**
     * Test of delete method, of class Community.
     */
    @Test
    public void testDeleteAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.DELETE); result = null;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.REMOVE); result = false;
            }
        };

        int id = community.getID();
        communityDAO.delete(context, community);
        Community found = communityDAO.find(context, id);
        assertThat("testDeleteAuth 0",found, nullValue());
    }

    /**
     * Test of delete method, of class Community.
     */
    @Test
    public void testDeleteAuth2() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.DELETE); result = null;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.REMOVE); result = true;
            }
        };

        int id = community.getID();
        communityDAO.delete(context, community);
        Community found = communityDAO.find(context, id);
        assertThat("testDeleteAuth2 0",found, nullValue());
    }

    /**
     * Test of delete method, of class Community.
     */
    @Test(expected=AuthorizeException.class)
    public void testDeleteNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.DELETE); result = new AuthorizeException();
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.REMOVE); result = false;
            }
        };

        int id = community.getID();
        communityDAO.delete(context, community);
        fail("Exception expected");
    }

    /**
     * Test of equals method, of class Community.
     */
    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void testEquals() throws SQLException, AuthorizeException
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.isAdmin((Context) any); result = true;
            }
        };

        assertFalse("testEquals 0", community.equals(null));
        assertFalse("testEquals 1", community.equals(communityDAO.create(null, context)));
        assertTrue("testEquals 2", community.equals(community));
    }

    /**
     * Test of getType method, of class Community.
     */
    @Test
    @Override
    public void testGetType()
    {
        assertThat("testGetType 0", community.getType(), equalTo(Constants.COMMUNITY));
    }

    /**
     * Test of canEditBoolean method, of class Community.
     */
    @Test
    public void testCanEditBooleanAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE); result = true;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = true;
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.WRITE); result = null;
            }
        };

        assertTrue("testCanEditBooleanAuth 0", communityDAO.canEditBoolean(context, community));
    }

    /**
     * Test of canEditBoolean method, of class Community.
     */
    @Test
    public void testCanEditBooleanAuth2() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE); result = false;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = true;
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.WRITE); result = null;
            }
        };

        assertTrue("testCanEditBooleanAuth2 0", communityDAO.canEditBoolean(context, community));
    }

    /**
     * Test of canEditBoolean method, of class Community.
     */
    @Test
    public void testCanEditBooleanAuth3() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE); result = true;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = false;
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.WRITE); result = null;
            }
        };

        assertTrue("testCanEditBooleanAuth3 0", communityDAO.canEditBoolean(context, community));
    }

    /**
     * Test of canEditBoolean method, of class Community.
     */
    @Test
    public void testCanEditBooleanAuth4() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE); result = false;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = false;
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.WRITE); result = null;
            }
        };

        assertTrue("testCanEditBooleanAuth4 0", communityDAO.canEditBoolean(context, community));
    }

    /**
     * Test of canEditBoolean method, of class Community.
     */
    @Test
    public void testCanEditBooleanNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE); result = false;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = false;
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.WRITE); result = new AuthorizeException();
            }
        };

        assertFalse("testCanEditBooleanNoAuth 0", communityDAO.canEditBoolean(context, community));
    }    

    /**
     * Test of canEdit method, of class Community.
     */
    @Test
    public void testCanEditAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE); result = false;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = false;
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.WRITE); result = null;
            }
        };

        communityDAO.canEdit(context, community);
    }

    /**
     * Test of canEdit method, of class Community.
     */
    @Test
    public void testCanEditAuth1() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE); result = true;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = false;
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.WRITE); result = null;
            }
        };

        communityDAO.canEdit(context, community);
    }

    /**
     * Test of canEdit method, of class Community.
     */
    @Test
    public void testCanEditAuth2() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE); result = false;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = true;
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.WRITE); result = null;
            }
        };

        communityDAO.canEdit(context, community);
    }

    /**
     * Test of canEdit method, of class Community.
     */
    @Test(expected=AuthorizeException.class)
    public void testCanEditNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                 AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE); result = false;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD); result = false;
                AuthorizeManager.authorizeAction((Context) any, (Community) any,
                        Constants.WRITE); result = new AuthorizeException();
            }
        };

        communityDAO.canEdit(context, community);
        fail("Exception expected");
    }

    /**
     * Test of countItems method, of class Community.
     */
    @Test
    public void testCountItems() throws Exception 
    {
        //0 by default
        //TODO: HIBERNATE: TEST WHEN COUNT ITEMS BECOMES AVAILABLE
//        assertTrue("testCountItems 0", c.countItems() == 0);
    }

    /**
     * Test of getAdminObject method, of class Community.
     */
    @Test
    @Override
    public void testGetAdminObject() throws SQLException
    {
        //default community has no admin object
        assertThat("testGetAdminObject 0", (Community)communityDAO.getAdminObject(community, Constants.REMOVE), equalTo(community));
        assertThat("testGetAdminObject 1", (Community)communityDAO.getAdminObject(community, Constants.ADD), equalTo(community));
        assertThat("testGetAdminObject 2", communityDAO.getAdminObject(community, Constants.DELETE), nullValue());
        assertThat("testGetAdminObject 3", (Community)communityDAO.getAdminObject(community, Constants.ADMIN), equalTo(community));
    }

    /**
     * Test of getParentObject method, of class Community.
     */
    @Test
    @Override
    public void testGetParentObject() throws SQLException
    {
        try
        {
            //default has no parent
            assertThat("testGetParentObject 0", communityDAO.getParentObject(context, community), nullValue());

            context.turnOffAuthorisationSystem();
            Community son = communityDAO.createSubcommunity(context, community);
            context.restoreAuthSystemState();
            assertThat("testGetParentObject 1", communityDAO.getParentObject(context, son), notNullValue());
            assertThat("testGetParentObject 2", (Community)communityDAO.getParentObject(context, son), equalTo(community));
        }
        catch(AuthorizeException ex)
        {
            fail("Authorize exception catched");
        }
    }

}