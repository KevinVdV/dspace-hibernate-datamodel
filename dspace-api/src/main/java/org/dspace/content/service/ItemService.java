package org.dspace.content.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.content.*;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 21/03/14
 * Time: 10:24
 */
public interface ItemService extends DSpaceObjectService<Item> {

    public Item create(Context context, WorkspaceItem workspaceItem) throws SQLException, AuthorizeException;

    public Item createTemplateItem(Context context, Collection collection) throws SQLException, AuthorizeException;

    public Iterator<Item> findAll(Context context) throws SQLException;

    public Iterator<Item> findAllUnfiltered(Context context) throws SQLException;

    public Iterator<Item> findBySubmitter(Context context, EPerson eperson) throws SQLException;

    /**
     * Get the in_archive items in this collection. The order is indeterminate.
     *
     * @return an iterator over the items in the collection.
     * @throws SQLException
     */
    public Iterator<Item> findArchivedItemsByCollection(Context context, Collection collection) throws SQLException;

    /**
     * Get the in_archive items in this collection. The order is indeterminate.
     * Provides the ability to use limit and offset, for efficient paging.
     * @param limit Max number of results in set
     * @param offset Number of results to jump ahead by. 100 = 100th result is first, not 100th page.
     * @return an iterator over the items in the collection.
     * @throws SQLException
     */
    public Iterator<Item> findArchivedItemsByCollection(Context context, Collection collection, Integer limit, Integer offset) throws SQLException;

    /**
     * Get all the items in this collection. The order is indeterminate.
     *
     * @return an iterator over the items in the collection.
     * @throws SQLException
     */
    public Iterator<Item> findByCollection(Context context, Collection collection) throws SQLException;

    public List<MetadataValue> getMetadata(Item item, MetadataField metadataField, String lang);

    public List<MetadataValue> getMetadata(Item item, String schema, String element, String qualifier, String lang);

    public List<MetadataValue> getMetadata(Item item, String mdString);

    public void addMetadata(Context context, Item item, MetadataField metadataField, String lang, String value) throws SQLException;

    public void addMetadata(Context context, Item item, MetadataField metadataField, String lang, List<String> values, List<String> authorities, List<Integer> confidences) throws SQLException;

    public void addMetadata(Context context, Item item, String schema, String element, String qualifier, String lang, List<String> values) throws SQLException;

    public void addMetadata(Context context, Item item, String schema, String element, String qualifier, String lang, List<String> values, List<String> authorities, List<Integer> confidences) throws SQLException;

    public void addMetadata(Context context, Item item, String schema, String element, String qualifier, String lang, String value) throws SQLException;

    public void addMetadata(Context context, Item item, String schema, String element, String qualifier, String lang, String value, String authority, int confidence) throws SQLException;

    public void clearMetadata(Context context, Item item, String schema, String element, String qualifier, String lang) throws SQLException;

    public void removeMetadataValues(Context context, Item item, List<MetadataValue> values) throws SQLException;

    public boolean isIn(Item item, Collection collection) throws SQLException;

    public List<Community> getCommunities(Context context, Item item) throws SQLException;

    public List<Bundle> getBundles(Item item, String name) throws SQLException;

    public void addBundle(Context context, Item item, Bundle b) throws SQLException, AuthorizeException;

    public void removeAllBundles(Context context, Item item) throws AuthorizeException, SQLException, IOException;

    public void removeBundle(Context context, Item item, Bundle b) throws SQLException, AuthorizeException, IOException;

    public Bitstream createSingleBitstream(Context context, Item item, InputStream is, String name) throws AuthorizeException, IOException, SQLException;

    public Bitstream createSingleBitstream(Context context, Item item, InputStream is) throws AuthorizeException, IOException, SQLException;

    public List<Bitstream> getNonInternalBitstreams(Item item) throws SQLException;

    public void removeDSpaceLicense(Context context, Item item) throws SQLException, AuthorizeException, IOException;

    public void removeLicenses(Context context, Item item) throws SQLException, AuthorizeException, IOException;

    public void withdraw(Context context, Item item) throws SQLException, AuthorizeException;

    public void reinstate(Context context, Item item) throws SQLException, AuthorizeException;

    public void delete(Context context, Item item) throws SQLException, IOException, AuthorizeException;

    public boolean isOwningCollection(Item item, Collection c);

    public void replaceAllItemPolicies(Context context, Item item, List<ResourcePolicy> newpolicies) throws SQLException, AuthorizeException;

    public void replaceAllBitstreamPolicies(Context context, Item item, List<ResourcePolicy> newpolicies) throws SQLException, AuthorizeException;

    public void removeGroupPolicies(Context context, Item item, Group g) throws SQLException, AuthorizeException;

    public void inheritCollectionDefaultPolicies(Context context, Item item, Collection c) throws SQLException, AuthorizeException;

    public void adjustBundleBitstreamPolicies(Context context, Item item, Collection c) throws SQLException, AuthorizeException;

    public void adjustItemPolicies(Context context, Item item, Collection collection) throws SQLException, AuthorizeException;

    public boolean hasUploadedFiles(Item item) throws SQLException;

    public boolean canEdit(Context context, Item item) throws java.sql.SQLException;

    public Iterator<Item> findByMetadataField(Context context, String schema, String element, String qualifier, String value) throws SQLException, AuthorizeException;

    public Iterator<Item> findByAuthorityValue(Context context, String schema, String element, String qualifier, String value) throws SQLException, AuthorizeException;
}
