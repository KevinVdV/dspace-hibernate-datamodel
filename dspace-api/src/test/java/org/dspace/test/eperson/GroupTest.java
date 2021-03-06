package org.dspace.test.eperson;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.dspace.AbstractUnitTest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.EPersonDeletionException;
import org.dspace.eperson.Group;
import org.dspace.eperson.service.GroupService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.*;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 16/05/14
 * Time: 15:43
 */
public class GroupTest extends AbstractUnitTest {

    private static final Logger log = Logger.getLogger(GroupTest.class);

    //TODO: test duplicate names ?

    private Group topGroup;
    private Group level1Group;
    private Group level2Group;


    /**
     * This method will be run before every test as per @Before. It will
     * initialize resources required for the tests.
     *
     * Other methods can be annotated with @Before here or in subclasses
     * but no execution order is guaranteed
     */
    @Before
    @Override
    public void init() {
        super.init();
        try {
            //Only admins can perform group operations, so add as default user
            context.setCurrentUser(admin);

            topGroup = createGroup("topGroup");
            level1Group = createGroup("level1Group");
            groupService.addMember(context, topGroup, level1Group);
            level2Group = createGroup("level2Group");
            groupService.addMember(context, level1Group, level2Group);

            groupService.update(context,  topGroup);
            groupService.update(context,  level1Group);
            groupService.update(context,  level2Group);
            context.commit();


        }catch(SQLException ex)
        {
            log.error("SQL Error in init", ex);
            fail("SQL Error in init: " + ex.getMessage());
        } catch (AuthorizeException ex) {
            log.error("Authorization Error in init", ex);
            fail("Authorization Error in init: " + ex.getMessage());
        }
    }

    @After
    @Override
    public void destroy() throws SQLException, AuthorizeException {
        context.commit();
        context.turnOffAuthorisationSystem();
        if(level1Group != null)
        {
            groupService.delete(context, level1Group);
            level1Group = null;
        }
        if(level2Group != null)
        {
            groupService.delete(context,level2Group);
            level2Group = null;
        }
        if(topGroup != null)
        {
            groupService.delete(context,topGroup);
            topGroup = null;
        }
        context.restoreAuthSystemState();
        context.commit();
    }

    @Test
    public void createGroup() throws SQLException, AuthorizeException {
        Group group = null;
        try {
            group = groupService.create(context);
            assertThat("testCreateGroup", group, notNullValue());
        } finally {
            if(group != null)
            {
                groupService.delete(context, group);
            }
        }
    }

    @Test(expected = AuthorizeException.class)
    public void createGroupUnAuthorized() throws SQLException, AuthorizeException {
        context.setCurrentUser(null);
        groupService.create(context);
    }

    @Test
    public void setGroupName() throws SQLException, AuthorizeException {
        topGroup.setName("new name");
        groupService.update(context, topGroup);
        assertThat("setGroupName 1", topGroup.getName(), notNullValue());
        assertEquals("setGroupName 2", topGroup.getName(), "new name");
    }

    @Test
    public void findByName() throws SQLException {
        Group group = groupService.findByName(context, "topGroup");
        assertThat("findByName 1", group, notNullValue());
        assertThat("findByName 2", group.getName(), notNullValue());
        assertEquals("findByName 2", group.getName(), "topGroup");
    }

    @Test
    public void findAll() throws SQLException {
        List<Group> groups = groupService.findAll(context, GroupService.NAME);
        assertThat("findAll 1", groups, notNullValue());
        //We should find 5 groups: anonymous, admin & our 3 test groups
        assertEquals("findAll 2", groups.size(), 5);
    }

    @Test
    public void findAllIdSort() throws SQLException {
        List<Group> groups = groupService.findAll(context, GroupService.ID);

        assertThat("findAllIdSort 1", groups, notNullValue());

        //Check our sorting order by adding to a treeSet & check against arraylist values
        List<String> listNames = new ArrayList<String>();
        Set<String> setNames = new TreeSet<String>();
        for (Group group : groups) {
            listNames.add(group.getID().toString());
            setNames.add(group.getID().toString());
        }
        assertTrue("findAllIdSort 2 ", ArrayUtils.isEquals(setNames.toArray(new String[setNames.size()]), listNames.toArray(new String[listNames.size()])));
    }


    @Test
    public void findAllNameSort() throws SQLException {
        List<Group> groups = groupService.findAll(context, GroupService.NAME);

        assertThat("findAllNameSort 1", groups, notNullValue());

        //Check our sorting order by adding to a treeSet & check against arraylist values
        List<String> listNames = new ArrayList<String>();
        Set<String> setNames = new TreeSet<String>();
        for (Group group : groups) {
            listNames.add(group.getName());
            setNames.add(group.getName());
        }
        assertTrue("findAllNameSort 2 ", ArrayUtils.isEquals(setNames.toArray(new String[setNames.size()]), listNames.toArray(new String[listNames.size()])));
    }

    @Test
    public void searchByName() throws SQLException {
        //We can find 2 groups so attempt to retrieve with offset 0 and a max of one
        List<Group> groups = groupService.search(context, "level", 0, 1);
        assertThat("search 1", groups, notNullValue());
        assertEquals("search 2", groups.size(), 1);
        String firstGroupName = groups.iterator().next().getName();
        assertTrue("search 3", firstGroupName.equals("level1Group") || firstGroupName.equals("level2Group"));

        //Retrieve the second group
        groups = groupService.search(context, "level", 1, 2);
        assertThat("search 1", groups, notNullValue());
        assertEquals("search 2", groups.size(), 1);
        String secondGroupName = groups.iterator().next().getName();
        assertTrue("search 3", secondGroupName.equals("level1Group") || secondGroupName.equals("level2Group"));
    }

    @Test
    public void searchByID() throws SQLException
    {
        List<Group> searchResult = groupService.search(context, String.valueOf(topGroup.getID()), 0, 10);
        assertEquals("searchID 1", searchResult.size(), 1);
        assertEquals("searchID 2", searchResult.iterator().next(), topGroup);
    }


    @Test
    public void searchResultCount() throws SQLException {
        assertEquals("searchResultCount", groupService.searchResultCount(context, "level"), 2);
    }

    @Test
    public void addMemberEPerson() throws SQLException, AuthorizeException, EPersonDeletionException {
        EPerson ePerson =  null;
        try {
            ePerson = createEPersonAndAddToGroup("addMemberEPerson@dspace.org", topGroup);
            groupService.update(context, topGroup);

            assertEquals("addMemberEPerson 1", topGroup.getEpeople().size(), 1);
            assertTrue("addMemberEPerson 2", topGroup.getEpeople().contains(ePerson));
        } finally {
            if(ePerson != null)
            {
                ePersonService.delete(context, ePerson);
            }
        }
    }

    @Test
    public void addMemberGroup() throws SQLException, AuthorizeException, EPersonDeletionException {
        Group parentGroup = createGroup("parentGroup");
        Group childGroup = createGroup("childGroup");
        groupService.addMember(context, parentGroup, childGroup);
        groupService.update(context, parentGroup);
        groupService.update(context, childGroup);
        groupService.delete(context, parentGroup);
        groupService.delete(context, childGroup);
    }


    @Test
    public void deleteGroupEPersonMembers() throws SQLException, AuthorizeException, EPersonDeletionException {
        EPerson ePerson =  null;
        try {
            Group toDeleteGroup = createGroup("toDelete");
            ePerson = createEPerson("deleteGroupEPersonMembers@dspace.org");
            groupService.addMember(context, toDeleteGroup, ePerson);
            groupService.update(context, toDeleteGroup);
            groupService.delete(context, toDeleteGroup);
            assertEquals("deleteGroupEPersonMembers", ePerson.getGroups().size(), 0);
        } finally {
            if(ePerson != null)
            {
                ePersonService.delete(context, ePerson);
            }
        }
    }

    @Test
    public void deleteGroupGroupMembers() throws SQLException, AuthorizeException, EPersonDeletionException {
        //Delete parent first
        Group parentGroup = createGroup("toDeleteParent");
        Group childGroup = createGroup("toDeleteChild");
        groupService.addMember(context, parentGroup, childGroup);
        groupService.update(context, parentGroup);
        groupService.update(context, childGroup);
        groupService.delete(context, parentGroup);
        groupService.delete(context, childGroup);

        //Delete child first
        parentGroup = createGroup("toDeleteParent");
        childGroup = createGroup("toDeleteChild");
        groupService.addMember(context, parentGroup, childGroup);
        groupService.update(context, parentGroup);
        groupService.update(context, childGroup);
        groupService.delete(context, childGroup);
        groupService.delete(context, parentGroup);
    }

    @Test
    public void isMemberGroup() throws SQLException
    {
        assertTrue("isMemberGroup 1", groupService.isDirectMember(topGroup, level1Group));
        assertTrue("isMemberGroup 2", groupService.isDirectMember(level1Group, level2Group));
        assertFalse("isMemberGroup 3", groupService.isDirectMember(level1Group, topGroup));
        assertFalse("isMemberGroup 4", groupService.isDirectMember(level2Group, level1Group));
    }

    @Test
    public void isMemberEPerson() throws SQLException, AuthorizeException, EPersonDeletionException {
        EPerson ePerson = null;
        try {
            ePerson = createEPersonAndAddToGroup("isMemberEPerson@dspace.org", level1Group);
            assertTrue(groupService.isDirectMember(level1Group, ePerson));
            assertFalse(groupService.isDirectMember(topGroup, ePerson));
        } finally {
            if(ePerson != null)
            {
                ePersonService.delete(context, ePerson);
            }
        }
    }

    @Test
    public void isMemberContext() throws SQLException, AuthorizeException, EPersonDeletionException {
        EPerson ePerson = null;
        try {
            ePerson = createEPersonAndAddToGroup("isMemberContext@dspace.org", level2Group);

            context.setCurrentUser(ePerson);
            assertTrue(groupService.isMember(context, topGroup));
            assertTrue(groupService.isMember(context, level1Group));
            assertTrue(groupService.isMember(context, level2Group));
        } finally {
            if(ePerson != null)
            {
                context.setCurrentUser(admin);
                ePersonService.delete(context, ePerson);
            }
        }
    }

    @Test
    public void isMemberContextGroupId() throws SQLException, AuthorizeException, EPersonDeletionException {
        EPerson ePerson = null;
        try {
            ePerson = createEPersonAndAddToGroup("isMemberContextGroupId@dspace.org", level2Group);

            context.setCurrentUser(ePerson);
            assertTrue(groupService.isMember(context, topGroup.getID()));
            assertTrue(groupService.isMember(context, level1Group.getID()));
            assertTrue(groupService.isMember(context, level2Group.getID()));
        } finally {
            if(ePerson != null)
            {
                context.setCurrentUser(admin);
                ePersonService.delete(context, ePerson);
            }
        }
    }

    @Test
    public void removeMemberEPerson() throws SQLException, AuthorizeException, EPersonDeletionException {
        EPerson ePerson = null;
        try {
            // Test normal behavior, add user to group & remove
            ePerson = createEPersonAndAddToGroup("removeMemberEPerson@dspace.org", level2Group);
            context.setCurrentUser(ePerson);
            assertTrue(groupService.isMember(context, topGroup.getID()));
            assertTrue(groupService.isMember(context, level1Group.getID()));
            assertTrue(groupService.isMember(context, level2Group.getID()));
            groupService.removeMember(context, level2Group, ePerson);
            assertFalse(groupService.isMember(context, topGroup.getID()));
            assertFalse(groupService.isMember(context, level1Group.getID()));
            assertFalse(groupService.isMember(context, level2Group.getID()));


            //Test non recursive removal, if not a member do not add
            groupService.addMember(context, level2Group, ePerson);
            assertTrue(groupService.isMember(context, topGroup.getID()));
            assertTrue(groupService.isMember(context, level1Group.getID()));
            assertTrue(groupService.isMember(context, level2Group.getID()));
            groupService.removeMember(context, topGroup, ePerson);
            assertTrue(groupService.isMember(context, topGroup.getID()));
            assertTrue(groupService.isMember(context, level1Group.getID()));
            assertTrue(groupService.isMember(context, level2Group.getID()));
        } finally {
            if(ePerson != null)
            {
                context.setCurrentUser(admin);
                ePersonService.delete(context, ePerson);
            }
        }
    }

    @Test
    public void removeMemberGroup() throws SQLException {
        assertTrue(groupService.isDirectMember(topGroup, level1Group));
        groupService.removeMember(context, topGroup, level1Group);
        assertFalse(groupService.isDirectMember(topGroup, level1Group));
    }

    @Test
    public void allMemberGroups() throws SQLException, AuthorizeException, EPersonDeletionException {
        EPerson ePerson = createEPersonAndAddToGroup("allMemberGroups@dspace.org", level1Group);
        try {
            assertTrue(groupService.allMemberGroups(context, ePerson).containsAll(Arrays.asList(topGroup, level1Group)));
        } finally {
            ePersonService.delete(context, ePerson);
        }

    }

    @Test
    public void allMembers() throws SQLException, AuthorizeException, EPersonDeletionException {
        List<EPerson> allEPeopleAdded = new ArrayList<EPerson>();
        try {
            allEPeopleAdded.add(createEPersonAndAddToGroup("allMemberGroups1@dspace.org", topGroup));
            allEPeopleAdded.add(createEPersonAndAddToGroup("allMemberGroups2@dspace.org", level1Group));
            allEPeopleAdded.add(createEPersonAndAddToGroup("allMemberGroups3@dspace.org", level2Group));

            assertTrue(groupService.allMembers(context, topGroup).containsAll(allEPeopleAdded));
            assertTrue(groupService.allMembers(context, level1Group).containsAll(allEPeopleAdded.subList(1, 2)));
            assertTrue(groupService.allMembers(context, level2Group).containsAll(allEPeopleAdded.subList(2, 2)));
        } finally {
            //Remove all the people added
            for (EPerson ePerson : allEPeopleAdded) {
                ePersonService.delete(context, ePerson);
            }
        }
    }

    @Test
    public void isEmpty() throws SQLException, AuthorizeException, EPersonDeletionException {
        assertTrue(groupService.isEmpty(topGroup));
        assertTrue(groupService.isEmpty(level1Group));
        assertTrue(groupService.isEmpty(level2Group));

        EPerson person = createEPersonAndAddToGroup("isEmpty@dspace.org", level2Group);
        assertFalse(groupService.isEmpty(topGroup));
        assertFalse(groupService.isEmpty(level1Group));
        assertFalse(groupService.isEmpty(level2Group));
        ePersonService.delete(context, person);
        assertTrue(groupService.isEmpty(level2Group));
    }



    protected Group createGroup(String name) throws SQLException, AuthorizeException {
        Group group = groupService.create(context);
        group.setName(name);
        groupService.update(context, group);
        return group;
    }

}
