package org.dspace.content.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.core.Context;
import org.dspace.eperson.Group;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.MissingResourceException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 17/03/14
 * Time: 13:48
 */
public interface CommunityService extends DSpaceObjectService<Community> {

    public Community find(Context context, int id) throws SQLException;

    public Community create(Context context, Community parent) throws SQLException, AuthorizeException;

    public Community create(Context context, Community parent, String handle) throws SQLException, AuthorizeException;

    public List<Community> findAll(Context context) throws SQLException;

    public List<Community> findAllTop(Context context) throws SQLException;

    public Community findByAdminGroup(Context context, Group group) throws SQLException;

    public void setName(Community community, String value)throws MissingResourceException;

    public Bitstream createLogo(Context context, Community community, InputStream is) throws AuthorizeException, IOException, SQLException;

    public Group createAdministrators(Context context, Community community) throws SQLException, AuthorizeException;

    public void removeAdministrators(Context context, Community community) throws SQLException, AuthorizeException;

    public List<Community> getAllParents(Context context, Community community) throws SQLException;

    public List<Collection> getAllCollections(Community community) throws SQLException;

    public void addCollection(Context context, Community community, Collection collection) throws SQLException,
            AuthorizeException;

    public Community createSubcommunity(Context context, Community parentCommunity) throws SQLException,
            AuthorizeException;

    public Community createSubcommunity(Context context, Community parentCommunity, String handle) throws SQLException,
            AuthorizeException;

    public void addSubcommunity(Context context, Community parentCommunity, Community childCommunity) throws SQLException, AuthorizeException;

    public void removeCollection(Context context, Community community, Collection c) throws SQLException,
            AuthorizeException, IOException;

    public void removeSubcommunity(Context context, Community parentCommunity, Community childCommunity) throws SQLException,
            AuthorizeException, IOException;

    public void delete(Context context, Community community) throws SQLException, AuthorizeException, IOException;

    public boolean canEditBoolean(Context context, Community community) throws java.sql.SQLException;

    public void canEdit(Context context, Community community) throws AuthorizeException, SQLException;
}
