package org.dspace.versioning.service;

import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.service.DSpaceCRUDService;
import org.dspace.versioning.Version;
import org.dspace.versioning.VersionHistory;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 8/05/14
 * Time: 09:41
 */
public interface VersionHistoryService extends DSpaceCRUDService<VersionHistory>{

    public Version getLatestVersion(VersionHistory versionHistory);

    public Version getFirstVersion(VersionHistory versionHistory);

    public Version getPrevious(VersionHistory versionHistory, Version version);

    public Version getNext(VersionHistory versionHistory, Version version);

    public boolean hasNext(VersionHistory versionHistory, Version version);

    public void add(VersionHistory versionHistory, Version version);

    public Version getVersion(VersionHistory versionHistory, org.dspace.content.Item item);

    public boolean hasNext(VersionHistory versionHistory, org.dspace.content.Item item);

    public boolean isFirstVersion(VersionHistory versionHistory, Version version);

    public boolean isLastVersion(VersionHistory versionHistory, Version version);

    public void remove(VersionHistory versionHistory, Version version);

    public VersionHistory findByItem(Context context, Item item) throws SQLException;

}
