package org.dspace.content;

import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;

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
    @SequenceGenerator(name="workspace_item_seq", sequenceName="workspace_item_seq")
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
    private Boolean multipleTitles;

    @Column(name = "published_before")
    private Boolean publishedBefore = false;

    @Column(name = "multiple_files")
    private Boolean multipleFiles = false;

    @Column(name = "stage_reached")
    private Integer stageReached;

    @Column(name = "page_reached")
    private Integer pageReached;

    public WorkspaceItem() {
    }

    /**
     * Get the internal ID of this workspace item
     *
     * @return the internal identifier
     */
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

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    public boolean hasMultipleTitles() {
        return multipleTitles;
    }

    public void setMultipleTitles(boolean multipleTitles) {
        this.multipleTitles = multipleTitles;
    }

    public boolean getPublishedBefore() {
        return publishedBefore;
    }

    public void setPublishedBefore(boolean publishedBefore) {
        this.publishedBefore = publishedBefore;
    }

    public boolean hasMultipleFiles() {
        return multipleFiles;
    }



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
}
