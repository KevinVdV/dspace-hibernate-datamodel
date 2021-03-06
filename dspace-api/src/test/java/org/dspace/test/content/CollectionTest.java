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
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.dspace.authorize.AuthorizeException;
import org.apache.log4j.Logger;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.*;
import org.dspace.core.Context;
import org.dspace.eperson.Group;
import org.junit.*;
import static org.junit.Assert.* ;
import static org.hamcrest.CoreMatchers.*;
import mockit.*;
import org.dspace.app.util.AuthorizeUtil;
import org.dspace.core.Constants;

/**
 * Unit Tests for class Collection
 * @author pvillega
 */
public class CollectionTest extends AbstractDSpaceObjectTest
{

    /** log4j category */
    private static final Logger log = Logger.getLogger(CollectionTest.class);

    /**
     * Collection instance for the tests
     */
    private Collection collection;

    private Community owningCommunity;


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
            this.owningCommunity = communityService.create(context, null);
            this.collection = collectionService.create(context, owningCommunity);
            this.dspaceObject = collection;
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
    public void destroy() throws Exception {
        collection = null;
        super.destroy();
    }

    /**
     * Test of find method, of class Collection.
     */
    @Test
    public void testCollectionFind() throws Exception
    {
        UUID id = collection.getID();
        Collection found =  collectionService.find(context, id);
        assertThat("testCollectionFind 0", found, notNullValue());
        assertThat("testCollectionFind 1", found.getID(), equalTo(id));
        //the community created by default has no name
        assertThat("testCollectionFind 2", found.getName(), nullValue());
    }

    /**
     * Test of create method, of class Collection.
     */
    @Test
    public void testCreate() throws Exception
    {
        Collection created = collectionService.create(context, owningCommunity);
        assertThat("testCreate 0", created, notNullValue());
        assertThat("testCreate 1", created.getName(), equalTo(""));
    }

     /**
     * Test of create method (with specified valid handle), of class Collection
     */
    @Test
    public void testCreateWithValidHandle() throws Exception
    {
        //Default to Collection Admin Rights, but NO full-Admin rights
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Collection) any,
                        Constants.ADD); result = false;
                authorizeService.isAdmin((Context) any); result = true;
            }
        };
        // test creating collection with a specified handle which is NOT already in use
        // (this handle should not already be used by system, as it doesn't start with "1234567689" prefix)
        Collection created = collectionService.create(context, owningCommunity, "987654321/100");

        // check that collection was created, and that its handle was set to proper value
        assertThat("testCreateWithValidHandle 0", created, notNullValue());
        assertThat("testCreateWithValidHandle 1", created.getHandle(context), equalTo("987654321/100"));
    }


     /**
     * Test of create method (with specified invalid handle), of class Collection.
     */
    @Test(expected=IllegalStateException.class)
    public void testCreateWithInvalidHandle() throws Exception
    {
        //Default to Collection Admin Rights, but NO full-Admin rights
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Collection) any,
                        Constants.ADD); result = false;
                authorizeService.isAdmin((Context) any); result = true;
            }
        };


        //get handle of our default created collection
        String inUseHandle = collection.getHandle(context);

        // test creating collection with a specified handle which IS already in use
        // This should throw an exception
        Collection created = collectionService.create(context, owningCommunity, inUseHandle);
        fail("Exception expected");
    }


    /**
     * Test of findAll method, of class Collection.
     */
    @Test
    public void testFindAll() throws Exception
    {
        List<Collection> all = collectionService.findAll(context);
        assertThat("testFindAll 0", all, notNullValue());
        assertTrue("testFindAll 1", all.size() >= 1);

        boolean added = false;
        for(Collection cl: all)
        {
            if(cl.equals(collection))
            {
                added = true;
            }
        }
        assertTrue("testFindAll 2",added);
    }

    /**
     * Test of getItems method, of class Collection.
     */
    @Test
    public void testGetItems() throws Exception
    {
        Iterator<Item> items = itemService.findByCollection(context, collection);
        assertThat("testGetItems 0", items, notNullValue());
        //by default is empty
        assertFalse("testGetItems 1", items.hasNext());
    }

    /**
     * Test of getAllItems method, of class Collection.
     */
    @Test
    public void testGetAllItems() throws Exception
    {
        Iterator<Item> items = itemService.findByCollection(context, collection);
        assertThat("testGetAllItems 0", items, notNullValue());
        //by default is empty
        assertFalse("testGetAllItems 1", items.hasNext());
    }

    /**
     * Test of getID method, of class Collection.
     */
    @Test
    @Override
    public void testGetID()
    {
        assertTrue("testGetID 0", collection.getID() != null);
    }

    /**
     * Test of getHandle method, of class Collection.
     */
    @Test
    @Override
    public void testGetHandle() throws Exception {
        //default instance has a random handle
        assertTrue("testGetHandle 0", collection.getHandle(context).contains("123456789/"));
    }

    /**
     * Test of getMetadata method, of class Collection.
     */
    @Test
    public void testGetMetadata()
    {
        //by default all empty values will return ""
        assertThat("testGetMetadata 0", collection.getName(), nullValue());
        assertThat("testGetMetadata 1", collection.getShortDescription(), nullValue());
        assertThat("testGetMetadata 2", collection.getIntroductoryText(), nullValue());
        assertThat("testGetMetadata 3", collection.getLogo(), nullValue());
        assertThat("testGetMetadata 4", collection.getCopyrightText(), nullValue());
        assertThat("testGetMetadata 5", collection.getTemplateItem(), nullValue());
        assertThat("testGetMetadata 6", collection.getProvenanceDescription(), nullValue());
        assertThat("testGetMetadata 7", collection.getSideBarText(), nullValue());
        assertThat("testGetMetadata 8", collectionService.getLicense(collection), nullValue());
    }

    /**
     * Test of setMetadata method, of class Collection.
     */
    @Test
    public void testSetMetadata() throws Exception
    {
        String name = "name";
        String sdesc = "short description";
        String itext = "introductory text";
        String copy = "copyright declaration";
        String sidebar = "side bar text";
        String tempItem = "3";
        String provDesc = "provenance description";
        String license = "license text";

        collectionService.setName(collection, name);
        collection.setShortDescription(sdesc);
        collection.setIntroductoryText(itext);
        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream logo = collectionService.createLogo(context, collection, new FileInputStream(f));
        collection.setCopyrightText(copy);
        collection.setSideBarText(sidebar);
        Item templateItem = collectionService.createTemplateItem(context, collection);
        collection.setProvenanceDescription(provDesc);
        collection.setLicense(license);

        assertThat("testSetMetadata 0", collection.getName(), equalTo(name));
        assertThat("testSetMetadata 1", collection.getShortDescription(), equalTo(sdesc));
        assertThat("testSetMetadata 2", collection.getIntroductoryText(), equalTo(itext));
        assertThat("testSetMetadata 3", collection.getLogo(), equalTo(logo));
        assertThat("testSetMetadata 4", collection.getCopyrightText(), equalTo(copy));
        assertThat("testSetMetadata 5", collection.getSideBarText(), equalTo(sidebar));
        assertThat("testGetMetadata 6", collection.getTemplateItem(), equalTo(templateItem));
        assertThat("testGetMetadata 7", collection.getProvenanceDescription(), equalTo(provDesc));
        assertThat("testGetMetadata 8", collectionService.getLicense(collection), equalTo(license));
    }

    /**
     * Test of getName method, of class Collection.
     */
    @Test
    @Override
    public void testGetName()
    {
        //by default is empty
        assertThat("testGetName 0", collection.getName(), nullValue());
    }

    /**
     * Test of getLogo method, of class Collection.
     */
    @Test
    public void testGetLogo()
    {
        //by default is empty
        assertThat("testGetLogo 0", collection.getLogo(), nullValue());
    }

    /**
     * Test of setLogo method, of class Collection.
     */
    @Test
    public void testSetLogoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = true;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = true;
                 authorizeService.authorizeAction((Context) any, (Collection) any,
                         Constants.WRITE, true); result = null;
            }
        };

        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream logo = collectionService.createLogo(context, collection, new FileInputStream(f));
        assertThat("testSetLogoAuth 0", collection.getLogo(), equalTo(logo));

        collectionService.createLogo(context, collection, null);
        assertThat("testSetLogoAuth 1", collection.getLogo(), nullValue());
    }

    /**
     * Test of setLogo method, of class Collection.
     */
    @Test
    public void testSetLogoAuth2() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = false;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = true;
                 authorizeService.authorizeAction((Context) any, (Collection) any,
                         Constants.WRITE, true); result = null;
            }
        };

        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream logo = collectionService.createLogo(context, collection, new FileInputStream(f));
        assertThat("testSetLogoAuth2 0", collection.getLogo(), equalTo(logo));

        collectionService.createLogo(context, collection, null);
        assertThat("testSetLogoAuth2 1", collection.getLogo(), nullValue());
    }

    /**
     * Test of setLogo method, of class Collection.
     */
    @Test
    public void testSetLogoAuth3() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = true;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = false;
                 authorizeService.authorizeAction((Context) any, (Collection) any,
                         Constants.WRITE, true); result = null;
            }
        };

        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream logo = collectionService.createLogo(context, collection, new FileInputStream(f));
        assertThat("testSetLogoAuth3 0", collection.getLogo(), equalTo(logo));

        collectionService.createLogo(context, collection, null);
        assertThat("testSetLogoAuth3 1", collection.getLogo(), nullValue());
    }

    /**
     * Test of setLogo method, of class Collection.
     */
    @Test
    public void testSetLogoAuth4() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = false;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = false;
                 authorizeService.authorizeAction((Context) any, (Collection) any,
                         Constants.WRITE, true); result = null;
            }
        };

        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream logo = collectionService.createLogo(context, collection, new FileInputStream(f));
        assertThat("testSetLogoAuth4 0", collection.getLogo(), equalTo(logo));

        collectionService.createLogo(context, collection, null);
        assertThat("testSetLogoAuth4 1", collection.getLogo(), nullValue());
    }

    /**
     * Test of setLogo method, of class Collection.
     */
    @Test(expected=AuthorizeException.class)
    public void testSetLogoNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = false;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = false;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = new AuthorizeException();
            }
        };

        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream logo = collectionService.createLogo(context, collection, new FileInputStream(f));
        fail("EXception expected");
    }

    /**
     * Test of createWorkflowGroup method, of class Collection.
     */
    @Test
    public void testCreateWorkflowGroupAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeUtil authManager;
            {
                AuthorizeUtil.authorizeManageWorkflowsGroup((Context) any, (Collection) any);
                    result = null;
            }
        };

        int step = 1;
        Group result = collectionService.createWorkflowGroup(context, collection, step);
        assertThat("testCreateWorkflowGroupAuth 0", result, notNullValue());
    }

    /**
     * Test of createWorkflowGroup method, of class Collection.
     */
    @Test(expected=AuthorizeException.class)
    public void testCreateWorkflowGroupNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeUtil authManager;
            {
                AuthorizeUtil.authorizeManageWorkflowsGroup((Context) any, (Collection) any);
                    result = new AuthorizeException();
            }
        };

        int step = 1;
        Group result = collectionService.createWorkflowGroup(context,  collection, step);
        fail("Exception expected");
    }

    /**
     * Test of setWorkflowGroup method, of class Collection.
     */
    @Test
    public void testSetWorkflowGroup() throws Exception
    {
        context.turnOffAuthorisationSystem();
        int step = 1;
        Group g = groupService.create(context);
        context.commit();
        context.restoreAuthSystemState();
        collectionService.setWorkflowGroup(collection, step, g);
        assertThat("testSetWorkflowGroup 0", collectionService.getWorkflowGroup(collection, step), notNullValue());
        assertThat("testSetWorkflowGroup 1", collectionService.getWorkflowGroup(collection, step), equalTo(g));
    }

    /**
     * Test of getWorkflowGroup method, of class Collection.
     */
    @Test
    public void testGetWorkflowGroup() throws Exception
    {
        //null by default
        int step = 1;
        assertThat("testGetWorkflowGroup 0", collectionService.getWorkflowGroup(collection, step), nullValue());
    }

    /**
     * Test of createSubmitters method, of class Collection.
     */
    @Test
    public void testCreateSubmittersAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeUtil authManager;
            {
                AuthorizeUtil.authorizeManageSubmittersGroup((Context) any, (Collection) any);
                    result = null;
            }
        };

        Group result = collectionService.createSubmitters(context, collection);
        assertThat("testCreateSubmittersAuth 0",result, notNullValue());
    }

    /**
     * Test of createSubmitters method, of class Collection.
     */
    @Test(expected=AuthorizeException.class)
    public void testCreateSubmittersNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeUtil authManager;
            {
                AuthorizeUtil.authorizeManageSubmittersGroup((Context) any, (Collection) any);
                    result = new AuthorizeException();
            }
        };

        Group result = collectionService.createSubmitters(context, collection);
        fail("Exception expected");
    }

    /**
     * Test of removeSubmitters method, of class Collection.
     */
    @Test
    public void testRemoveSubmittersAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeUtil authManager;
            {
                AuthorizeUtil.authorizeManageSubmittersGroup((Context) any, (Collection) any);
                    result = null;
            }
        };

        collectionService.removeSubmitters(context, collection);
        assertThat("testRemoveSubmittersAuth 0", collection.getSubmitters(), nullValue());
    }

    /**
     * Test of removeSubmitters method, of class Collection.
     */
    @Test(expected=AuthorizeException.class)
    public void testRemoveSubmittersNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeUtil authManager;
            {
                AuthorizeUtil.authorizeManageSubmittersGroup((Context) any, (Collection) any);
                    result = new AuthorizeException();
            }
        };

        collectionService.removeSubmitters(context, collection);
        fail("Exception expected");
    }

    /**
     * Test of getSubmitters method, of class Collection.
     */
    @Test
    public void testGetSubmitters()
    {
        assertThat("testGetSubmitters 0", collection.getSubmitters(), nullValue());
    }

    /**
     * Test of createAdministrators method, of class Collection.
     */
    @Test
    public void testCreateAdministratorsAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeUtil authManager;
            {
                AuthorizeUtil.authorizeManageAdminGroup((Context) any, (Collection) any);
                    result = null;
            }
        };

        Group result = collectionService.createAdministrators(context, collection);
        assertThat("testCreateAdministratorsAuth 0", result, notNullValue());
    }

    /**
     * Test of createAdministrators method, of class Collection.
     */
    @Test(expected=AuthorizeException.class)
    public void testCreateAdministratorsNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeUtil authManager;
            {
                AuthorizeUtil.authorizeManageAdminGroup((Context) any, (Collection) any);
                    result = new AuthorizeException();
            }
        };

        Group result = collectionService.createAdministrators(context, collection);
        fail("Exception expected");
    }

    /**
     * Test of removeAdministrators method, of class Collection.
     */
    @Test
    public void testRemoveAdministratorsAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeUtil authManager;
            {
                AuthorizeUtil.authorizeRemoveAdminGroup((Context) any, (Collection) any);
                    result = null;
            }
        };

        collectionService.removeAdministrators(context, collection);
        assertThat("testRemoveAdministratorsAuth 0", collection.getAdministrators(), nullValue());
    }

    /**
     * Test of removeAdministrators method, of class Collection.
     */
    @Test(expected=AuthorizeException.class)
    public void testRemoveAdministratorsNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeUtil authManager;
            {
                AuthorizeUtil.authorizeRemoveAdminGroup((Context) any, (Collection) any);
                    result = new AuthorizeException();
            }
        };

        collectionService.removeAdministrators(context, collection);
        fail("Exception expected");
    }

    /**
     * Test of getAdministrators method, of class Collection.
     */
    @Test
    public void testGetAdministrators()
    {
        assertThat("testGetAdministrators 0", collection.getAdministrators(), nullValue());
    }

    /**
     * Test of getLicense method, of class Collection.
     */
    @Test
    public void testGetLicense()
    {
        assertThat("testGetLicense 0", collectionService.getLicense(collection), notNullValue());
        assertThat("testGetLicense 1", collectionService.getLicense(collection), equalTo(licenseService.getDefaultSubmissionLicense()));
    }

    /**
     * Test of getLicenseCollection method, of class Collection.
     */
    @Test
    public void testGetLicenseCollection()
    {
        assertThat("testGetLicenseCollection 0", collectionService.getLicenseCollection(collection), notNullValue());
        assertThat("testGetLicenseCollection 1", collectionService.getLicenseCollection(collection), equalTo(""));
    }

    /**
     * Test of hasCustomLicense method, of class Collection.
     */
    @Test
    public void testHasCustomLicense()
    {
        assertFalse("testHasCustomLicense 0", collectionService.hasCustomLicense(collection));
    }

    /**
     * Test of setLicense method, of class Collection.
     */
    @Test
    public void testSetLicense()
    {
        String license = "license for test";
        collection.setLicense(license);
        assertThat("testSetLicense 0", collectionService.getLicense(collection), notNullValue());
        assertThat("testSetLicense 1", collectionService.getLicense(collection), equalTo(license));
        assertThat("testSetLicense 2", collectionService.getLicenseCollection(collection), notNullValue());
        assertThat("testSetLicense 3", collectionService.getLicenseCollection(collection), equalTo(license));
    }

    /**
     * Test of getTemplateItem method, of class Collection.
     */
    @Test
    public void testGetTemplateItem() throws Exception
    {
        assertThat("testGetTemplateItem 0", collection.getTemplateItem(), nullValue());
    }

    /**
     * Test of createTemplateItem method, of class Collection.
     */
    @Test
    public void testCreateTemplateItemAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeUtil authManager;
            {
                AuthorizeUtil.authorizeManageTemplateItem((Context) any, (Collection) any);
                    result = null;
            }
        };

        collectionService.createTemplateItem(context, collection);
        assertThat("testCreateTemplateItemAuth 0", collection.getTemplateItem(), notNullValue());
    }

    /**
     * Test of createTemplateItem method, of class Collection.
     */
    @Test(expected=AuthorizeException.class)
    public void testCreateTemplateItemNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeUtil authManager;
            {
                AuthorizeUtil.authorizeManageTemplateItem((Context) any, (Collection) any);
                    result = new AuthorizeException();
            }
        };

        collectionService.createTemplateItem(context, collection);
        fail("Exception expected");
    }

    /**
     * Test of removeTemplateItem method, of class Collection.
     */
    @Test
    public void testRemoveTemplateItemAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeUtil authManager;
            {
                AuthorizeUtil.authorizeManageTemplateItem((Context) any, (Collection) any);
                    result = null;
            }
        };

        collectionService.removeTemplateItem(context, collection);
        assertThat("testRemoveTemplateItemAuth 0", collection.getTemplateItem(), nullValue());
    }

    /**
     * Test of removeTemplateItem method, of class Collection.
     */
    @Test(expected=AuthorizeException.class)
    public void testRemoveTemplateItemNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeUtil authManager;
            {
                AuthorizeUtil.authorizeManageTemplateItem((Context) any, (Collection) any);
                    result = new AuthorizeException();
            }
        };

        collectionService.removeTemplateItem(context, collection);
        fail("Exception expected");
    }

    /**
     * Test of addItem method, of class Collection.
     */
    @Test
    public void testAddItemAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.ADD); result = null;
            }
        };
        //TODO: IMPLEMENT WHEN WORKSPACE ITEM & STUFF BECOMS AVAILABE !
        /*
        Item item = Item.create(context);
        collection.addItem(item);
        boolean added = false;
        Iterator<Item> ii = collection.getAllItems();
        while(ii.hasNext())
        {
            if(ii.next().equals(item))
            {
                added = true;
            }
        }
        assertTrue("testAddItemAuth 0",added);
        */
    }

    /**
     * Test of addItem method, of class Collection.
     */
//    @Test(expected=AuthorizeException.class)
    public void testAddItemNoAuth() throws Exception
    {
//        new NonStrictExpectations()
//        {
//            AuthorizeManager authManager;
//            {
//                AuthorizeManager.authorizeAction((Context) any, (Collection) any,
//                        Constants.ADD); result = new AuthorizeException();
//            }
//        };

        //TODO: IMPLEMENT WHEN WORKSPACE ITEM & STUFF BECOMS AVAILABE !
//        Item item = Item.create(context);
//        collection.addItem(item);
//        fail("Excep[tion expected");
    }

    /**
     * Test of removeItem method, of class Collection.
     */
    @Test
    public void testRemoveItemAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.ADD); result = null;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.REMOVE); result = null;
            }
        };

        //TODO: IMPLEMENT WHEN WORKSPACE ITEM & STUFF BECOMS AVAILABE !
//        Item item = Item.create(context);
//        collection.addItem(item);
//
//        collection.removeItem(item);
        boolean isthere = false;
//        Iterator<Item> ii = collection.getAllItems();
//        while(ii.hasNext())
//        {
//            if(ii.next().equals(item))
//            {
//                isthere = true;
//            }
//        }
//        assertFalse("testRemoveItemAuth 0",isthere);
    }

    /**
     * Test of removeItem method, of class Collection.
     */
//    @Test(expected=AuthorizeException.class)
    public void testRemoveItemNoAuth() throws Exception
    {
//        new NonStrictExpectations()
//        {
//            AuthorizeManager authManager;
//            {
//                AuthorizeManager.authorizeAction((Context) any, (Collection) any,
//                        Constants.ADD); result = null;
//                AuthorizeManager.authorizeAction((Context) any, (Collection) any,
//                        Constants.REMOVE); result = new AuthorizeException();
//            }
//        };

        //TODO: IMPLEMENT WHEN ITEM BECOMES AVAILABLE
//        Item item = Item.create(context);
//        collection.addItem(item);
//
//        collection.removeItem(item);
//        fail("Exception expected");
    }

    /**
     * Test of update method, of class Collection.
     */
    @Test
    public void testUpdateAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authorizeService;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = true;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = true;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = null;
            }
        };

        //TODO: how to check update?
        collectionService.update(context, collection);
    }

    /**
     * Test of update method, of class Collection.
     */
    @Test
    public void testUpdateAuth2() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = false;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = true;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = null;
            }
        };

        //TODO: how to check update?
        collectionService.update(context, collection);
    }

    /**
     * Test of update method, of class Collection.
     */
    @Test
    public void testUpdateAuth3() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = true;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = false;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = null;
            }
        };

        //TODO: how to check update?
        collectionService.update(context, collection);
    }

    /**
     * Test of update method, of class Collection.
     */
    @Test
    public void testUpdateAuth4() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = false;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = false;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = null;
            }
        };

        //TODO: how to check update?
        collectionService.update(context, collection);
    }

    /**
     * Test of update method, of class Collection.
     */
    @Test(expected=AuthorizeException.class)
    public void testUpdateNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = false;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = false;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = new AuthorizeException();
            }
        };

        collectionService.update(context, collection);
        fail("Exception expected");
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditBooleanAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = true;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = true;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = null;
            }
        };

        assertTrue("testCanEditBooleanAuth 0", collectionService.canEditBoolean(context, collection));
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditBooleanAuth2() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = false;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = true;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = null;
            }
        };

        assertTrue("testCanEditBooleanAuth2 0", collectionService.canEditBoolean(context, collection));
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditBooleanAuth3() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = true;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = false;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = null;
            }
        };

        assertTrue("testCanEditBooleanAuth3 0", collectionService.canEditBoolean(context, collection));
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditBooleanAuth4() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = false;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = false;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = null;
            }
        };

        assertTrue("testCanEditBooleanAuth4 0", collectionService.canEditBoolean(context, collection));
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditBooleanNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = false;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = false;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = new AuthorizeException();
            }
        };

        assertFalse("testCanEditBooleanNoAuth 0", collectionService.canEditBoolean(context, collection));
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditBooleanAuth_boolean() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = true;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = true;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = null;
            }
        };

        assertTrue("testCanEditBooleanAuth_boolean 0", collectionService.canEditBoolean(context, collection, true));
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditBooleanAuth2_boolean() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = false;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = true;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = null;
            }
        };

        assertTrue("testCanEditBooleanAuth2_boolean 0", collectionService.canEditBoolean(context, collection, true));
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditBooleanAuth3_boolean() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = true;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = false;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = null;
            }
        };

        assertTrue("testCanEditBooleanAuth3_boolean 0", collectionService.canEditBoolean(context, collection, true));
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditBooleanAuth4_boolean() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = false;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = false;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = null;
            }
        };

        assertTrue("testCanEditBooleanAuth4_boolean 0", collectionService.canEditBoolean(context, collection, true));
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditBooleanAuth5_boolean() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, false); result = true;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, false); result = true;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, false); result = null;
            }
        };

        assertTrue("testCanEditBooleanAuth5_boolean 0", collectionService.canEditBoolean(context, collection, false));
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditBooleanAuth6_boolean() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, false); result = false;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, false); result = true;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, false); result = null;
            }
        };

        assertTrue("testCanEditBooleanAuth6_boolean 0", collectionService.canEditBoolean(context, collection, false));
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditBooleanAuth7_boolean() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, false); result = true;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, false); result = false;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, false); result = null;
            }
        };

        assertTrue("testCanEditBooleanAuth7_boolean 0", collectionService.canEditBoolean(context, collection, false));
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditBooleanAuth8_boolean() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, false); result = false;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, false); result = false;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, false); result = null;
            }
        };

        assertTrue("testCanEditBooleanAuth8_boolean 0", collectionService.canEditBoolean(context, collection, false));
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditBooleanNoAuth_boolean() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = false;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = false;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = new AuthorizeException();
            }
        };

        assertFalse("testCanEditBooleanNoAuth_boolean 0", collectionService.canEditBoolean(context, collection, true));
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditBooleanNoAuth2_boolean() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, false); result = false;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, false); result = false;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, false); result = new AuthorizeException();
            }
        };

        assertFalse("testCanEditBooleanNoAuth_boolean 0", collectionService.canEditBoolean(context, collection, false));
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditAuth_0args() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = true;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = true;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = null;
            }
        };

        //TODO: how to check??
        collectionService.canEdit(context, collection);
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditAuth2_0args() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = false;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = true;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = null;
            }
        };

        //TODO: how to check??
        collectionService.canEdit(context, collection);
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditAuth3_0args() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = true;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = false;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = null;
            }
        };

        //TODO: how to check??
        collectionService.canEdit(context, collection);
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditAuth4_0args() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = false;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = false;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = null;
            }
        };

        //TODO: how to check??
        collectionService.canEdit(context, collection);
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test(expected=AuthorizeException.class)
    public void testCanEditNoAuth_0args() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = false;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = false;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = new AuthorizeException();
            }
        };

        collectionService.canEdit(context, collection);
        fail("Exception expected");
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditAuth_boolean() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = true;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = true;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = null;
            }
        };

        //TOO: how to check?
        collectionService.canEdit(context,  collection, true);
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditAuth2_boolean() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = false;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = true;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = null;
            }
        };

        //TOO: how to check?
        collectionService.canEdit(context,  collection, true);
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditAuth3_boolean() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = true;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = false;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = null;
            }
        };

        //TOO: how to check?
        collectionService.canEdit(context,  collection, true);
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditAuth4_boolean() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = false;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = false;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = null;
            }
        };

        //TOO: how to check?
        collectionService.canEdit(context,  collection, true);
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditAuth5_boolean() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, false); result = true;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, false); result = true;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, false); result = null;
            }
        };

        //TOO: how to check?
        collectionService.canEdit(context,  collection, false);
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditAuth6_boolean() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, false); result = false;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, false); result = true;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, false); result = null;
            }
        };

        //TOO: how to check?
        collectionService.canEdit(context,  collection, false);
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditAuth7_boolean() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, false); result = true;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, false); result = false;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, false); result = null;
            }
        };

        //TOO: how to check?
        collectionService.canEdit(context,  collection, false);
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditAuth8_boolean() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, false); result = false;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, false); result = false;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, false); result = null;
            }
        };

        //TOO: how to check?
        collectionService.canEdit(context,  collection, false);
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test(expected=AuthorizeException.class)
    public void testCanEditNoAuth_boolean() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, false); result = false;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, false); result = false;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, false); result = new AuthorizeException();
            }
        };

        //TOO: how to check?
        collectionService.canEdit(context,  collection, false);
        fail("Exception expected");
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test(expected=AuthorizeException.class)
    public void testCanEditNoAuth2_boolean() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = false;
                authorizeService.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = false;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = new AuthorizeException();
            }
        };

        //TOO: how to check?
        collectionService.canEdit(context,  collection, true);
        fail("Exception expected");
    }

    /**
     * Test of delete method, of class Collection.
     */
    @Test
    public void testDeleteAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeUtil authUtil;
            AuthorizeService authManager;
            {
                AuthorizeUtil.authorizeManageTemplateItem((Context) any, (Collection) any);
                 result = null;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, anyBoolean); result = null;
            }
        };

        UUID id = collection.getID();
        communityService.removeCollection(context, owningCommunity, collection);
        assertThat("testDelete 0", collectionService.find(context, id),nullValue());
    }

    /**
     * Test of delete method, of class Collection.
     */
    @Test(expected=AuthorizeException.class)
    public void testDeleteNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeUtil authUtil;
            AuthorizeService authManager;
            {
                AuthorizeUtil.authorizeManageTemplateItem((Context) any, (Collection) any);
                 result = new AuthorizeException();
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, anyBoolean); result = null;
            }
        };

        communityService.removeCollection(context, owningCommunity, collection);
        fail("Exception expected");
    }

     /**
     * Test of delete method, of class Collection.
     */
    @Test(expected=AuthorizeException.class)
    public void testDeleteNoAuth2() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeUtil authUtil;
            AuthorizeService authManager;
            {
                AuthorizeUtil.authorizeManageTemplateItem((Context) any, (Collection) any);
                 result = null;
                authorizeService.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, anyBoolean); result = new AuthorizeException();
            }
        };

        UUID id = collection.getID();
        communityService.removeCollection(context, owningCommunity, collection);
        fail("Exception expected");
    }

    /**
     * Test of getCommunities method, of class Collection.
     */
    @Test
    public void testGetCommunities() throws Exception
    {
        assertThat("testGetCommunities 0", collection.getCommunities(), notNullValue());
        assertTrue("testGetCommunities 1", CollectionUtils.isNotEmpty(collection.getCommunities()));
        assertEquals("testGetCommunities 2", collection.getCommunities().iterator().next(), owningCommunity);
    }

    /**
     * Test of equals method, of class Collection.
     */
    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void testEquals() throws SQLException, AuthorizeException
    {
        new NonStrictExpectations()
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeActionBoolean((Context) any, (Collection) any,
                        Constants.ADD); result = true;
            }
        };

        assertFalse("testEquals 0", collection.equals(null));
        assertFalse("testEquals 1", collection.equals(collectionService.create(context, owningCommunity)));
        assertTrue("testEquals 2", collection.equals(collection));
    }

    /**
     * Test of getType method, of class Collection.
     */
    @Test
    @Override
    public void testGetType()
    {
        assertThat("testGetType 0", collection.getType(), equalTo(Constants.COLLECTION));
    }

    /**
     * Test of findAuthorized method, of class Collection.
     */
    @Test
    public void testFindAuthorized() throws Exception
    {
        context.turnOffAuthorisationSystem();
        Community com = communityService.create(context, null);
        context.restoreAuthSystemState();

        List<Collection> found = collectionService.findAuthorized(context, com, Constants.WRITE);
        assertThat("testFindAuthorized 0",found,notNullValue());
        assertTrue("testFindAuthorized 1",found.size() == 0);

        found = collectionService.findAuthorized(context, null, Constants.WRITE);
        assertThat("testFindAuthorized 2",found,notNullValue());
        assertTrue("testFindAuthorized 3",found.size() == 0);

        found = collectionService.findAuthorized(context, com, Constants.ADD);
        assertThat("testFindAuthorized 3",found,notNullValue());
        assertTrue("testFindAuthorized 4",found.size() == 0);

        found = collectionService.findAuthorized(context, null, Constants.ADD);
        assertThat("testFindAuthorized 5",found,notNullValue());
        assertTrue("testFindAuthorized 6",found.size() == 0);

        found = collectionService.findAuthorized(context, com, Constants.READ);
        assertThat("testFindAuthorized 7",found,notNullValue());
        assertTrue("testFindAuthorized 8",found.size() == 0);

        found = collectionService.findAuthorized(context, null, Constants.READ);
        assertThat("testFindAuthorized 9",found,notNullValue());
        assertTrue("testFindAuthorized 10",found.size() >= 1);
    }

    /**
     * Test of countItems method, of class Collection.
     */
    @Test
    public void testCountItems() throws Exception
    {
        //0 by default
        //TODO: IMPLEMENT WHEN COUNT ITEMS BECOMES AVAILABLE
//        assertTrue("testCountItems 0", collection.countItems() == 0);
    }

    /**
     * Test of getAdminObject method, of class Collection.
     */
    @Test
    @Override
    public void testGetAdminObject() throws SQLException
    {
        //default community has no admin object
        assertThat("testGetAdminObject 0", (Collection) collectionService.getAdminObject(context, collection, Constants.REMOVE), equalTo(collection));
        assertThat("testGetAdminObject 1", (Collection) collectionService.getAdminObject(context, collection, Constants.ADD), equalTo(collection));
        assertThat("testGetAdminObject 2", (Community) collectionService.getAdminObject(context, collection, Constants.DELETE), equalTo(owningCommunity));
        assertThat("testGetAdminObject 3", (Collection) collectionService.getAdminObject(context, collection, Constants.ADMIN), equalTo(collection));
    }

    /**
     * Test of getParentObject method, of class Collection.
     */
    @Test
    @Override
    public void testGetParentObject() throws SQLException
    {
        try
        {
            context.turnOffAuthorisationSystem();
            Community parent = communityService.create(context, null);
            communityService.addCollection(context, parent, collection);
            context.commit();
            context.restoreAuthSystemState();
            assertThat("testGetParentObject 1", collectionService.getParentObject(context, collection), notNullValue());
            assertThat("testGetParentObject 2", (Community) collectionService.getParentObject(context, collection), equalTo(parent));
        }
        catch(AuthorizeException ex)
        {
            fail("Authorize exception catched");
        }
    }

}