/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.test.content;

import java.util.Iterator;

import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.*;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import mockit.NonStrictExpectations;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.log4j.Logger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.ResourcePolicy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * Units tests for class Bundle
 * @author pvillega
 */
public class BundleTest extends AbstractDSpaceObjectTest
{
    /** log4j category */
    private static final Logger log = Logger.getLogger(BundleTest.class);
 
    /**
     * Bundle instance for the tests
     */
    private Bundle b;
    private Item it;
    private Collection collection;
    private Community owningCommunity;
    private String bundleName = "ORIGINAL";


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
            context.turnOffAuthorisationSystem();
            this.owningCommunity = communityService.create(context, null);
            this.collection = collectionService.create(context, owningCommunity);
            WorkspaceItem workspaceItem = workspaceItemService.create(context, collection, false);
            it = installItemService.installItem(context, workspaceItem);
            this.b = bundleService.create(context, it, bundleName);
            this.dspaceObject = b;

            //we need to commit the changes so we don't block the table for testing
            //we need to commit the changes so we don't block the table for testing
            context.commit();
            context.restoreAuthSystemState();
        }
        catch (SQLException ex)
        {
            log.error("SQL Error in init", ex);
            fail("SQL Error in init: " + ex.getMessage());
        } catch (AuthorizeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
        context.turnOffAuthorisationSystem();
        if(bundleService.find(context, b.getID()) != null)
        {
            itemService.removeBundle(context, it, b);
        }
        collectionService.removeItem(context, collection, it);
        communityService.removeCollection(context, owningCommunity, collection);
        context.restoreAuthSystemState();
        super.destroy();
    }

    /**
     * Test of find method, of class Bundle.
     */
    @Test
    public void testBundleFind() throws SQLException
    {
        int id = b.getID();
        Bundle found =  bundleService.find(context, id);
        assertThat("testBundleFind 0", found, notNullValue());
        assertThat("testBundleFind 1", found.getID(), equalTo(id));
    }

    /**
     * Test of create method, of class Bundle.
     */
    @Test
    public void testCreate() throws SQLException, AuthorizeException, IOException {
        new NonStrictExpectations(authorizeService.getClass())
        {
            {
                authorizeService.authorizeAction((Context) any, (Item) any,
                        Constants.ADD, true); result = null;
                authorizeService.authorizeAction((Context) any, (Item) any,
                        Constants.REMOVE); result = null;
            }
        };
        Bundle created = bundleService.create(context, it, "test");
        //the item created by default has no name nor type set
        assertThat("testCreate 0", created, notNullValue());
        assertTrue("testCreate 1", created.getID() >= 0);
        assertTrue("testCreate 2", created.getBitstreams().size() == 0);
        assertThat("testCreate 3", created.getName(), equalTo("test"));
        itemService.removeBundle(context, it, created);
    }

    /**
     * Test of getID method, of class Bundle.
     */
    @Test
    public void testGetID()
    {
        assertTrue("testGetID 0", b.getID() >= 0);
    }

    /**
     * Test of getName method, of class Bundle.
     */
    @Test
    public void testGetName()
    {
        //created bundle has no name
        assertThat("testGetName 0", b.getName(), equalTo(bundleName));
    }

    /**
     * Test of setName method, of class Bundle.
     */
    @Test
    public void testSetName()
    {
        String name = "new name";
        b.setName(name);
        assertThat("testSetName 0", b.getName(), notNullValue());
        assertThat("testSetName 1", b.getName(), not(equalTo("")));
        assertThat("testSetName 2", b.getName(), equalTo(name));
    }

    /**
     * Test of getPrimaryBitstreamID method, of class Bundle.
     */
    @Test
    public void testGetPrimaryBitstreamID() 
    {
        //is -1 when not set
        assertThat("testGetPrimaryBitstreamID 0", b.getPrimaryBitstream(), nullValue());
    }

    /**
     * Test of setPrimaryBitstreamID method, of class Bundle.
     */
    //TODO: add test for this once we can create bitstreams with our tests
    /*
    @Test
    public void testSetPrimaryBitstreamID()
    {


        b.setPrimaryBitstreamID(id);
        assertThat("testSetPrimaryBitstreamID 0", b.getPrimaryBitstreamID(), equalTo(id));
    }
    */
    /**
     * Test of unsetPrimaryBitstreamID method, of class Bundle.
     */
        //TODO: add test for this once we can create bitstreams with our tests
    /*
    @Test
    public void testUnsetPrimaryBitstreamID()
    {
        //set a value different than default
        int id = 6;
        b.setPrimaryBitstream(id);
        //unset
        b.setPrimaryBitstream(null);
        //is -1 when not set
        assertThat("testUnsetPrimaryBitstreamID 0", b.getPrimaryBitstream(), nullValue());
    }
*/
    /**
     * Test of getHandle method, of class Bundle.
     */
    @Test
    public void testGetHandle() throws SQLException {
        //no handle for bundles
        assertThat("testGetHandle 0", b.getHandle(context), nullValue());
    }

    /**
     * Test of getBitstreamByName method, of class Bundle.
     */
    @Test
    public void testGetBitstreamByName() throws FileNotFoundException, SQLException, IOException, AuthorizeException
    {
        new NonStrictExpectations(authorizeService.getClass())
        {
            {
                authorizeService.authorizeAction((Context) any, (Bundle) any,
                        Constants.ADD); result = null;
                authorizeService.authorizeAction((Context) any, (Bundle) any,
                        Constants.WRITE); result = null;
            }
        };

        String name = "name";
        //by default there is no bitstream
        assertThat("testGetHandle 0", bundleService.getBitstreamByName(b, name), nullValue());
        
        //let's add a bitstream
        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream bs = bitstreamService.create(context, new FileInputStream(f));
        bs.setName(name);
        bundleService.addBitstream(context, b, bs);
        bundleService.update(context, b);
        assertThat("testGetHandle 1", bundleService.getBitstreamByName(b, name), notNullValue());
        assertThat("testGetHandle 2", bundleService.getBitstreamByName(b, name), equalTo(bs));
        assertThat("testGetHandle 3", bundleService.getBitstreamByName(b, name).getName(), equalTo(name));
        context.commit();
    }

    /**
     * Test of getBitstreams method, of class Bundle.
     */
    @Test
    public void testGetBitstreams() throws FileNotFoundException, SQLException, IOException, AuthorizeException
    {
        new NonStrictExpectations(authorizeService.getClass())
        {
            {
                authorizeService.authorizeAction((Context) any, (Bundle) any,
                        Constants.ADD); result = null;
                authorizeService.authorizeAction((Context) any, (Bundle) any,
                        Constants.WRITE); result = null;
            }
        };

        //default bundle has no bitstreams
        assertThat("testGetBitstreams 0", b.getBitstreams(), notNullValue());
        assertThat("testGetBitstreams 1", b.getBitstreams().size(), equalTo(0));

        //let's add a bitstream
        String name = "name";
        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream bs = bitstreamService.create(context, new FileInputStream(f));
        bs.setName(name);
        bundleService.addBitstream(context, b, bs);
        bundleService.update(context, b);
        assertThat("testGetBitstreams 2", b.getBitstreams(), notNullValue());
        assertThat("testGetBitstreams 3", b.getBitstreams().size(), equalTo(1));
        assertThat("testGetBitstreams 4", b.getBitstreams().get(0).getBitstream().getName(), equalTo(name));
        context.commit();
    }

    /**
     * Test of getItems method, of class Bundle.
     */
    @Test
    public void testGetItems() throws SQLException
    {
        //by default this bundle belong to no item
        assertThat("testGetItems 0", bundleService.getParentObject(context, b), notNullValue());
        assertThat("testGetItems 1", (Item) bundleService.getParentObject(context, b), equalTo(it));
    }

    /**
     * Test of createBitstream method, of class Bundle.
     */
    @Test(expected=AuthorizeException.class)
    public void testCreateBitstreamNoAuth() throws FileNotFoundException, AuthorizeException, SQLException, IOException
    {
        new NonStrictExpectations(authorizeService.getClass())
        {
            {
                authorizeService.authorizeAction((Context) any, (Bundle) any,
                        Constants.ADD); result = new AuthorizeException();
            }
        };

        String name = "name";
        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream bs = bitstreamService.create(context, b, new FileInputStream(f));
        fail("Exception should be thrown");
    }

    /**
     * Test of createBitstream method, of class Bundle.
     */
    @Test
    public void testCreateBitstreamAuth() throws FileNotFoundException, AuthorizeException, SQLException, IOException
    {
        new NonStrictExpectations(authorizeService.getClass())
        {
            {
                authorizeService.authorizeAction((Context) any, (Bundle) any,
                        Constants.ADD); result = null;
                authorizeService.authorizeAction((Context) any, (Bundle) any,
                        Constants.WRITE); result = null;
            }
        };

        String name = "name";
        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream bs = bitstreamService.create(context, b, new FileInputStream(f));
        bs.setName(name);
        assertThat("testCreateBitstreamAuth 0", bundleService.getBitstreamByName(b, name), notNullValue());
        assertThat("testCreateBitstreamAuth 1", bundleService.getBitstreamByName(b, name), equalTo(bs));
        assertThat("testCreateBitstreamAuth 2", bundleService.getBitstreamByName(b, name).getName(), equalTo(name));
    }
    
    /**
     * Test of registerBitstream method, of class Bundle.
     */
    @Test(expected=AuthorizeException.class)
    public void testRegisterBitstreamNoAuth() throws AuthorizeException, IOException, SQLException 
    {

        new NonStrictExpectations(authorizeService.getClass())
        {
            {
                authorizeService.authorizeAction((Context) any, (Bundle) any, Constants.ADD);
                result = new AuthorizeException();
            }
        };

        int assetstore = 0;
        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream bs = bitstreamService.register(context, b, assetstore, f.getAbsolutePath());
        fail("Exception should be thrown");
    }

    /**
     * Test of registerBitstream method, of class Bundle.
     */
    @Test
    public void testRegisterBitstreamAuth() throws AuthorizeException, IOException, SQLException 
    {

        new NonStrictExpectations(authorizeService.getClass())
        {
            AuthorizeService authManager;
            {
                authorizeService.authorizeAction((Context) any, (Bundle) any, Constants.ADD);
                result = null;
                authorizeService.authorizeAction((Context) any, (Bitstream) any, Constants.WRITE);
                result = null;
            }
        };

        int assetstore = 0;
        String name = "name bitstream";
        File f = new File(testProps.get("test.bitstream").toString());        
        Bitstream bs = bitstreamService.register(context, b, assetstore, f.getName());
        bs.setName(name);
        assertThat("testRegisterBitstream 0", bundleService.getBitstreamByName(b, name), notNullValue());
        assertThat("testRegisterBitstream 1", bundleService.getBitstreamByName(b, name), equalTo(bs));
        assertThat("testRegisterBitstream 2", bundleService.getBitstreamByName(b, name).getName(), equalTo(name));
    }

    /**
     * Test of addBitstream method, of class Bundle.
     */
    @Test(expected=AuthorizeException.class)
    public void testAddBitstreamNoAuth() throws SQLException, AuthorizeException
    {
        new NonStrictExpectations(authorizeService.getClass())
        {
            {
                authorizeService.authorizeAction((Context) any, (Bundle) any, Constants.ADD);
                result = new AuthorizeException();
            }
        };

        int id = 1;
        Bitstream bs = bitstreamService.find(context, id);
        bundleService.addBitstream(context, b, bs);
        bundleService.update(context, b);
        fail("Exception should have been thrown");
    }

    /**
     * Test of addBitstream method, of class Bundle.
     */
    @Test
    public void testAddBitstreamAuth() throws SQLException, AuthorizeException, FileNotFoundException, IOException
    {
        new NonStrictExpectations(authorizeService.getClass())
        {
            {
                authorizeService.authorizeAction((Context) any, (Bundle) any, Constants.ADD);
                result = null;
                authorizeService.authorizeAction((Context) any, (Bitstream) any, Constants.WRITE);
                result = null;
            }
        };


        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream bs = bitstreamService.create(context, b, new FileInputStream(f));
        bs.setName("name");
        assertThat("testAddBitstreamAuth 0", bundleService.getBitstreamByName(b, bs.getName()), notNullValue());
        assertThat("testAddBitstreamAuth 1", bundleService.getBitstreamByName(b, bs.getName()), equalTo(bs));
        assertThat("testAddBitstreamAuth 2", bundleService.getBitstreamByName(b, bs.getName()).getName(), equalTo(bs.getName()));
    }

    /**
     * Test of removeBitstream method, of class Bundle.
     */
    @Test(expected=AuthorizeException.class)
    public void testRemoveBitstreamNoAuth() throws SQLException, AuthorizeException, IOException
    {
        new NonStrictExpectations(authorizeService.getClass())
        {
            {
                authorizeService.authorizeAction((Context) any, (Bundle) any, Constants.REMOVE);
                result = new AuthorizeException();
            }
        };

        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream bs = bitstreamService.create(context, b, new FileInputStream(f));
        bs.setName("name");
        bundleService.removeBitstream(context, b, bs);
        fail("Exception should have been thrown");
    }

    /**
     * Test of removeBitstream method, of class Bundle.
     */
    @Test
    public void testRemoveBitstreamAuth() throws SQLException, AuthorizeException, IOException
    {
        new NonStrictExpectations(authorizeService.getClass())
        {
            {
                authorizeService.authorizeAction((Context) any, (Bundle) any, Constants.REMOVE);
                result = null;
                authorizeService.authorizeAction((Context) any, (Bundle) any, Constants.ADD);
                result = null;
                authorizeService.authorizeAction((Context) any, (Bitstream) any, Constants.WRITE);
                result = null;
            }
        };

        int id = 1;
        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream bs = bitstreamService.find(context, id);
        bundleService.addBitstream(context, b, bs);
        bundleService.update(context, b);
        context.commit();
        bundleService.removeBitstream(context, b, bs);
        bundleService.update(context, b);
        assertThat("testRemoveBitstreamAuth 0", bundleService.getBitstreamByName(b, bs.getName()), nullValue());
    }


    /**
     * Test of update method, of class Bundle.
     */
    @Test
    public void testUpdate() throws SQLException, AuthorizeException 
    {
        //TODO: we only check for sql errors
        //TODO: note that update can't throw authorize exception!!
        bundleService.update(context, b);
    }

    /**
     * Test of delete method, of class Bundle.
     */
    @Test
    public void testDelete() throws SQLException, AuthorizeException, IOException
    {
        new NonStrictExpectations(authorizeService.getClass())
        {
            {
                authorizeService.authorizeAction((Context) any, (Item) any,
                        Constants.REMOVE, true); result = null;
            }
        };
        int id = b.getID();
        itemService.removeBundle(context, it, b);
        context.commit();
        assertThat("testDelete 0", bundleService.find(context, id), nullValue());
    }

    /**
     * Test of getType method, of class Bundle.
     */
    @Test
    public void testGetType()
    {
        assertThat("testGetType 0", b.getType(), equalTo(Constants.BUNDLE));
    }

    /**
     * Test of inheritCollectionDefaultPolicies method, of class Bundle.
     */
    @Test
    public void testInheritCollectionDefaultPolicies() throws AuthorizeException, SQLException, CloneNotSupportedException {

        //TODO: we would need a method to get policies from collection, probably better!
        List<ResourcePolicy> newpolicies = authorizeService.getPoliciesActionFilter(context, collection,
                Constants.DEFAULT_BITSTREAM_READ);
        Iterator<ResourcePolicy> it = newpolicies.iterator();
        while (it.hasNext())
        {
            ResourcePolicy rp = (ResourcePolicy) it.next().clone();
            rp.setAction(Constants.READ);
        }

        bundleService.inheritCollectionDefaultPolicies(context, b, collection);

        List<ResourcePolicy> bspolicies = authorizeService.getPolicies(context, b);
        assertTrue("testInheritCollectionDefaultPolicies 0", newpolicies.size() == bspolicies.size());

        boolean equals = true;
        for(int i=0; i < newpolicies.size() && equals; i++)
        {
            if(!newpolicies.contains(bspolicies.get(i)))
            {
                equals = false;
            }
        }
        assertTrue("testInheritCollectionDefaultPolicies 1", equals);

        bspolicies = bundleService.getBitstreamPolicies(context, b);
        boolean exists = true;
        for(int i=0; bspolicies.size() > 0 && i < newpolicies.size() && exists; i++)
        {
            if(!bspolicies.contains(newpolicies.get(i)))
            {
                exists = false;
            }
        }
        assertTrue("testInheritCollectionDefaultPolicies 2", exists);
        
    }

    /**
     * Test of replaceAllBitstreamPolicies method, of class Bundle.
     */
    @Test
    public void testReplaceAllBitstreamPolicies() throws SQLException, AuthorizeException
    {
        List<ResourcePolicy> newpolicies = new ArrayList<ResourcePolicy>();
        newpolicies.add(resourcePolicyService.create(context));
        newpolicies.add(resourcePolicyService.create(context));
        newpolicies.add(resourcePolicyService.create(context));
        bundleService.replaceAllBitstreamPolicies(context, b, newpolicies);
        
        List<ResourcePolicy> bspolicies = authorizeService.getPolicies(context, b);
        assertTrue("testReplaceAllBitstreamPolicies 0", newpolicies.size() == bspolicies.size());

        boolean equals = true;
        for(int i=0; i < newpolicies.size() && equals; i++)
        {
            if(!newpolicies.contains(bspolicies.get(i)))
            {
                equals = false;
            }
        }
        assertTrue("testReplaceAllBitstreamPolicies 1", equals);

        bspolicies = bundleService.getBitstreamPolicies(context, b);
        boolean exists = true;
        for(int i=0; bspolicies.size() > 0 && i < newpolicies.size() && exists; i++)
        {
            if(!bspolicies.contains(newpolicies.get(i)))
            {
                exists = false;
            }
        }
        assertTrue("testReplaceAllBitstreamPolicies 2", exists);
    }

    /**
     * Test of getBundlePolicies method, of class Bundle.
     */
    @Test
    public void testGetBundlePolicies() throws SQLException
    {
        //empty by default
        List<ResourcePolicy> bpolicies = authorizeService.getPolicies(context, b);
        assertFalse("testGetBundlePolicies 0", bpolicies.isEmpty());
    }

    /**
     * Test of getBundlePolicies method, of class Bundle.
     */
    @Test
    public void testGetBitstreamPolicies() throws SQLException
    {
        //empty by default
        List<ResourcePolicy> bspolicies = bundleService.getBitstreamPolicies(context, b);
        assertTrue("testGetBitstreamPolicies 0", bspolicies.isEmpty());
    }

    /**
     * Test of getAdminObject method, of class Bundle.
     */
    @Test
    @Override
    public void testGetAdminObject() throws SQLException
    {
        //default bundle has no admin object
        assertEquals("testGetAdminObject 0", bundleService.getAdminObject(context, b, Constants.REMOVE), it);
        assertEquals("testGetAdminObject 1", bundleService.getAdminObject(context, b, Constants.ADD), it);
    }

    /**
     * Test of getParentObject method, of class Bundle.
     */
    @Test
    @Override
    public void testGetParentObject() throws SQLException
    {
        //default bundle has no parent
        assertEquals("testGetParentObject 0", bundleService.getParentObject(context, b), it);
    }

}