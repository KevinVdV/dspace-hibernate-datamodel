package org.dspace.content;

import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.hibernate.annotations.CollectionId;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 26/02/14
 * Time: 16:02
 */
@Entity
@Table(name="bundle", schema = "public")
public class Bundle extends DSpaceObject{

    @Id
    @Column(name="bundle_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="bundle_seq")
    @SequenceGenerator(name="bundle_seq", sequenceName="bundle_seq", allocationSize = 1)
    private Integer id;

    @Column(name= "name", length = 16)
    private String name = null;


    @OneToOne
    @JoinColumn(name = "primary_bitstream_id")
    private Bitstream primaryBitstream;


    /** Flag set when data is modified, for events */
    @Transient
    private boolean modified = false;

    /** Flag set when metadata is modified, for events */
    @Transient
    private boolean modifiedMetadata = false;


    @OneToMany(mappedBy = "bundle", fetch = FetchType.LAZY)
    @OrderBy("bitstreamOrder asc")
    private List<BundleBitstream> bitstreams = new ArrayList<BundleBitstream>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "item2bundle",
            joinColumns = {@JoinColumn(name = "bundle_id") },
            inverseJoinColumns = {@JoinColumn(name = "item_id") }
    )
    private List<Item> items = null;

    /**
     * Get the internal identifier of this bundle
     *
     * @return the internal identifier
     */
    public int getID()
    {
        return id;
    }

    /**
     * return type found in Constants
     */
    public int getType()
    {
        return Constants.BUNDLE;
    }

    /**
     * Get the name of the bundle
     *
     * @return name of the bundle (ORIGINAL, TEXT, THUMBNAIL) or NULL if not set
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the bundle
     *
     * @param name
     *            string name of the bundle (ORIGINAL, TEXT, THUMBNAIL) are the
     *            values currently used
     */
    public void setName(String name) {
        this.name = name;
        modifiedMetadata = true;

    }

    /**
     * Get the primary bitstream ID of the bundle
     *
     * @return primary bitstream ID or null if not set
     */
    public Bitstream getPrimaryBitstream()
    {
        return primaryBitstream;
    }

    /**
     * Set the primary bitstream ID of the bundle
     *
     * @param bitstream
     *            int ID of primary bitstream (e.g. index html file)
     */
    public void setPrimaryBitstream(Bitstream bitstream)
    {
        this.primaryBitstream = bitstream;
        modified = true;
    }

    public boolean isModified() {
        return modified;
    }

    public void clearModified() {
        this.modified = false;
    }

    public boolean isModifiedMetadata() {
        return modifiedMetadata;
    }

    public void cleartModifiedMetadata() {
        this.modifiedMetadata = false;
    }

    void addBitstream(BundleBitstream bitstream){
        bitstreams.add(bitstream);
    }


    /**
     * Get the bitstreams in this bundle
     *
     * @return the bitstreams
     */
    public List<BundleBitstream> getBitstreams() {
        return bitstreams;
    }

    /**
     * Get the item this bundle appears in
     *
     * @return array of <code>Item</code> s this bundle appears in
     */
    public List<Item> getItems() {
        return items;
    }

    /**
     * Set the item this bundle appears in
     *
     * @return array of <code>Item</code> s this bundle appears in
     */
    void setItems(List<Item> items) {
        this.items = items;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final Bundle other = (Bundle) obj;
        if (this.getType() != other.getType())
        {
            return false;
        }
        if(this.getID() != other.getID())
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
}
