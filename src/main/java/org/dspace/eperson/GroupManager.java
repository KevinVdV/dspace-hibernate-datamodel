package org.dspace.eperson;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObjectManager;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 31/03/14
 * Time: 13:07
 */
public interface GroupManager extends DSpaceObjectManager<Group> {

    public Group create(Context context) throws SQLException, AuthorizeException;

    public void addMember(Context context, Group groupEntity, EPerson e);

    public void addMember(Context context, Group groupParent, Group groupChild);

    public void removeMember(Context context, Group owningGroup, EPerson childPerson);

    public void removeMember(Context context, Group owningGroup, Group childGroup);

    public boolean isMember(Group group, EPerson e);

    public boolean isMember(Group owningGroup, Group childGroup);

    public boolean isMember(Context c, Group group) throws SQLException;

    public boolean isMember(Context c, int groupid) throws SQLException;

    public List<Group> allMemberGroups(Context c, EPerson e) throws SQLException;

    public Set<Integer> allMemberGroupIDs(Context c, EPerson e) throws SQLException;

    public List<EPerson> allMembers(Context c, Group group) throws SQLException;

    public Group find(Context context, int id) throws SQLException;

    public Group findByName(Context context, String name) throws SQLException;

    public List<Group> findAll(Context context, int sortField) throws SQLException;

    public List<Group> search(Context context, String query, int offset, int limit) throws SQLException;

    public int searchResultCount(Context context, String query) throws SQLException;

    public void delete(Context context, Group groupEntity) throws SQLException;

    public boolean isEmpty(Group groupEntity);

    public void update(Context context, Group group) throws SQLException, AuthorizeException;
}
