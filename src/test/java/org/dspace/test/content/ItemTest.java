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
import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.lang.time.DateUtils;
import org.dspace.authorize.AuthorizeException;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.*;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.junit.*;
import static org.junit.Assert.* ;
import static org.hamcrest.CoreMatchers.*;
import mockit.*;
import org.dspace.app.util.AuthorizeUtil;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.core.Constants;

/**
 * Unit Tests for class Item
 * @author pvillega
 */
public class ItemTest  extends AbstractDSpaceObjectTest
{

    /** log4j category */
    private static final Logger log = Logger.getLogger(ItemTest.class);

    /**
     * Item instance for the tests
     */
    private Item it;
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
            this.owningCommunity = communityManager.create(null, context);
            this.collection = collectionManager.create(context, owningCommunity);

            this.it = createItem();

            itemManager.update(context, it);
            this.dspaceObject = it;
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
        } catch (IllegalAccessException ex) {
            log.error("IllegalAccess Error in init", ex);
            fail("IllegalAccess Error in init: " + ex.getMessage());
        } catch (IOException ex) {
            log.error("IO Error in init", ex);
            fail("IO Error in init: " + ex.getMessage());
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
    public void destroy() throws Exception
    {
        context.turnOffAuthorisationSystem();
        if(itemManager.find(context,  it.getID()) != null)
        {
            itemManager.delete(context, it);
        }
        context.restoreAuthSystemState();
        super.destroy();
    }


    /**
     * Test of find method, of class Item.
     */
    @Test
    public void testItemFind() throws Exception
    {
        int id = it.getID();
        Item found =  itemManager.find(context, id);
        assertThat("testItemFind 0", found, notNullValue());
        assertThat("testItemFind 1", found.getID(), equalTo(id));
        assertThat("testItemFind 2", found.getName(), nullValue());
    }

    /**
     * Test of findAll method, of class Item.
     */
    @Test
    public void testFindAll() throws Exception
    {
        Iterator<Item> all = itemManager.findAll(context);
        assertThat("testFindAll 0", all, notNullValue());

        boolean added = false;
        while(all.hasNext())
        {
            Item tmp = all.next();
            if(tmp.equals(it))
            {
                added = true;
            }
        }
        assertTrue("testFindAll 1", added);
    }

    /**
     * Test of findBySubmitter method, of class Item.
     */
    @Test
    public void testFindBySubmitter() throws Exception
    {
        Iterator<Item> all = itemManager.findBySubmitter(context, context.getCurrentUser());
        assertThat("testFindBySubmitter 0", all, notNullValue());

        boolean added = false;
        while(all.hasNext())
        {
            Item tmp = all.next();
            if(tmp.equals(it))
            {
                added = true;
            }
        }
        assertTrue("testFindBySubmitter 1",added);

        context.turnOffAuthorisationSystem();
        all = itemManager.findBySubmitter(context, ePersonManager.create(context));
        context.restoreAuthSystemState();

        assertThat("testFindBySubmitter 2", all, notNullValue());
        assertFalse("testFindBySubmitter 3", all.hasNext());
    }

    /**
     * Test of getID method, of class Item.
     */
    @Test
    public void testGetID()
    {
        assertTrue("testGetID 0", it.getID() >= 1);
    }

    /**
     * Test of getHandle method, of class Item.
     */
    @Test
    public void testGetHandle() throws Exception
    {
        //default instance has a random handle
        assertThat("testGetHandle 0", it.getHandle(context), nullValue());
    }

    /**
     * Test of isArchived method, of class Item.
     */
    @Test
    public void testIsArchived() throws Exception
    {
        //we are archiving items in the test by default so other tests run
        assertTrue("testIsArchived 0", it.isArchived());

        //false by default
        context.turnOffAuthorisationSystem();
        Item tmp = createItem();
        context.restoreAuthSystemState();
        assertTrue("testIsArchived 1", tmp.isArchived());
    }

    /**
     * Test of isWithdrawn method, of class Item.
     */
    @Test
    public void testIsWithdrawn()
    {
        assertFalse("testIsWithdrawn 0", it.isWithdrawn());
    }

    /**
     * Test of getLastModified method, of class Item.
     */
    @Test
    public void testGetLastModified()
    {
        assertThat("testGetLastModified 0", it.getLastModified(), notNullValue());
        assertTrue("testGetLastModified 1", DateUtils.isSameDay(it.getLastModified(), new Date()));
    }

    /**
     * Test of setArchived method, of class Item.
     */
    @Test
    public void testSetArchived()
    {
        it.setInArchive(true);
        assertTrue("testSetArchived 0", it.isArchived());
    }

    /**
     * Test of setOwningCollection method, of class Item.
     */
    @Test
    public void testSetOwningCollection() throws SQLException, AuthorizeException
    {
        context.turnOffAuthorisationSystem();
        Collection c = createCollection();
        context.restoreAuthSystemState();

        it.setOwningCollection(c);
        assertThat("testSetOwningCollection 0", it.getOwningCollection(), notNullValue());
        assertThat("testSetOwningCollection 1", it.getOwningCollection(), equalTo(c));
    }

    /**
     * Test of getOwningCollection method, of class Item.
     */
    @Test
    public void testGetOwningCollection() throws Exception
    {
        assertThat("testGetOwningCollection 0", it.getOwningCollection(), notNullValue());
    }

    /**
     * Test of getMetadata method, of class Item.
     */
    @Test
    public void testGetMetadata_4args()
    {
        String schema = "dc";
        String element = "contributor";
        String qualifier = "author";
        String lang = Item.ANY;
        List<MetadataValue> dc = itemManager.getMetadata(it, schema, element, qualifier, lang);
        assertThat("testGetMetadata_4args 0", dc, notNullValue());
        assertTrue("testGetMetadata_4args 1", dc.size() == 0);
    }

    /**
     * Test of getMetadata method, of class Item.
     */
    @Test
    public void testGetMetadata_String()
    {
        String mdString = "dc.contributor.author";
        List<MetadataValue> dc = itemManager.getMetadata(it, mdString);
        assertThat("testGetMetadata_String 0",dc,notNullValue());
        assertTrue("testGetMetadata_String 1",dc.size() == 0);

        mdString = "dc.contributor.*";
        dc = itemManager.getMetadata(it, mdString);
        assertThat("testGetMetadata_String 2",dc,notNullValue());
        assertTrue("testGetMetadata_String 3",dc.size() == 0);

        mdString = "dc.contributor";
        dc = itemManager.getMetadata(it, mdString);
        assertThat("testGetMetadata_String 4",dc,notNullValue());
        assertTrue("testGetMetadata_String 5",dc.size() == 0);
    }

    /**
     * A test for DS-806: Item.match() incorrect logic for schema testing
     */
    @Test
    public void testDS806() throws Exception
    {
        //Create our "test" metadata field
        context.turnOffAuthorisationSystem();
        MetadataSchema metadataSchema = metadataSchemaManager.create(context, "test", "test");
        MetadataField metadataField = metadataFieldManager.create(context, metadataSchema, "type", null, null);
        context.restoreAuthSystemState();

        // Set the item to have two pieces of metadata for dc.type and dc2.type
        String dcType = "DC-TYPE";
        String testType = "TEST-TYPE";
        itemManager.addMetadata(context, it, "dc", "type", null, null, dcType);
        itemManager.addMetadata(context, it, "test", "type", null, null, testType);

        // Check that only one is returned when we ask for all dc.type values
        List<MetadataValue> values = itemManager.getMetadata(it, "dc", "type", null, null);
        assertTrue("Return results", values.size() == 1);

        //Delete the field & schema
        context.turnOffAuthorisationSystem();
        metadataFieldManager.delete(context, metadataField);
        metadataSchemaManager.delete(context, metadataSchema);
        context.restoreAuthSystemState();
    }

    /**
     * Test of addMetadata method, of class Item.
     */
    @Test
    public void testAddMetadata_5args_1() throws Exception
    {
        String schema = "dc";
        String element = "contributor";
        String qualifier = "author";
        String lang = Item.ANY;
        String[] values = {"value0","value1"};
        itemManager.addMetadata(context, it, schema, element, qualifier, lang, values);

        List<MetadataValue> dc = itemManager.getMetadata(it, schema, element, qualifier, lang);
        assertThat("testAddMetadata_5args_1 0",dc,notNullValue());
        assertTrue("testAddMetadata_5args_1 1",dc.size() == 2);
        assertThat("testAddMetadata_5args_1 2",dc.get(0).getMetadataField().getMetadataSchema().getName(),equalTo(schema));
        assertThat("testAddMetadata_5args_1 3",dc.get(0).getMetadataField().getElement(),equalTo(element));
        assertThat("testAddMetadata_5args_1 4",dc.get(0).getMetadataField().getQualifier(),equalTo(qualifier));
        assertThat("testAddMetadata_5args_1 5",dc.get(0).getLanguage(),equalTo(lang));
        assertThat("testAddMetadata_5args_1 6",dc.get(0).getValue(),equalTo(values[0]));
        assertThat("testAddMetadata_5args_1 7",dc.get(1).getMetadataField().getMetadataSchema().getName(),equalTo(schema));
        assertThat("testAddMetadata_5args_1 8",dc.get(1).getMetadataField().getElement(),equalTo(element));
        assertThat("testAddMetadata_5args_1 9",dc.get(1).getMetadataField().getQualifier(),equalTo(qualifier));
        assertThat("testAddMetadata_5args_1 10",dc.get(1).getLanguage(),equalTo(lang));
        assertThat("testAddMetadata_5args_1 11",dc.get(1).getValue(),equalTo(values[1]));
    }

    /**
     * Test of addMetadata method, of class Item.
     */
    @Test
    public void testAddMetadata_7args_1_authority() throws Exception
    {
        //we have enabled an authority control in dspace-test.cfg to run this test
        //as MetadataAuthorityManager can't be mocked properly

        String schema = "dc";
        String element = "language";
        String qualifier = "iso";
        String lang = Item.ANY;
        String[] values = {"en_US","en"};
        String[] authorities = {"accepted","uncertain"};
        int[] confidences = {0,0};
        itemManager.addMetadata(context, it, schema, element, qualifier, lang, values, authorities, confidences);

        List<MetadataValue> dc = itemManager.getMetadata(it, schema, element, qualifier, lang);
        assertThat("testAddMetadata_7args_1 0",dc,notNullValue());
        assertTrue("testAddMetadata_7args_1 1",dc.size() == 2);
        assertThat("testAddMetadata_7args_1 2",dc.get(0).getMetadataField().getMetadataSchema().getName(),equalTo(schema));
        assertThat("testAddMetadata_7args_1 3",dc.get(0).getMetadataField().getElement(),equalTo(element));
        assertThat("testAddMetadata_7args_1 4",dc.get(0).getMetadataField().getQualifier(),equalTo(qualifier));
        assertThat("testAddMetadata_7args_1 5",dc.get(0).getLanguage(),equalTo(lang));
        assertThat("testAddMetadata_7args_1 6",dc.get(0).getValue(),equalTo(values[0]));
        assertThat("testAddMetadata_7args_1 7",dc.get(0).getAuthority(),equalTo(authorities[0]));
        assertThat("testAddMetadata_7args_1 8",dc.get(0).getConfidence(),equalTo(confidences[0]));
        assertThat("testAddMetadata_7args_1 9",dc.get(1).getMetadataField().getMetadataSchema().getName(),equalTo(schema));
        assertThat("testAddMetadata_7args_1 10",dc.get(1).getMetadataField().getElement(),equalTo(element));
        assertThat("testAddMetadata_7args_1 11",dc.get(1).getMetadataField().getQualifier(),equalTo(qualifier));
        assertThat("testAddMetadata_7args_1 12",dc.get(1).getLanguage(),equalTo(lang));
        assertThat("testAddMetadata_7args_1 13",dc.get(1).getValue(),equalTo(values[1]));
        assertThat("testAddMetadata_7args_1 14",dc.get(1).getAuthority(),equalTo(authorities[1]));
        assertThat("testAddMetadata_7args_1 15",dc.get(1).getConfidence(),equalTo(confidences[1]));
    }

    /**
     * Test of addMetadata method, of class Item.
     */
    @Test
    public void testAddMetadata_7args_1_noauthority() throws Exception
    {
        //by default has no authority

        String schema = "dc";
        String element = "contributor";
        String qualifier = "author";
        String lang = Item.ANY;
        String[] values = {"value0","value1"};
        String[] authorities = {"auth0","auth2"};
        int[] confidences = {0,0};
        itemManager.addMetadata(context, it, schema, element, qualifier, lang, values, authorities, confidences);

        List<MetadataValue> dc = itemManager.getMetadata(it, schema, element, qualifier, lang);
        assertThat("testAddMetadata_7args_1 0",dc,notNullValue());
        assertTrue("testAddMetadata_7args_1 1",dc.size() == 2);
        assertThat("testAddMetadata_7args_1 2",dc.get(0).getMetadataField().getMetadataSchema().getName(),equalTo(schema));
        assertThat("testAddMetadata_7args_1 3",dc.get(0).getMetadataField().getElement(),equalTo(element));
        assertThat("testAddMetadata_7args_1 4",dc.get(0).getMetadataField().getQualifier(),equalTo(qualifier));
        assertThat("testAddMetadata_7args_1 5",dc.get(0).getLanguage(),equalTo(lang));
        assertThat("testAddMetadata_7args_1 6",dc.get(0).getValue(),equalTo(values[0]));
        assertThat("testAddMetadata_7args_1 7",dc.get(0).getAuthority(),nullValue());
        assertThat("testAddMetadata_7args_1 8",dc.get(0).getConfidence(),equalTo(-1));
        assertThat("testAddMetadata_7args_1 9",dc.get(1).getMetadataField().getMetadataSchema().getName(),equalTo(schema));
        assertThat("testAddMetadata_7args_1 10",dc.get(1).getMetadataField().getElement(),equalTo(element));
        assertThat("testAddMetadata_7args_1 11",dc.get(1).getMetadataField().getQualifier(),equalTo(qualifier));
        assertThat("testAddMetadata_7args_1 12",dc.get(1).getLanguage(),equalTo(lang));
        assertThat("testAddMetadata_7args_1 13",dc.get(1).getValue(),equalTo(values[1]));
        assertThat("testAddMetadata_7args_1 14",dc.get(1).getAuthority(),nullValue());
        assertThat("testAddMetadata_7args_1 15",dc.get(1).getConfidence(),equalTo(-1));
    }

    /**
     * Test of addMetadata method, of class Item.
     */
    @Test
    public void testAddMetadata_5args_2() throws Exception
    {
        String schema = "dc";
        String element = "contributor";
        String qualifier = "author";
        String lang = Item.ANY;
        String[] values = {"value0","value1"};
        itemManager.addMetadata(context, it, schema, element, qualifier, lang, values);

        List<MetadataValue> dc = itemManager.getMetadata(it, schema, element, qualifier, lang);
        assertThat("testAddMetadata_5args_2 0",dc,notNullValue());
        assertTrue("testAddMetadata_5args_2 1",dc.size() == 2);
        assertThat("testAddMetadata_5args_2 2",dc.get(0).getMetadataField().getMetadataSchema().getName(),equalTo(schema));
        assertThat("testAddMetadata_5args_2 3",dc.get(0).getMetadataField().getElement(),equalTo(element));
        assertThat("testAddMetadata_5args_2 4",dc.get(0).getMetadataField().getQualifier(),equalTo(qualifier));
        assertThat("testAddMetadata_5args_2 5",dc.get(0).getLanguage(),equalTo(lang));
        assertThat("testAddMetadata_5args_2 6",dc.get(0).getValue(),equalTo(values[0]));
        assertThat("testAddMetadata_5args_2 7",dc.get(1).getMetadataField().getMetadataSchema().getName(),equalTo(schema));
        assertThat("testAddMetadata_5args_2 8",dc.get(1).getMetadataField().getElement(),equalTo(element));
        assertThat("testAddMetadata_5args_2 9",dc.get(1).getMetadataField().getQualifier(),equalTo(qualifier));
        assertThat("testAddMetadata_5args_2 10",dc.get(1).getLanguage(),equalTo(lang));
        assertThat("testAddMetadata_5args_2 11",dc.get(1).getValue(),equalTo(values[1]));
    }

    /**
     * Test of addMetadata method, of class Item.
     */
    @Test
    public void testAddMetadata_7args_2_authority() throws Exception
    {
        //we have enabled an authority control in dspace-test.cfg to run this test
        //as MetadataAuthorityManager can't be mocked properly

        String schema = "dc";
        String element = "language";
        String qualifier = "iso";
        String lang = Item.ANY;
        String values = "en";
        String authorities = "accepted";
        int confidences = 0;
        itemManager.addMetadata(context, it, schema, element, qualifier, lang, values, authorities, confidences);

        List<MetadataValue> dc = itemManager.getMetadata(it, schema, element, qualifier, lang);
        assertThat("testAddMetadata_7args_2 0",dc,notNullValue());
        assertTrue("testAddMetadata_7args_2 1",dc.size() == 1);
        assertThat("testAddMetadata_7args_2 2",dc.get(0).getMetadataField().getMetadataSchema().getName(),equalTo(schema));
        assertThat("testAddMetadata_7args_2 3",dc.get(0).getMetadataField().getElement(),equalTo(element));
        assertThat("testAddMetadata_7args_2 4",dc.get(0).getMetadataField().getQualifier(),equalTo(qualifier));
        assertThat("testAddMetadata_7args_2 5",dc.get(0).getLanguage(),equalTo(lang));
        assertThat("testAddMetadata_7args_2 6",dc.get(0).getValue(),equalTo(values));
        assertThat("testAddMetadata_7args_2 7",dc.get(0).getAuthority(),equalTo(authorities));
        assertThat("testAddMetadata_7args_2 8", dc.get(0).getConfidence(), equalTo(confidences));
    }

    /**
     * Test of addMetadata method, of class Item.
     */
    @Test
    public void testAddMetadata_7args_2_noauthority() throws Exception
    {
        //by default has no authority

        String schema = "dc";
        String element = "contributor";
        String qualifier = "author";
        String lang = Item.ANY;
        String values = "value0";
        String authorities = "auth0";
        int confidences = 0;
        itemManager.addMetadata(context, it, schema, element, qualifier, lang, values, authorities, confidences);

        List<MetadataValue> dc = itemManager.getMetadata(it, schema, element, qualifier, lang);
        assertThat("testAddMetadata_7args_2 0",dc,notNullValue());
        assertTrue("testAddMetadata_7args_2 1",dc.size() == 1);
        assertThat("testAddMetadata_7args_2 2",dc.get(0).getMetadataField().getMetadataSchema().getName(),equalTo(schema));
        assertThat("testAddMetadata_7args_2 3",dc.get(0).getMetadataField().getElement(),equalTo(element));
        assertThat("testAddMetadata_7args_2 4",dc.get(0).getMetadataField().getQualifier(),equalTo(qualifier));
        assertThat("testAddMetadata_7args_2 5",dc.get(0).getLanguage(),equalTo(lang));
        assertThat("testAddMetadata_7args_2 6",dc.get(0).getValue(),equalTo(values));
        assertThat("testAddMetadata_7args_2 7",dc.get(0).getAuthority(),nullValue());
        assertThat("testAddMetadata_7args_2 8",dc.get(0).getConfidence(),equalTo(-1));
    }

    /**
     * Test of clearMetadata method, of class Item.
     */
    @Test
    public void testClearMetadata() throws Exception
    {
        String schema = "dc";
        String element = "contributor";
        String qualifier = "author";
        String lang = Item.ANY;
        String values = "value0";
        itemManager.addMetadata(context, it, schema, element, qualifier, lang, values);

        itemManager.clearMetadata(context, it, schema, element, qualifier, lang);

        List<MetadataValue> dc = itemManager.getMetadata(it, schema, element, qualifier, lang);
        assertThat("testClearMetadata 0",dc,notNullValue());
        assertTrue("testClearMetadata 1", dc.size() == 0);
    }

    /**
     * Test of getSubmitter method, of class Item.
     */
    @Test
    public void testGetSubmitter() throws Exception
    {
        assertThat("testGetSubmitter 0", it.getSubmitter(), notNullValue());

        //null by default
        context.turnOffAuthorisationSystem();
        Item tmp = createItem();
        context.restoreAuthSystemState();
        assertEquals("testGetSubmitter 1", tmp.getSubmitter(), context.getCurrentUser());
    }

    /**
     * Test of setSubmitter method, of class Item.
     */
    @Test
    public void testSetSubmitter() throws SQLException, AuthorizeException
    {
        context.turnOffAuthorisationSystem();
        EPerson sub = ePersonManager.create(context);
        context.restoreAuthSystemState();

        it.setSubmitter(sub);

        assertThat("testSetSubmitter 0", it.getSubmitter(), notNullValue());
        assertThat("testSetSubmitter 1", it.getSubmitter().getID(), equalTo(sub.getID()));
    }

    /**
     * Test of getCollections method, of class Item.
     */
    @Test
    public void testGetCollections() throws Exception
    {
        assertThat("testGetCollections 0", it.getCollections(), notNullValue());
        assertTrue("testGetCollections 1", it.getCollections().size() == 1);
    }

    /**
     * Test of getCommunities method, of class Item.
     */
    @Test
    public void testGetCommunities() throws Exception
    {
        assertThat("testGetCommunities 0", itemManager.getCommunities(it), notNullValue());
        assertTrue("testGetCommunities 1", itemManager.getCommunities(it).size() == 1);
    }

    /**
     * Test of getBundles method, of class Item.
     */
    @Test
    public void testGetBundles_0args() throws Exception
    {
        assertThat("testGetBundles_0args 0", it.getBundles(), notNullValue());
        assertTrue("testGetBundles_0args 1", it.getBundles().size() == 0);
    }

    /**
     * Test of getBundles method, of class Item.
     */
    @Test
    public void testGetBundles_String() throws Exception
    {
        String name = "name";
        assertThat("testGetBundles_String 0", itemManager.getBundles(it, name), notNullValue());
        assertTrue("testGetBundles_String 1", itemManager.getBundles(it, name).size() == 0);
    }

    /**
     * Test of createBundle method, of class Item.
     */
    @Test
    public void testCreateBundleAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.ADD, true); result = null;
            }
        };

        String name = "bundle";
        Bundle created = itemManager.createBundle(context, it, name);
        assertThat("testCreateBundleAuth 0",created, notNullValue());
        assertThat("testCreateBundleAuth 1",created.getName(), equalTo(name));
        assertThat("testCreateBundleAuth 2", itemManager.getBundles(it, name), notNullValue());
        assertTrue("testCreateBundleAuth 3", itemManager.getBundles(it, name).size() == 1);
    }

    /**
     * Test of createBundle method, of class Item.
     */
    @Test(expected=SQLException.class)
    public void testCreateBundleNoName() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.ADD, true); result = null;
            }
        };

        String name = "";
        Bundle created = itemManager.createBundle(context, it, name);
        fail("Exception expected");
    }

    /**
     * Test of createBundle method, of class Item.
     */
    @Test(expected=SQLException.class)
    public void testCreateBundleNoName2() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.ADD, true); result = null;
            }
        };

        String name = null;
        Bundle created = itemManager.createBundle(context, it, name);
        fail("Exception expected");
    }


    /**
     * Test of createBundle method, of class Item.
     */
    @Test(expected=AuthorizeException.class)
    public void testCreateBundleNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.ADD); result = new AuthorizeException();
            }
        };

        String name = "bundle";
        Bundle created = itemManager.createBundle(context, it, name);
        fail("Exception expected");
    }

    /**
     * Test of addBundle method, of class Item.
     */
    @Test
    public void testAddBundleAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.ADD, true); result = null;
            }
        };

        String name = "bundle";
        Bundle created = itemManager.createBundle(context, it, name);
        created.setName(name);

        assertThat("testAddBundleAuth 0", itemManager.getBundles(it, name), notNullValue());
        assertTrue("testAddBundleAuth 1", itemManager.getBundles(it, name).size() == 1);
        assertThat("testAddBundleAuth 2", itemManager.getBundles(it, name).get(0), equalTo(created));
    }

    /**
     * Test of addBundle method, of class Item.
     */
    @Test(expected=AuthorizeException.class)
    public void testAddBundleNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.ADD); result = new AuthorizeException();
            }
        };

        String name = "bundle";
        Bundle created = itemManager.createBundle(context, it, name);
        created.setName(name);

        itemManager.addBundle(context, it, created);
        fail("Exception expected");
    }

    /**
     * Test of removeBundle method, of class Item.
     */
    @Test
    public void testRemoveBundleAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.ADD, true); result = null;
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.REMOVE, true); result = null;
            }
        };

        String name = "bundle";
        Bundle created = itemManager.createBundle(context, it, name);
        created.setName(name);
        itemManager.addBundle(context, it, created);

        itemManager.removeBundle(context, it, created);
        assertThat("testRemoveBundleAuth 0", itemManager.getBundles(it, name), notNullValue());
        assertTrue("testRemoveBundleAuth 1", itemManager.getBundles(it, name).size() == 0);
    }

    /**
     * Test of removeBundle method, of class Item.
     */
    @Test(expected=AuthorizeException.class)
    public void testRemoveBundleNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.ADD); result = null;
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.REMOVE); result = new AuthorizeException();
            }
        };

        String name = "bundle";
        Bundle created = itemManager.createBundle(context, it, name);
        created.setName(name);

        itemManager.removeBundle(context, it, created);
        fail("Exception expected");
    }

    /**
     * Test of createSingleBitstream method, of class Item.
     */
    @Test
    public void ritestCreateSingleBitstream_InputStream_StringAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.ADD, true); result = null;
            }
        };

        String name = "new bundle";
        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream result = itemManager.createSingleBitstream(context, it, new FileInputStream(f), name);
        assertThat("testCreateSingleBitstream_InputStream_StringAuth 0", result, notNullValue());
    }

    /**
     * Test of createSingleBitstream method, of class Item.
     */
    @Test(expected=AuthorizeException.class)
    public void testCreateSingleBitstream_InputStream_StringNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.ADD); result = new AuthorizeException();
            }
        };

        String name = "new bundle";
        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream result = itemManager.createSingleBitstream(context, it, new FileInputStream(f), name);
        fail("Exception expected");
    }

    /**
     * Test of createSingleBitstream method, of class Item.
     */
    @Test
    public void testCreateSingleBitstream_InputStreamAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.ADD, true); result = null;
            }
        };

        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream result = itemManager.createSingleBitstream(context, it, new FileInputStream(f));
        assertThat("testCreateSingleBitstream_InputStreamAuth 0", result, notNullValue());
    }

    /**
     * Test of createSingleBitstream method, of class Item.
     */
    @Test(expected=AuthorizeException.class)
    public void testCreateSingleBitstream_InputStreamNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.ADD); result = new AuthorizeException();
            }
        };

        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream result = itemManager.createSingleBitstream(context, it, new FileInputStream(f));
        fail("Expected exception");
    }

    /**
     * Test of getNonInternalBitstreams method, of class Item.
     */
    @Test
    public void testGetNonInternalBitstreams() throws Exception
    {
        assertThat("testGetNonInternalBitstreams 0", itemManager.getNonInternalBitstreams(it), notNullValue());
        assertTrue("testGetNonInternalBitstreams 1", itemManager.getNonInternalBitstreams(it).size() == 0);
    }

    /**
     * Test of removeDSpaceLicense method, of class Item.
     */
    @Test
    public void testRemoveDSpaceLicenseAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.ADD, true); result = null;
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.REMOVE, true); result = null;
            }
        };

        String name = "LICENSE";
        Bundle created = itemManager.createBundle(context, it, name);
        created.setName(name);

        itemManager.removeDSpaceLicense(context, it);
        assertThat("testRemoveDSpaceLicenseAuth 0", itemManager.getBundles(it, name), notNullValue());
        assertTrue("testRemoveDSpaceLicenseAuth 1", itemManager.getBundles(it, name).size() == 0);
    }

    /**
     * Test of removeDSpaceLicense method, of class Item.
     */
    @Test(expected=AuthorizeException.class)
    public void testRemoveDSpaceLicenseNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.ADD); result = null;
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.REMOVE); result = new AuthorizeException();
            }
        };

        String name = "LICENSE";
        Bundle created = itemManager.createBundle(context, it, name);
        created.setName(name);

        itemManager.removeDSpaceLicense(context, it);
        fail("Exception expected");
    }

    /**
     * Test of removeLicenses method, of class Item.
     */
    @Test
    public void testRemoveLicensesAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.ADD); result = null;
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.REMOVE); result = null;
            }
        };

        String name = "LICENSE";
        Bundle created = itemManager.createBundle(context, it, name);
        created.setName(name);

        String bsname = "License";
        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream result = itemManager.createSingleBitstream(context, it, new FileInputStream(f), bsname);
        bitstreamManager.setFormat(context, result, bitstreamFormatManager.findByShortDescription(context, bsname));
        bundleManager.addBitstream(context, created, result);


        itemManager.removeLicenses(context, it);
        assertThat("testRemoveLicensesAuth 0", itemManager.getBundles(it, name), notNullValue());
        assertTrue("testRemoveLicensesAuth 1", itemManager.getBundles(it, name).size() == 0);
    }

    /**
     * Test of removeLicenses method, of class Item.
     */
    @Test(expected=AuthorizeException.class)
    public void testRemoveLicensesNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.ADD); result = null;
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.REMOVE); result = new AuthorizeException();
            }
        };

        String name = "LICENSE";
        Bundle created = itemManager.createBundle(context, it, name);
        created.setName(name);

        String bsname = "License";
        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream result = itemManager.createSingleBitstream(context, it, new FileInputStream(f), bsname);
        bitstreamManager.setFormat(context, result, bitstreamFormatManager.findByShortDescription(context, bsname));
        bundleManager.addBitstream(context, created, result);

        itemManager.removeLicenses(context, it);
        fail("Exception expected");
    }

    /**
     * Test of update method, of class Item.
     */
    @Test
    public void testUpdateAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.WRITE); result = null;
            }
        };

        //TOOD: how to test?
        itemManager.update(context, it);
    }

    /**
     * Test of update method, of class Item.
     */
    @Test
    public void testUpdateAuth2() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.WRITE); result = null;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = false;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = false;
                AuthorizeManager.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = new AuthorizeException();

            }
        };

        context.turnOffAuthorisationSystem();
        Collection c = createCollection();
        it.setOwningCollection(c);
        context.restoreAuthSystemState();

        //TOOD: how to test?
        itemManager.update(context, it);
    }

    /**
     * Test of update method, of class Item.
     */
    @Test(expected=AuthorizeException.class)
    public void testUpdateNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.WRITE); result = new AuthorizeException();
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, anyBoolean); result = false;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, anyBoolean); result = false;
                AuthorizeManager.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, anyBoolean); result = new AuthorizeException();
            }
        };

        context.turnOffAuthorisationSystem();
        Collection c = createCollection();
        it.setOwningCollection(c);
        context.restoreAuthSystemState();

        //TOOD: how to test?
        itemManager.update(context, it);
    }

    /**
     * Test of withdraw method, of class Item.
     */
    @Test
    public void testWithdrawAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeUtil authManager;
            {
                AuthorizeUtil.authorizeWithdrawItem((Context) any, (Item) any);
                result = null;
            }
            AuthorizeManager authorizeManager;
            {
                AuthorizeManager.authorizeActionBoolean((Context) any, (Item) any,
                        Constants.WRITE); result = true;
            }
        };

        itemManager.withdraw(context, it);
        assertTrue("testWithdrawAuth 0", it.isWithdrawn());
    }

    /**
     * Test of withdraw method, of class Item.
     */
    @Test(expected=AuthorizeException.class)
    public void testWithdrawNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeUtil authManager;
            {
                AuthorizeUtil.authorizeWithdrawItem((Context) any, (Item) any);
                result = new AuthorizeException();
            }
        };

        itemManager.withdraw(context, it);
        fail("Exception expected");
    }

    /**
     * Test of reinstate method, of class Item.
     */
    @Test
    public void testReinstateAuth() throws Exception
    {

        context.turnOffAuthorisationSystem();
        itemManager.withdraw(context, it);
        itemManager.reinstate(context, it);
        context.restoreAuthSystemState();
        assertFalse("testReinstate 0", it.isWithdrawn());
    }

    /**
     * Test of reinstate method, of class Item.
     */
    @Test(expected=AuthorizeException.class)
    public void testReinstateNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeUtil authManager;
            {
                AuthorizeUtil.authorizeWithdrawItem((Context) any, (Item) any);
                result = null;
                AuthorizeUtil.authorizeReinstateItem((Context) any, (Item) any);
                result = new AuthorizeException();
            }
        };

        itemManager.withdraw(context, it);
        itemManager.reinstate(context, it);
        fail("Exceotion expected");
    }

    /**
     * Test of delete method, of class Item.
     */
    @Test
    public void testDeleteAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.REMOVE, true); result = null;
            }
        };

        boolean added = false;
        Iterator<Item> items = collectionManager.getItems(context, collection);
        while(items.hasNext())
        {
            Item tmp = items.next();
            if(tmp.equals(it))
            {
                added = true;
            }
        }

        assertTrue("testDeleteAuth 0", added);

        int id = it.getID();
        itemManager.delete(context,  it);
        Item found = itemManager.find(context, id);
        assertThat("testDeleteAuth 1", found, nullValue());

        added = false;
        items = collectionManager.getItems(context, collection);
        while(items.hasNext())
        {
            Item tmp = items.next();
            if(tmp.equals(it))
            {
                added = true;
            }
        }
        assertFalse("testDeleteAuth 2", added);
    }

    /**
     * Test of delete method, of class Item.
     */
    @Test(expected=AuthorizeException.class)
    public void testDeleteNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.REMOVE); result = new AuthorizeException();
            }
        };

        itemManager.delete(context, it);
        fail("Exception expected");
    }

    /**
     * Test of equals method, of class Item.
     */
    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void testEquals() throws Exception
    {
        context.turnOffAuthorisationSystem();
        assertFalse("testEquals 0", it.equals(null));
        Item item = createItem();
        try {
            assertFalse("testEquals 1",it.equals(item));
            assertTrue("testEquals 2", it.equals(it));
        } finally {
            itemManager.delete(context, item);
            context.restoreAuthSystemState();
        }
    }

    /**
     * Test of isOwningCollection method, of class Item.
     */
    @Test
    public void testIsOwningCollection() throws SQLException, AuthorizeException
    {
        context.turnOffAuthorisationSystem();
        Collection c = createCollection();
        context.restoreAuthSystemState();

        boolean result = itemManager.isOwningCollection(it, c);
        assertFalse("testIsOwningCollection 0", result);
    }

    /**
     * Test of getType method, of class Item.
     */
    @Test
    public void testGetType()
    {
        assertThat("testGetType 0", it.getType(), equalTo(Constants.ITEM));
    }

    /**
     * Test of replaceAllItemPolicies method, of class Item.
     */
    @Test
    public void testReplaceAllItemPolicies() throws Exception
    {
        List<ResourcePolicy> newpolicies = new ArrayList<ResourcePolicy>();
        ResourcePolicy pol1 = resourcePolicyManager.create(context);
        newpolicies.add(pol1);
        itemManager.replaceAllItemPolicies(context, it, newpolicies);

        List<ResourcePolicy> retrieved = AuthorizeManager.getPolicies(context, it);
        assertThat("testReplaceAllItemPolicies 0",retrieved, notNullValue());
        assertThat("testReplaceAllItemPolicies 1", retrieved.size(), equalTo(newpolicies.size()));
    }

    /**
     * Test of replaceAllBitstreamPolicies method, of class Item.
     */
    @Test
    public void testReplaceAllBitstreamPolicies() throws Exception
    {
        context.turnOffAuthorisationSystem();
        //we add some bundles for the test
        String name = "LICENSE";
        Bundle created = itemManager.createBundle(context, it, name);
        created.setName(name);

        String bsname = "License";
        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream result = itemManager.createSingleBitstream(context, it, new FileInputStream(f), bsname);
        bitstreamManager.setFormat(context, result, bitstreamFormatManager.findByShortDescription(context, bsname));
        bundleManager.addBitstream(context, created, result);

        List<ResourcePolicy> newpolicies = new ArrayList<ResourcePolicy>();
        newpolicies.add(resourcePolicyManager.create(context));
        newpolicies.add(resourcePolicyManager.create(context));
        newpolicies.add(resourcePolicyManager.create(context));
        context.restoreAuthSystemState();

        itemManager.replaceAllBitstreamPolicies(context, it, newpolicies);

        List<ResourcePolicy> retrieved = new ArrayList<ResourcePolicy>();
        List<Bundle> bundles = it.getBundles();
        for(Bundle b: bundles)
        {
            retrieved.addAll(bundleManager.getBundlePolicies(context, b));
            retrieved.addAll(bundleManager.getBitstreamPolicies(context, b));
        }
        assertFalse("testReplaceAllBitstreamPolicies 0",retrieved.isEmpty());

        boolean equals = true;
        for(int i=0; i < newpolicies.size() && equals; i++)
        {
            if(!newpolicies.contains(retrieved.get(i)))
            {
                equals = false;
            }
        }
        assertTrue("testReplaceAllBitstreamPolicies 1", equals);
    }

    /**
     * Test of removeGroupPolicies method, of class Item.
     */
    @Test
    public void testRemoveGroupPolicies() throws Exception
    {
        context.turnOffAuthorisationSystem();
        List<ResourcePolicy> newpolicies = new ArrayList<ResourcePolicy>();
        Group g = groupManager.create(context);
        ResourcePolicy pol1 = resourcePolicyManager.create(context);
        newpolicies.add(pol1);
        resourcePolicyManager.setGroup(pol1, g);
        itemManager.replaceAllItemPolicies(context, it, newpolicies);
        context.restoreAuthSystemState();

        itemManager.removeGroupPolicies(context, it, g);

        List<ResourcePolicy> retrieved = AuthorizeManager.getPolicies(context, it);
        assertThat("testRemoveGroupPolicies 0",retrieved, notNullValue());
        assertTrue("testRemoveGroupPolicies 1", retrieved.isEmpty());
    }

    /**
     * Test of inheritCollectionDefaultPolicies method, of class Item.
     */
    @Test
    public void testInheritCollectionDefaultPolicies() throws Exception
    {
        context.turnOffAuthorisationSystem();

        Collection c = createCollection();

        //TODO: we would need a method to get policies from collection, probably better!
        List<ResourcePolicy> newpolicies = AuthorizeManager.getPoliciesActionFilter(context, c,
                Constants.DEFAULT_BITSTREAM_READ);
        Iterator<ResourcePolicy> iter = newpolicies.iterator();
        while (iter.hasNext())
        {
            ResourcePolicy rp = (ResourcePolicy) iter.next();
            rp.setAction(Constants.READ);
        }

        //we add some bundles for the test
        String name = "LICENSE";
        Bundle created = itemManager.createBundle(context, it, name);
        created.setName(name);

        String bsname = "License";
        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream result = itemManager.createSingleBitstream(context, it, new FileInputStream(f), bsname);
        bitstreamManager.setFormat(context, result, bitstreamFormatManager.findByShortDescription(context, bsname));
        bundleManager.addBitstream(context, created, result);

        context.restoreAuthSystemState();

        itemManager.inheritCollectionDefaultPolicies(context, it, c);

        //test item policies
        List<ResourcePolicy> retrieved = AuthorizeManager.getPolicies(context, it);
        boolean equals = true;
        for(int i=0; i < retrieved.size() && equals; i++)
        {
            if(!newpolicies.contains(retrieved.get(i)))
            {
                equals = false;
            }
        }
        assertTrue("testInheritCollectionDefaultPolicies 0", equals);

        retrieved = new ArrayList<ResourcePolicy>();
        List<Bundle> bundles = it.getBundles();
        for(Bundle b: bundles)
        {
            retrieved.addAll(bundleManager.getBundlePolicies(context, b));
            retrieved.addAll(bundleManager.getBitstreamPolicies(context, b));
        }
        assertFalse("testInheritCollectionDefaultPolicies 1",retrieved.isEmpty());

        equals = true;
        for(int i=0; i < newpolicies.size() && equals; i++)
        {
            if(!newpolicies.contains(retrieved.get(i)))
            {
                equals = false;
            }
        }
        assertTrue("testInheritCollectionDefaultPolicies 2", equals);

    }

    /**
     * Test of move method, of class Item.
     */
    @Test
    public void testMove() throws Exception
    {
        //we disable the permission testing as it's shared with other methods where it's already tested (can edit)
        context.turnOffAuthorisationSystem();
        Collection from = createCollection();
        Collection to = createCollection();
        it.setOwningCollection(from);

        collectionManager.move(context, it, from, to);
        context.restoreAuthSystemState();
        assertThat("testMove 0",it.getOwningCollection(), notNullValue());
        assertThat("testMove 1", it.getOwningCollection(), equalTo(to));
    }

    /**
     * Test of hasUploadedFiles method, of class Item.
     */
    @Test
    public void testHasUploadedFiles() throws Exception
    {
        assertFalse("testHasUploadedFiles 0", itemManager.hasUploadedFiles(it));
    }

    /**
     * Test of getCollectionsNotLinked method, of class Item.
     */
    @Test
    public void testGetCollectionsNotLinked() throws Exception
    {
        List<Collection> result = collectionManager.getCollectionsNotLinked(context, it);
        boolean isin = false;
        for(Collection c: result)
        {
            Iterator<Item> iit = collectionManager.getAllItems(context, collection);
            while(iit.hasNext())
            {
                if(iit.next().getID() == it.getID())
                {
                    isin = true;
                }
            }
        }
        assertTrue("testGetCollectionsNotLinked 0", isin);
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditBooleanAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeActionBoolean((Context) any, (Item) any,
                        Constants.WRITE); result = true;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = true;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = true;
                AuthorizeManager.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = null;
            }
        };

        assertTrue("testCanEditBooleanAuth 0", itemManager.canEdit(context, it));
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditBooleanAuth2() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeActionBoolean((Context) any, (Item) any,
                        Constants.WRITE); result = false;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = true;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = true;
                AuthorizeManager.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = null;
            }
        };

        assertTrue("testCanEditBooleanAuth2 0", itemManager.canEdit(context, it));
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditBooleanAuth3() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeActionBoolean((Context) any, (Item) any,
                        Constants.WRITE); result = false;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, true); result = true;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, true); result = true;
                AuthorizeManager.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, true); result = null;
            }
        };

        context.turnOffAuthorisationSystem();
        Collection c = createCollection();
        it.setOwningCollection(c);
        context.restoreAuthSystemState();

        assertTrue("testCanEditBooleanAuth3 0", itemManager.canEdit(context, it));
    }

    /**
     * Test of canEditBoolean method, of class Collection.
     */
    @Test
    public void testCanEditBooleanNoAuth() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeActionBoolean((Context) any, (Item) any,
                        Constants.WRITE); result = false;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.WRITE, anyBoolean); result = false;
                AuthorizeManager.authorizeActionBoolean((Context) any, (Community) any,
                        Constants.ADD, anyBoolean); result = false;
                AuthorizeManager.authorizeAction((Context) any, (Collection) any,
                        Constants.WRITE, anyBoolean); result = new AuthorizeException();
            }
        };

        context.turnOffAuthorisationSystem();
        Collection c = createCollection();
        it.setOwningCollection(c);
        context.restoreAuthSystemState();

        assertFalse("testCanEditBooleanNoAuth 0", itemManager.canEdit(context, it));
    }

    /**
     * Test of getName method, of class Item.
     */
    @Test
    public void testGetName()
    {
        assertThat("testGetName 0", it.getName(), nullValue());
    }

    /**
     * Test of findByMetadataField method, of class Item.
     */
    @Test
    public void testFindByMetadataField() throws Exception
    {
        String schema = "dc";
        String element = "contributor";
        String qualifier = "author";
        String value = "value";

        Iterator<Item> result = itemManager.findByMetadataField(context, schema, element, qualifier, value);
        assertThat("testFindByMetadataField 0", result, notNullValue());
        assertFalse("testFindByMetadataField 1", result.hasNext());

        itemManager.addMetadata(context, it, schema,element, qualifier, Item.ANY, value);
        //Ensure that the current user can update the item
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.WRITE); result = null;
            }
        };
        itemManager.update(context, it);

        result = itemManager.findByMetadataField(context, schema, element, qualifier, value);
        assertThat("testFindByMetadataField 3",result,notNullValue());
        assertTrue("testFindByMetadataField 4",result.hasNext());
        assertTrue("testFindByMetadataField 5", result.next().equals(it));
    }

    /**
     * Test of getAdminObject method, of class Item.
     */
    @Test
    @Override
    public void testGetAdminObject() throws SQLException
    {
        //default community has no admin object
        assertThat("testGetAdminObject 0", (Item) itemManager.getAdminObject(context, it, Constants.REMOVE), equalTo(it));
        assertThat("testGetAdminObject 1", (Item) itemManager.getAdminObject(context, it, Constants.ADD), equalTo(it));
        assertThat("testGetAdminObject 2", (Collection) itemManager.getAdminObject(context, it, Constants.DELETE), equalTo(collection));
        assertThat("testGetAdminObject 3", (Item) itemManager.getAdminObject(context, it, Constants.ADMIN), equalTo(it));
    }

    /**
     * Test of getParentObject method, of class Item.
     */
    @Test
    @Override
    public void testGetParentObject() throws SQLException
    {
        try
        {
            //default has collection parent
            assertThat("testGetParentObject 0", itemManager.getParentObject(context, it), notNullValue());

            context.turnOffAuthorisationSystem();
            Collection parent = createCollection();
            it.setOwningCollection(parent);
            context.restoreAuthSystemState();
            assertThat("testGetParentObject 1", itemManager.getParentObject(context, it), notNullValue());
            assertThat("testGetParentObject 2", (Collection) itemManager.getParentObject(context, it), equalTo(parent));
        }
        catch(AuthorizeException ex)
        {
            fail("Authorize exception catched");
        }
    }

    /**
     * Test of findByAuthorityValue method, of class Item.
     */
    @Test
    public void testFindByAuthorityValue() throws Exception
    {
        String schema = "dc";
        String element = "language";
        String qualifier = "iso";
        String value = "en";
        String authority = "accepted";
        int confidence = 0;

        Iterator<Item> result = itemManager.findByAuthorityValue(context, schema, element, qualifier, value);
        assertThat("testFindByAuthorityValue 0", result, notNullValue());
        assertFalse("testFindByAuthorityValue 1", result.hasNext());

        itemManager.addMetadata(context, it, schema, element, qualifier, Item.ANY, value, authority, confidence);
        //Ensure that the current user can update the item
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.WRITE); result = null;
            }
        };
        itemManager.update(context, it);

        result = itemManager.findByAuthorityValue(context, schema, element, qualifier, authority);
        assertThat("testFindByAuthorityValue 3",result,notNullValue());
        assertTrue("testFindByAuthorityValue 4",result.hasNext());
        assertThat("testFindByAuthorityValue 5",result.next(),equalTo(it));
    }

    protected Collection createCollection() throws SQLException, AuthorizeException {
        return collectionManager.create(context, owningCommunity);
    }

    protected Item createItem() throws SQLException, IOException, AuthorizeException, IllegalAccessException {
        WorkspaceItem workspaceItem = workspaceItemManager.create(context, collection, false);
        return InstallItem.installItem(context, workspaceItem);
    }

}