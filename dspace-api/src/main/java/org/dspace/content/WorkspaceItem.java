package org.dspace.content;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 08/03/14
 * Time: 10:18
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name = "workspaceitem", schema = "public")
public class WorkspaceItem implements InProgressSubmission{

    @Id
    @Column(name = "workspace_item_id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="workspace_item_seq")
    @SequenceGenerator(name="workspace_item_seq", sequenceName="workspace_item_seq", allocationSize = 1)
    private int workspaceItemId;

    /** The item this workspace object pertains to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    /** The collection the item is being submitted to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    private Collection collection;

    @Column(name = "multiple_titles")
    private boolean multipleTitles = false;

    @Column(name = "published_before")
    private boolean publishedBefore = false;

    @Column(name = "multiple_files")
    private boolean multipleFiles = false;

    @Column(name = "stage_reached")
    private Integer stageReached;

    @Column(name = "page_reached")
    private Integer pageReached;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "epersongroup2workspaceitem",
            joinColumns = {@JoinColumn(name = "workspace_item_id") },
            inverseJoinColumns = {@JoinColumn(name = "eperson_group_id") }
    )
    private List<Group> supervisorGroups = new ArrayList<Group>();

    public WorkspaceItem() {
    }

    /**
     * Get the internal ID of this workspace item
     *
     * @return the internal identifier
     */
    @Override
    public int getID() {
        return workspaceItemId;
    }

    /**
     * Decide if this WorkspaceItem is equal to another
     *
     * @param o The other workspace item to compare to
     * @return If they are equal or not
     */
    public boolean equals(Object o) {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final WorkspaceItem that = (WorkspaceItem)o;
        if (this.getID() != that.getID())
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return new HashCodeBuilder().append(getID()).toHashCode();
    }

    @Override
    public Item getItem() {
        return item;
    }

    void setItem(Item item) {
        this.item = item;
    }

    @Override
    public Collection getCollection() {
        return collection;
    }

    @Override
    public EPerson getSubmitter() {
        return item.getSubmitter();
    }

    void setCollection(Collection collection) {
        this.collection = collection;
    }

    @Override
    public boolean hasMultipleTitles() {
        return multipleTitles;
    }

    @Override
    public void setMultipleTitles(boolean multipleTitles) {
        this.multipleTitles = multipleTitles;
    }

    @Override
    public boolean isPublishedBefore() {
        return publishedBefore;
    }

    @Override
    public void setPublishedBefore(boolean publishedBefore) {
        this.publishedBefore = publishedBefore;
    }

    @Override
    public boolean hasMultipleFiles() {
        return multipleFiles;
    }



    @Override
    public void setMultipleFiles(boolean multipleFiles) {
        this.multipleFiles = multipleFiles;
    }

    /**
     * Get the value of the stage reached column
     *
     * @return the value of the stage reached column
     */
    public Integer getStageReached() {
        return stageReached;
    }

    /**
     * Set the value of the stage reached column
     *
     * @param stageReached
     *            the value of the stage reached column
     */
    public void setStageReached(Integer stageReached) {
        this.stageReached = stageReached;
    }

    /**
     * Get the value of the page reached column (which represents the page
     * reached within a stage/step)
     *
     * @return the value of the page reached column
     */
    public Integer getPageReached() {
        return pageReached;
    }

    /**
     * Set the value of the page reached column (which represents the page
     * reached within a stage/step)
     *
     * @param pageReached
     *            the value of the page reached column
     */
    public void setPageReached(Integer pageReached) {
        this.pageReached = pageReached;
    }

    public List<Group> getSupervisorGroups() {
        return supervisorGroups;
    }

    void removeSupervisorGroup(Group group)
    {
        supervisorGroups.remove(group);
    }

    void addSupervisorGroup(Group group)
    {
        supervisorGroups.add(group);
    }
}
