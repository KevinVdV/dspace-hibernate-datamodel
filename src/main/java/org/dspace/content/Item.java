package org.dspace.content;

import org.dspace.core.Constants;
import org.dspace.eperson.EPerson;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CollectionId;
import org.hibernate.annotations.Type;

import org.dspace.content.Collection;
import javax.persistence.*;
import java.sql.SQLException;
import java.util.*;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 26/02/14
 * Time: 09:47
 */
@Entity
@Table(name="item")
public class Item extends DSpaceObject{

    /**
     * Wild card for Dublin Core metadata qualifiers/languages
     */
    public static final String ANY = "*";

    @Id
    @Column(name="item_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="item_seq")
    @SequenceGenerator(name="item_seq", sequenceName="item_seq")
    private Integer id;

    @Column(name= "in_archive")
    private boolean inArchive = false;

    @Column(name= "discoverable")
    private boolean discoverable = false;

    @Column(name= "withdrawn")
    private boolean withdrawn = false;

    @Column(name= "last_modified")
    private Date lastModified = new Date();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owning_collection")
    private Collection owningCollection;

    /** The e-person who submitted this item */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitter_id")
    private EPerson submitter = null;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "collection2item",
            joinColumns = {@JoinColumn(name = "item_id") },
            inverseJoinColumns = {@JoinColumn(name = "collection_id") }
    )
    @CollectionId(
            columns = @Column(name="id"),
            type=@Type(type="integer"),
            generator = "collection2item_seq"
    )
    @SequenceGenerator(name="collection2item_seq", sequenceName="collection2item_seq", allocationSize = 1)
    private Set<Collection> collections = new LinkedHashSet<Collection>();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "item2bundle",
            joinColumns = {@JoinColumn(name = "item_id") },
            inverseJoinColumns = {@JoinColumn(name = "bundle_id") }
    )
    @CollectionId(
            columns = @Column(name="id"),
            type=@Type(type="integer"),
            generator = "item2bundle_seq"
    )
    @SequenceGenerator(name="item2bundle_seq", sequenceName="item2bundle_seq", allocationSize = 1)
    private List<Bundle> bundles = null;


    //TODO: Change to onToMany or ManyToOne ?
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name="metadatavalue", joinColumns=@JoinColumn(name="item_id"))
        @CollectionId(
                columns = @Column(name="metadata_value_id"),
                type=@Type(type="integer"),
                generator = "metadatavalue_seq"
        )
    @SequenceGenerator(name="metadatavalue_seq", sequenceName="metadatavalue_seq", allocationSize = 1)
    //TODO: HIBERNATE, ORDER BY:  metadata_field_id, place
    private List<MetadataValue> metadata;



    /**
     * True if the Dublin Core has changed since reading from the DB or the last
     * update()
     */
    @Transient
    private boolean dublinCoreChanged = false;

    /**
     * True if anything else was changed since last update()
     * (to drive event mechanism)
     */
    @Transient
    private boolean modified = false;




    /**
     * Return <code>true</code> if <code>other</code> is the same Item as
     * this object, <code>false</code> otherwise
     *
     * @param obj
     *            object to compare to
     * @return <code>true</code> if object passed in represents the same item
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
         final Item other = (Item) obj;
         if (this.getType() != other.getType())
         {
             return false;
         }
         if (this.getID() != other.getID())
         {
             return false;
         }

         return true;
     }

     @Override
     public int hashCode()
     {
         int hash = 5;
         hash += 71 * hash + getType();
         hash += 71 * hash + getID();
         return hash;
     }


    /**
     * Get the internal ID of this item. In general, this shouldn't be exposed
     * to users
     *
     * @return the internal identifier
     */
    public int getID()
    {
        return id;
    }

    /**
     * return type found in Constants
     *
     * @return int Constants.ITEM
     */
    public int getType()
    {
        return Constants.ITEM;
    }

    /**
     * Find out if the item has been withdrawn
     *
     * @return true if the item has been withdrawn
     */
    public boolean isWithdrawn() {
        return withdrawn;
    }


    /**
     * Find out if the item is discoverable
     *
     * @return true if the item is discoverable
     */
    void setWithdrawn(boolean withdrawn) {
        this.withdrawn = withdrawn;
    }

    /**
     * Find out if the item is part of the main archive
     *
     * @return true if the item is in the main archive
     */
    public boolean isArchived() {
        return inArchive;
    }

    /**
     * Set the "is_archived" flag. This is public and only
     * <code>WorkflowItem.archive()</code> should set this.
     *
     * @param isArchived
     *            new value for the flag
     */
    public void setInArchive(boolean isArchived) {
        this.inArchive = isArchived;
        modified = true;

    }

    /**
     * Set the "discoverable" flag. This is public and only
     *
     * @param discoverable
     *            new value for the flag
     */
    public void setDiscoverable(boolean discoverable)
    {
        this.discoverable = discoverable;
        modified = true;
    }

    /**
     * Get the date the item was last modified, or the current date if
     * last_modified is null
     *
     * @return the date the item was last modified, or the current date if the
     *         column is null.
     */
    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Get the owning Collection for the item
     *
     * @return Collection that is the owner of the item
     * @throws java.sql.SQLException
     */
    public Collection getOwningCollection() {
        return owningCollection;
    }

    /**
     * Set the owning Collection for the item
     *
     * @param owningCollection
     *            Collection
     */
    public void setOwningCollection(Collection owningCollection) {
        this.owningCollection = owningCollection;
        modified = true;
    }
    /**
     * Get the e-person that originally submitted this item
     *
     * @return the submitter
     */
    public EPerson getSubmitter() throws SQLException
    {
        return submitter;
    }

    /**
     * Set the e-person that originally submitted this item. This is a public
     * method since it is handled by the WorkspaceItem class in the ingest
     * package. <code>update</code> must be called to write the change to the
     * database.
     *
     * @param sub
     *            the submitter
     */
    public void setSubmitter(EPerson sub)
    {
        this.submitter = sub;
        modified = true;
    }

    /**
     * Get the collections this item is in. The order is indeterminate.
     *
     * @return the collections this item is in, if any.
     * @throws SQLException
     */
    public Set<Collection> getCollections(){
        return collections;
    }

    public List<MetadataValue> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<MetadataValue> metadata) {
        if(!this.metadata.equals(metadata))
        {
            dublinCoreChanged = true;
        }
        this.metadata = metadata;
    }

    public List<Bundle> getBundles() {
        return bundles;
    }

    public void addBundle(Bundle bundle)
    {
        bundles.add(bundle);
    }

    public void removeBundle(Bundle bundle)
    {
        bundles.remove(bundle);
    }

    public void addCollection(Collection collection)
    {
        getCollections().add(collection);
    }

    public void removeCollection(Collection collection)
    {
        getCollections().remove(collection);
    }
}

