package org.dspace.eperson;

import org.dbunit.DBTestCase;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.dbunit.util.fileloader.DataFileLoader;
import org.dbunit.util.fileloader.FlatXmlDataFileLoader;
import org.dspace.core.Context;
import org.dspace.hibernate.HibernateUtil;

import java.sql.SQLException;

/**
 * Created by Roel on 15/02/14.
 */
public class EPersonTest extends DBTestCase {

    @Override
    protected DatabaseOperation getSetUpOperation() throws Exception {
        return DatabaseOperation.REFRESH;
    }

    public EPersonTest() {
        System.setProperty("dspace.configuration","E:\\dspaces\\dspace-tst\\config\\dspace.cfg");
        try {
            HibernateUtil.getSession();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.setProperty( PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, "org.hsqldb.jdbcDriver" );
        System.setProperty( PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, "jdbc:hsqldb:sample" );
        System.setProperty( PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME, "sa" );
        System.setProperty( PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD, "" );
    }

    @Override
    protected IDataSet getDataSet() throws Exception {
        DataFileLoader loader = new FlatXmlDataFileLoader();
        return loader.load("/org/dspace/eperson/epersonData2.xml");
    }
    private Context context;


    public void setUp() throws Exception {

        super.setUp();
        context=new Context();

    }

    public void tearDown() throws Exception {
       context.complete();
    }

    public void testFind() throws Exception {
        EPersonEntity ePersonEntity = EPersonDAO.find(context, 1);
        assertEquals("Didn't find the expected email","Keyshawn@queenie.org",ePersonEntity.getEmail());
    }

    public void testFindByEmail() throws Exception {
        EPersonEntity ePersonEntity = EPersonDAO.findByEmail(context, "Keyshawn@queenie.org");
        assertNotNull("No eperson retrieved",ePersonEntity);
        assertEquals("Didn't find the expected entity", 1, ePersonEntity.getID());
    }

    public void testFindByNetid() throws Exception {
        fail("Not yet implemented");
    }

    public void testSearch() throws Exception {
        fail("Not yet implemented");
    }

    public void testSearch1() throws Exception {
        fail("Not yet implemented");
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
