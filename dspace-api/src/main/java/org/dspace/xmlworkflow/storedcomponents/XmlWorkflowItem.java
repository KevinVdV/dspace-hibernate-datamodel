package org.dspace.xmlworkflow.storedcomponents;

import org.dspace.content.Collection;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;

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
public class XmlWorkflowItem implements InProgressSubmission{

    @Id
    @Column(name="workflowitem_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="cwf_workflowitem_seq")
    @SequenceGenerator(name="cwf_workflowitem_seq", sequenceName="cwf_workflowitem_seq", allocationSize = 1)
    private int id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    private Collection collection;

    @Column(name = "multiple_titles")
    private Boolean multipleTitles = false;

    @Column(name = "published_before")
    private Boolean publishedBefore = false;

    @Column(name = "multiple_files")
    private Boolean multipleFiles = false;

    public int getID() {
        return id;
    }

    public Item getItem() {
        return item;
    }

    void setItem(Item item) {
        this.item = item;
    }

    public Collection getCollection() {
        return collection;
    }

    void setCollection(Collection collection) {
        this.collection = collection;
    }

    public boolean hasMultipleTitles() {
        return multipleTitles;
    }

    public void setMultipleTitles(boolean multipleTitles) {
        this.multipleTitles = multipleTitles;
    }

    public boolean isPublishedBefore() {
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
}
