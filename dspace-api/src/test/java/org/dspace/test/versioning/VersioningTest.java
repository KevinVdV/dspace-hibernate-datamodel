/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.test.versioning;

import org.apache.log4j.Logger;
import org.dspace.AbstractUnitTest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;
import org.dspace.content.WorkspaceItem;
import org.dspace.versioning.Version;
import org.dspace.versioning.VersionHistory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


/**
 * Unit Tests for versioning
 *
 * @author Fabio Bolognesi (fabio at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 */
public class VersioningTest extends AbstractUnitTest {

    private static final Logger log = Logger.getLogger(VersioningTest.class);

    private Item originalItem;
    private Item versionedItem;
    private String summary = "Unit test version";


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
            Community community = communityService.create(context, null);

            Collection col = collectionService.create(context, community);
            WorkspaceItem is = workspaceItemService.create(context, col, false);

            originalItem = installItemService.installItem(context, is);

            Version version = versionService.createVersion(context, originalItem, summary);
            WorkspaceItem wsi = workspaceItemService.findByItem(context, version.getItem());

            versionedItem = installItemService.installItem(context, wsi);
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
    public void destroy() throws Exception {
        super.destroy();
    }


    @Test
    public void testVersionFind() throws SQLException {
        VersionHistory versionHistory = versionHistoryService.findByItem(context, originalItem);
        assertThat("testFindVersionHistory", versionHistory, notNullValue());
        Version version = versionHistoryService.getVersion(versionHistory, versionedItem);
        assertThat("testFindVersion", version, notNullValue());
    }

    /**
     * Test of installItem method, of class InstallItem.
     */
    @Test
    public void testVersionSummary() throws Exception
    {
        //Start by creating a new item !
        VersionHistory versionHistory = versionHistoryService.findByItem(context, originalItem);
        Version version = versionHistoryService.getVersion(versionHistory, versionedItem);
        assertThat("Test_version_summary", summary, equalTo(version.getSummary()));
    }

    @Test
    public void testVersionHandle() throws Exception {
        assertThat("Test_version_handle", versionedItem.getHandle(context), notNullValue());
    }

    @Test
    public void testVersionDelete() throws Exception {
        context.turnOffAuthorisationSystem();
        versionService.removeVersion(context, versionedItem);
        assertThat("Test_version_delete", itemService.find(context, versionedItem.getID()), nullValue());
        assertThat("Test_version_handle_delete", versionedItem.getHandle(context), nullValue());
        context.restoreAuthSystemState();
    }
}
