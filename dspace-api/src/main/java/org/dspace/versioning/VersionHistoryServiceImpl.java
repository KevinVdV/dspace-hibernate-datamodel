package org.dspace.versioning;

import org.apache.commons.collections.CollectionUtils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.versioning.dao.VersionHistoryDAO;
import org.dspace.versioning.service.VersionHistoryService;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 8/05/14
 * Time: 10:59
 */
public class VersionHistoryServiceImpl implements VersionHistoryService {

    @Autowired(required = true)
    protected VersionHistoryDAO versionHistoryDAO;

    // LIST order: descending
    public Version getPrevious(VersionHistory versionHistory, Version version) {
        List<Version> versions = versionHistory.getVersions();
        int index = versions.indexOf(version);

        if( (index+1)==versions.size()) return null;

        return versions.get(index+1);
    }

    // LIST order: descending
    public Version getNext(VersionHistory versionHistory, Version version)
    {
        List<Version> versions = versionHistory.getVersions();
        int index = versions.indexOf(version);
        if(index==0)
        {
            return null;
        }
        return versions.get(index-1);
    }

    public Version getVersion(VersionHistory versionHistory, Item item) {
        List<Version> versions = versionHistory.getVersions();
        for(Version v : versions)
        {
           if(v.getItem().getID()==item.getID())
           {
               return v;
           }
        }
        return null;
    }

    public boolean hasNext(VersionHistory versionHistory, Item item)
    {
        Version version = getVersion(versionHistory, item);
        return hasNext(versionHistory, version);
    }

    public boolean hasNext(VersionHistory versionHistory, Version version)
    {
        return getNext(versionHistory, version)!=null;
    }

    public void add(VersionHistory versionHistory, Version version)
    {
        versionHistory.addVersionAtStart(version);
    }

    @Override
    public VersionHistory create(Context context) throws SQLException {
        return versionHistoryDAO.create(context, new VersionHistory());
    }

    @Override
    public VersionHistory find(Context context, int id) throws SQLException {
        return versionHistoryDAO.findByID(context, VersionHistory.class, id);
    }

    @Override
    public void update(Context context, VersionHistory versionHistory) throws SQLException, AuthorizeException {
        versionHistoryDAO.save(context, versionHistory);
    }

    @Override
    public void delete(Context context, VersionHistory versionHistory) throws SQLException {
        versionHistoryDAO.delete(context, new VersionHistory());

    }

    public Version getLatestVersion(VersionHistory versionHistory)
    {
        List<Version> versions = versionHistory.getVersions();
        if(CollectionUtils.isEmpty(versions))
        {
            return null;
        }

        return versions.iterator().next();
    }

    public Version getFirstVersion(VersionHistory versionHistory)
    {
        List<Version> versions = versionHistory.getVersions();
        if(CollectionUtils.isEmpty(versions))
        {
            return null;
        }

        return versions.get(versions.size()-1);
    }


    public boolean isFirstVersion(VersionHistory versionHistory, Version version)
    {
        List<Version> versions = versionHistory.getVersions();
        Version first = versions.get(versions.size()-1);
        return first.equals(version);
    }

    public boolean isLastVersion(VersionHistory versionHistory, Version version)
    {
        List<Version> versions = versionHistory.getVersions();
        if(CollectionUtils.isEmpty(versions))
        {
            return false;
        }
        Version last = versions.iterator().next();
        return last.equals(version);
    }

    public void remove(VersionHistory versionHistory, Version version)
    {
        versionHistory.removeVersion(version);
    }

    @Override
    public VersionHistory findByItem(Context context, Item item) throws SQLException {
        return versionHistoryDAO.findByItem(context, item);
    }
}
