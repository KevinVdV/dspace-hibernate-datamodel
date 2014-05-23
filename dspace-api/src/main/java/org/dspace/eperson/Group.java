package org.dspace.eperson;

import org.dspace.content.DSpaceObject;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.service.GroupService;
import org.dspace.factory.DSpaceServiceFactory;
import org.hibernate.annotations.CollectionId;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 15/02/14
 * Time: 17:34
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name = "epersongroup", schema = "public" )
public class Group extends DSpaceObject {

    /**
     * Initial value is set to 2 since 0 & 1 are reserved for anonymous & administrative uses
     */
    @Id
    @Column(name="eperson_group_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="epersongroup_seq")
    @SequenceGenerator(name="epersongroup_seq", sequenceName="epersongroup_seq", initialValue = 2)
    private Integer id;

    @Column(name="name")
    private String name;

    /** Flag set when metadata is modified, for events */
    @Transient
    private boolean modifiedMetadata = false;

    /** Flag set when group parent or children are changed */
    @Transient
    private boolean groupsChanged;


    /** lists of epeople and groups in the group */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "epersongroup2eperson",
            joinColumns = {@JoinColumn(name = "eperson_group_id") },
            inverseJoinColumns = {@JoinColumn(name = "eperson_id") }
    )
    private List<EPerson> epeople = new ArrayList<EPerson>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "group2group",
            joinColumns = {@JoinColumn(name = "parent_id") },
            inverseJoinColumns = {@JoinColumn(name = "child_id") }
    )
    private List<Group> groups = new ArrayList<Group>();


    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "groups")
    private List<Group> parentGroups = new ArrayList<Group>();


    @Transient
    private GroupService groupService = DSpaceServiceFactory.getInstance().getGroupService();

    public Group() {
    }

    /**
     * get the ID of the group object
     *
     * @return id
     */
    public int getID()
    {
        return id;
    }

    /**
     * get name of group
     *
     * @return name
     */
    String getNameInternal()
    {
        return name;
    }

    @Override
    public int getType() {
        return Constants.GROUP;
    }

    /**
     * set name of group
     *
     * @param name
     *            new group name
     */
    public void setName(String name)
    {
        this.name = name;
        modifiedMetadata = true;
        addDetails("name");
    }

    void addMember(EPerson e)
    {
        getEpeople().add(e);
    }

    void addMember(Group g)
    {
        getGroups().add(g);
        groupsChanged = true;
    }

    void addParentGroup(Group group)
    {
        getParentGroups().add(group);
        groupsChanged = true;
    }

    void removeParentGroup(Group group)
    {
        getParentGroups().remove(group);
        groupsChanged = true;
    }

    boolean remove(EPerson e)
    {
        return getEpeople().remove(e);
    }

    boolean remove(Group g)
    {
        groupsChanged = true;
        return getGroups().remove(g);
    }

    boolean contains(Group g)
    {
        return getGroups().contains(g);
    }

    boolean contains(EPerson e)
    {
        return getEpeople().contains(e);
    }

    /**
     * Return Group members of a Group.
     */
    public List<Group> getGroups() {
        return groups;
    }

    List<Group> getParentGroups() {
        return parentGroups;
    }

    /**
     * Return EPerson members of a Group
     */
    public List<EPerson> getEpeople() {
        return epeople;
    }

    /**
     * Return <code>true</code> if <code>other</code> is the same Group as
     * this object, <code>false</code> otherwise
     *
     * @param obj
     *            object to compare to
     *
     * @return <code>true</code> if object passed in represents the same group
     *         as this object
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final Group other = (Group) obj;
        if(this.getID() != other.getID())
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 59 * hash + this.getID();
        hash = 59 * hash + (this.getNameInternal() != null? this.getNameInternal().hashCode():0);
        return hash;
    }

    public boolean isModifiedMetadata()
    {
        return modifiedMetadata;
    }


    /*
        Getters & setters which should be removed on the long run, they are just here to provide all getters & setters to the item object
    */



    public String getName()
    {
        return groupService.getName(this);
    }

    public boolean isGroupsChanged() {
        return groupsChanged;
    }

    public void clearGroupsChanged() {
        this.groupsChanged = false;
    }
}
