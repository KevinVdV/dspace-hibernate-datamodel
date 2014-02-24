package org.dspace.eperson;

import org.dspace.content.DSpaceObject;
import org.dspace.core.Constants;
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
@Table(name = "epersongroup")
public class Group extends DSpaceObject {

    @Id
    @Column(name="eperson_group_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="epersongroup_seq")
    @SequenceGenerator(name="epersongroup_seq", sequenceName="epersongroup_seq")
    private Integer id;

    @Column(name="name")
    private String name;

    /** Flag set when metadata is modified, for events */
    @Transient
    private boolean modifiedMetadata;

    /** lists of epeople and groups in the group */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "epersongroup2eperson",
            joinColumns = {@JoinColumn(name = "eperson_group_id") },
            inverseJoinColumns = {@JoinColumn(name = "eperson_id") }
    )
    @CollectionId(
            columns = @Column(name="id"),
            type=@Type(type="long"),
            generator = "epersongroup2eperson_seq"
    )
    @SequenceGenerator(name="epersongroup2eperson_seq", sequenceName="epersongroup2eperson_seq", allocationSize = 1)
    private List<EPerson> epeople = new ArrayList<EPerson>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "group2group",
            joinColumns = {@JoinColumn(name = "parent_id") },
            inverseJoinColumns = {@JoinColumn(name = "child_id") }
    )
    @CollectionId(
            columns = @Column(name="id"),
            type=@Type(type="long"),
            generator = "group2group_seq"
    )
    @SequenceGenerator(name="group2group_seq", sequenceName="group2group_seq", allocationSize = 1)
    private List<Group> groups = new ArrayList<Group>();


    public Group() {
        // Cache ourselves
        //TODO: HIBERNATE CACHE CONTEXT
        //context.cache(this, row.getIntColumn("eperson_group_id"));
        modifiedMetadata = false;
        //TODO HIBERNATE: Implement the details !
        //clearDetails();
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

    @Override
    public String getHandle() {
        return null;
    }

    /**
     * get name of group
     *
     * @return name
     */
    public String getName()
    {
        return name;
    }

    @Override
    public void updateLastModified() {
        //Not required for groups
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
        //TODO HIBERNATE: Implement the details !
        //addDetails("name");
    }

    void addMember(EPerson e)
    {
        getEpeople().add(e);
    }

    void addMember(Group g)
    {
        getGroups().add(g);
    }

    boolean remove(EPerson e)
    {
        return getEpeople().remove(e);
    }

    boolean remove(Group g)
    {
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

    public List<Group> getGroups() {
        return groups;
    }

    public List<EPerson> getEpeople() {
        return epeople;
    }

    /**
     * Return Group members of a Group.
     */
    public Group[] getMemberGroups()
    {
        Group[] myArray = new Group[groups.size()];
        myArray = groups.toArray(myArray);

        return myArray;
    }

    /**
     * Return EPerson members of a Group
     */
    public EPerson[] getMembers()
    {
        EPerson[] myArray = new EPerson[epeople.size()];
        myArray = epeople.toArray(myArray);

        return myArray;
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
        hash = 59 * hash + (this.getName() != null? this.getName().hashCode():0);
        return hash;
    }

    public boolean isModifiedMetadata()
    {
        return modifiedMetadata;
    }

}
