package org.dspace.content;

import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Event;
import org.hibernate.annotations.CollectionId;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 27/02/14
 * Time: 09:25
 */
@Entity
@Table(name="bitstream")
public class Bitstream extends DSpaceObject{


    @Id
    @Column(name="bitstream_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="bitstream_seq")
    @SequenceGenerator(name="bitstream_seq", sequenceName="bitstream_seq")
    private Integer id;

    @Column(name = "sequence_id")
    private Integer sequenceId;

    @Column(name = "name")
    private String name;

    @Column(name = "source")
    private String source;

    @Column(name = "description")
    private String description;

    @Column(name = "checksum")
    private String checksum;

    @Column(name = "checksum_algorithm")
    private String checksumAlgorithm;

    @Column(name = "size_bytes")
    private long sizeBytes;

    @Column(name = "deleted")
    private boolean deleted;

    @Column(name = "internal_id")
    private String internalId;

    @Column(name = "store_number")
    private int storeNumber;

    @Column(name = "bitstream_order")
    private int bitstreamOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bitstream_format_id")
    private BitstreamFormat bitstreamFormat;

    @ManyToOne(fetch = FetchType.LAZY)
    /** The bitstreams in this bundle */
    @JoinTable(
            name = "bundle2bitstream",
            joinColumns = {@JoinColumn(name = "bitstream_id") },
            inverseJoinColumns = {@JoinColumn(name = "bundle_id") }
    )
    @CollectionId(
            columns = @Column(name="id"),
            type=@Type(type="integer"),
            generator = "bundle2bitstream_seq"
    )
    @SequenceGenerator(name="bundle2bitstream_seq", sequenceName="bundle2bitstream_seq", allocationSize = 1)
    @OrderBy("sequence_id asc")
    //TODO: link to multiple bundles
    private Bundle bundle = null;

    @OneToOne(fetch = FetchType.LAZY, mappedBy="logo")
    private Community community;

    @OneToOne(fetch = FetchType.LAZY, mappedBy="logo")
    private Collection collection;

    @Column(name = "user_format_description")
    private String userFormatDescription;


    /** Flag set when data is modified, for events */
    @Transient
    private boolean modified;

    /** Flag set when metadata is modified, for events */
    @Transient
    private boolean modifiedMetadata;





    /**
     * Get the internal identifier of this bitstream
     *
     * @return the internal identifier
     */
    public int getID()
    {
        return id;
    }

    @Override
    public String getHandle(Context context) throws SQLException {
        return null;
    }

    /**
     * Get the sequence ID of this bitstream
     *
     * @return the sequence ID
     */
    public int getSequenceID()
    {
        return sequenceId;
    }

    /**
     * Set the sequence ID of this bitstream
     *
     * @param sid
     *            the ID
     */
    public void setSequenceID(int sid)
    {
        this.sequenceId = sid;
        modifiedMetadata = true;
        //TODO: HIBERNATE, IMPLEMENT DETAILS
//        addDetails("SequenceID");
    }

    /**
     * Get the name of this bitstream - typically the filename, without any path
     * information
     *
     * @return the name of the bitstream
     */
    public String getName()
    {
        return name;
    }

    @Override
    public void updateLastModified(Context context) {
        //Also fire a modified event since the bitstream HAS been modified
        context.addEvent(new Event(Event.MODIFY, Constants.BITSTREAM, getID(), null));
    }

    @Override
    public int getType() {
        return Constants.BITSTREAM;
    }

    /**
     * Set the name of the bitstream
     *
     * @param n
     *            the new name of the bitstream
     */
    public void setName(String n)
    {
        this.name = n;
        modifiedMetadata = true;
        //TODO: HIBERNATE, IMPLEMENT DETAILS
//        addDetails("Name");
    }

    /**
     * Get the source of this bitstream - typically the filename with path
     * information (if originally provided) or the name of the tool that
     * generated this bitstream
     *
     * @return the source of the bitstream
     */
    public String getSource()
    {
        return source;
    }

    /**
     * Set the source of the bitstream
     *
     * @param n
     *            the new source of the bitstream
     */
    public void setSource(String n)
    {
        this.source = n;
        modifiedMetadata = true;
        //TODO: HIBERNATE, IMPLEMENT DETAILS
//        addDetails("Source");
    }

    /**
     * Get the description of this bitstream - optional free text, typically
     * provided by a user at submission time
     *
     * @return the description of the bitstream
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Set the description of the bitstream
     *
     * @param n
     *            the new description of the bitstream
     */
    public void setDescription(String n)
    {
        this.description = n;
        modifiedMetadata = true;
        //TODO: HIBERNATE, IMPLEMENT DETAILS
//        addDetails("Description");
    }


    /**
     * Get the checksum of the content of the bitstream, for integrity checking
     *
     * @return the checksum
     */
    public String getChecksum()
    {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    /**
     * Get the algorithm used to calculate the checksum
     *
     * @return the algorithm, e.g. "MD5"
     */
    public String getChecksumAlgorithm()
    {
        return checksumAlgorithm;
    }

    public void setChecksumAlgorithm(String checksumAlgorithm) {
        this.checksumAlgorithm = checksumAlgorithm;
    }

    /**
     * Get the size of the bitstream
     *
     * @return the size in bytes
     */
    public long getSize()
    {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    /**
     * Get the asset store number where this bitstream is stored
     *
     * @return the asset store number of the bitstream
     */
    public int getStoreNumber() {
        return storeNumber;
    }

    public void setStoreNumber(int storeNumber) {
        this.storeNumber = storeNumber;
    }

    /**
     * Get the format of the bitstream
     *
     * @return the format of this bitstream
     */
    public BitstreamFormat getFormat() {
        return bitstreamFormat;
    }

    void setFormat(BitstreamFormat bitstreamFormat) {
        this.bitstreamFormat = bitstreamFormat;
        modified = true;
    }

    /**
     * Get the bundle this bitstream appears in
     *
     * @return array of <code>Bundle</code> s this bitstream appears in
     * @throws SQLException
     */
    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public Collection getCollection() {
        return collection;
    }

    public Community getCommunity() {
        return community;
    }

    public int getBitstreamOrder() {
        return bitstreamOrder;
    }

    public void setBitstreamOrder(int bitstreamOrder) {
        this.bitstreamOrder = bitstreamOrder;
    }

    /**
     * Get the user's format description. Returns null if the format is known by
     * the system.
     *
     * @return the user's format description.
     */
    public String getUserFormatDescription() {
        return userFormatDescription;
    }

    public void setUserFormatDescription(String userFormatDescription) {
        this.userFormatDescription = userFormatDescription;
        modifiedMetadata = true;
    }

    public boolean isModifiedMetadata() {
        return modifiedMetadata;
    }

    public void setModifiedMetadata(boolean modifiedMetadata) {
        this.modifiedMetadata = modifiedMetadata;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }
}
