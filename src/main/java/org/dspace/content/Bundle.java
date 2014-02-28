package org.dspace.content;

import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.hibernate.annotations.CollectionId;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 26/02/14
 * Time: 16:02
 */
@Entity
@Table(name="bundle")
public class Bundle extends DSpaceObject{

    private static final String bitstreamOrdering  = ConfigurationManager.getProperty("webui.bitstream.order.field") + " " + ConfigurationManager.getProperty("webui.bitstream.order.direction");

    @Id
    @Column(name="bundle_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="bundle_seq")
    @SequenceGenerator(name="bundle_seq", sequenceName="bundle_seq")
    private Integer id;

    @Column(name= "name")
    private String name = null;

    @Column(name="primary_bitstream_id")
    private Integer primaryBitstreamId;


    /** Flag set when data is modified, for events */
    @Transient
    private boolean modified = false;

    /** Flag set when metadata is modified, for events */
    @Transient
    private boolean modifiedMetadata = false;


    @OneToMany(fetch = FetchType.LAZY)
    /** The bitstreams in this bundle */
    @JoinTable(
            name = "bundle2bitstream",
            joinColumns = {@JoinColumn(name = "bundle_id") },
            inverseJoinColumns = {@JoinColumn(name = "bitstream_id") }
    )
    @CollectionId(
            columns = @Column(name="id"),
            type=@Type(type="integer"),
            generator = "bundle2bitstream_seq"
    )
    @SequenceGenerator(name="bundle2bitstream_seq", sequenceName="bundle2bitstream_seq", allocationSize = 1)
    @OrderBy("sequence_id asc")
    private List<Bitstream> bitstreams = new ArrayList<Bitstream>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(
            name = "item2bundle",
            joinColumns = {@JoinColumn(name = "bundle_id") },
            inverseJoinColumns = {@JoinColumn(name = "item_id") }
    )
    @CollectionId(
            columns = @Column(name="id"),
            type=@Type(type="integer"),
            generator = "item2bundle_seq"
    )
    @SequenceGenerator(name="item2bundle_seq", sequenceName="item2bundle_seq", allocationSize = 1)
    private Item item = null;

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
     * @return primary bitstream ID or -1 if not set
     */
    public int getPrimaryBitstreamID()
    {
        return primaryBitstreamId;
    }

    /**
     * Set the primary bitstream ID of the bundle
     *
     * @param bitstreamID
     *            int ID of primary bitstream (e.g. index html file)
     */
    public void setPrimaryBitstreamID(int bitstreamID)
    {
        this.primaryBitstreamId = bitstreamID;
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

    public void addBitstream(Bitstream bitstream){
        bitstreams.add(bitstream);
    }


    /**
     * Get the bitstreams in this bundle
     *
     * @return the bitstreams
     */
    public List<Bitstream> getBitstreams() {
        return bitstreams;
    }

    /**
     * Get the item this bundle appears in
     *
     * @return array of <code>Item</code> s this bundle appears in
     */
    public Item getItem() {
        return item;
    }
}
