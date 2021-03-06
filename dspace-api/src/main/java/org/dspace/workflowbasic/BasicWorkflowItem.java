package org.dspace.workflowbasic;

import org.dspace.content.Collection;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;
import org.dspace.eperson.EPerson;
import org.dspace.workflow.WorkflowItem;

import javax.persistence.*;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 7/04/14
 * Time: 15:53
 */
@Entity
@Table(name = "workflowitem", schema = "public")
public class BasicWorkflowItem implements WorkflowItem {

    @Id
    @Column(name = "workflow_id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="workflowitem_seq")
    @SequenceGenerator(name="workflowitem_seq", sequenceName="workflowitem_seq", allocationSize = 1)
    private int workflowitemId;

    /** The item this workflowitem object pertains to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", unique = true)
    private Item item;

    /** The collection the item is being submitted to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    private Collection collection;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner")
    private EPerson owner;

    @Column(name = "state")
    private int state;

    @Column(name = "multiple_titles")
    private boolean multipleTitles = false;

    @Column(name = "published_before")
    private boolean publishedBefore = false;

    @Column(name = "multiple_files")
    private boolean multipleFiles = false;


    /**
     * Get the internal ID of this workflow item
     *
     * @return the internal identifier
     */
    @Override
    public int getID() {
        return workflowitemId;
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
        return getItem().getSubmitter();
    }

    void setCollection(Collection collection) {
        this.collection = collection;
    }

    /**
     * get owner of WorkflowItem
     *
     * @return EPerson owner
     */
    public EPerson getOwner() {
        return owner;
    }

    /**
     * set owner of WorkflowItem
     *
     * @param owner
     *            owner
     */
    public void setOwner(EPerson owner) {
        this.owner = owner;
    }

    /**
     * Get state of WorkflowItem
     *
     * @return state
     */
    public int getState() {
        return state;
    }

    /**
     * Set state of WorkflowItem
     *
     * @param state
     *            new state (from <code>WorkflowManager</code>)
     */
    public void setState(int state) {
        this.state = state;
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
}

