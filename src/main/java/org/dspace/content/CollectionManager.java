package org.dspace.content;

import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.eperson.Group;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 20/03/14
 * Time: 11:28
 */
public interface CollectionManager extends DSpaceObjectManager<Collection> {

    public Collection find(Context context, int id) throws SQLException;

    public Collection create(Context context, Community community) throws SQLException, AuthorizeException;

    public Collection create(Context context, Community community, String handle) throws SQLException, AuthorizeException;

    public List<Collection> findAll(Context context) throws SQLException;

    public List<Collection> findAll(Context context, Integer limit, Integer offset) throws SQLException;

    public Iterator<Item> getItems(Context context, Collection collection) throws SQLException;

    public Iterator<Item> getItems(Context context, Collection collection, Integer limit, Integer offset) throws SQLException;

    public Iterator<Item> getAllItems(Context context, Collection collection) throws SQLException;

    public void setName(Collection collection, String value) throws MissingResourceException;

    public Bitstream setLogo(Context context, Collection collection, InputStream is) throws AuthorizeException,IOException, SQLException;

    public Group createWorkflowGroup(Context context, Collection collection, int step) throws SQLException, AuthorizeException, IllegalAccessException;

    public void setWorkflowGroup(Collection collection, int step, Group g);

    public Group getWorkflowGroup(Collection collection, int step) throws IllegalStateException;

    public Group createSubmitters(Context context, Collection collection) throws SQLException, AuthorizeException;

    public void removeSubmitters(Context context, Collection collection) throws SQLException, AuthorizeException;

    public Group createAdministrators(Context context, Collection collection) throws SQLException, AuthorizeException;

    public void removeAdministrators(Context context, Collection collection) throws SQLException, AuthorizeException;

    public String getLicense(Collection collection);

    public String getLicenseCollection(Collection collection);

    public boolean hasCustomLicense(Collection collection);

    public Item createTemplateItem(Context context, Collection collection) throws SQLException, AuthorizeException;

    public void removeTemplateItem(Context context, Collection collection) throws SQLException, AuthorizeException, IOException;

    public Collection findByTemplateItem(Context context, Item item) throws SQLException;

    public void addItem(Context context, Collection collection, Item item) throws SQLException, AuthorizeException;

    public void removeItem(Context context, Collection collection, Item item) throws SQLException, AuthorizeException,
            IOException;

    public boolean canEditBoolean(Context context, Collection collection) throws java.sql.SQLException;

    public boolean canEditBoolean(Context context, Collection collection, boolean useInheritance) throws java.sql.SQLException;

    public void canEdit(Context context, Collection collection)  throws AuthorizeException, SQLException;

    public void canEdit(Context context, Collection collection, boolean useInheritance) throws AuthorizeException, SQLException;

    public void delete(Context context, Collection collection) throws SQLException, AuthorizeException, IOException;

    public List<Collection> findAuthorized(Context context, Community comm, int actionID) throws java.sql.SQLException;

    public List<Collection> getCollectionsNotLinked(Context context, Item item) throws SQLException;

    public void move(Context context, Item item, Collection from, Collection to) throws SQLException, AuthorizeException, IOException;

    public void move(Context context, Item item, Collection from, Collection to, boolean inheritDefaultPolicies) throws SQLException, AuthorizeException, IOException;

}
