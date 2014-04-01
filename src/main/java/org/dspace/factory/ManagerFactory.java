package org.dspace.factory;

import org.dspace.content.*;
import org.dspace.eperson.EPersonRepo;
import org.dspace.eperson.GroupRepo;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 01/04/14
 * Time: 08:26
 * To change this template use File | Settings | File Templates.
 */
public interface ManagerFactory {

    public BitstreamFormatRepo getBitstreamFormatManager();

    public BitstreamRepo getBitstreamManager();

    public BundleRepo getBundleManager();

    public CollectionRepo getCollectionManager();

    public CommunityRepo getCommunityManager();

    public EPersonRepo getEPersonManager();

    public GroupRepo getGroupManager();

    public ItemRepo getItemManager();

    public MetadataFieldRepo getMetadataFieldManager();

    public MetadataSchemaRepo getMetadataSchemaManager();

    public MetadataValueRepo getMetadataValueManager();

    public WorkspaceItemRepo getWorkspaceItemManager();

}
