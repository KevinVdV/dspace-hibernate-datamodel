package org.dspace.xmlworkflow.storedcomponents;

import org.dspace.content.Collection;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;
import org.dspace.eperson.EPerson;
import org.dspace.workflow.WorkflowItem;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 21/04/14
 * Time: 09:16
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name="cwf_workflowitem", schema = "public")
public class XmlWorkflowItem implements WorkflowItem {

    @Id
    @Column(name="workflowitem_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="cwf_workflowitem_seq")
    @SequenceGenerator(name="cwf_workflowitem_seq", sequenceName="cwf_workflowitem_seq", allocationSize = 1)
    private int id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", unique = true)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    private Collection collection;

    @Column(name = "multiple_titles")
    private boolean multipleTitles = false;

    @Column(name = "published_before")
    private boolean publishedBefore = false;

    @Column(name = "multiple_files")
    private boolean multipleFiles = false;

    @Override
    public int getID() {
        return id;
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
}
