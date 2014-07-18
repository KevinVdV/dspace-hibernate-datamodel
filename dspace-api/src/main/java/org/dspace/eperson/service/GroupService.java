package org.dspace.eperson.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.service.DSpaceObjectService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 31/03/14
 * Time: 13:07
 */
public interface GroupService extends DSpaceObjectService<Group> {

        // findAll sortby types
    public static final int ID = 0; // sort by ID

    public static final int NAME = 1; // sort by NAME (default)


    public Group create(Context context) throws SQLException, AuthorizeException;

    public void addMember(Context context, Group group, EPerson e);

    public void addMember(Context context, Group groupParent, Group groupChild);

    public void removeMember(Context context, Group owningGroup, EPerson childPerson);

    public void removeMember(Context context, Group owningGroup, Group childGroup);

    public boolean isDirectMember(Group group, EPerson e);

    public boolean isDirectMember(Group owningGroup, Group childGroup);

    public boolean isMember(Context c, Group group) throws SQLException;

    public boolean isMember(Context c, UUID groupid) throws SQLException;

    public List<Group> allMemberGroups(Context c, EPerson e) throws SQLException;

    public List<EPerson> allMembers(Context c, Group group) throws SQLException;

    public Group findByName(Context context, String name) throws SQLException;

    public List<Group> findAll(Context context, int sortField) throws SQLException;

    public List<Group> search(Context context, String query, int offset, int limit) throws SQLException;

    public int searchResultCount(Context context, String query) throws SQLException;

    public void delete(Context context, Group group) throws SQLException, AuthorizeException;

    public boolean isEmpty(Group group);
}
