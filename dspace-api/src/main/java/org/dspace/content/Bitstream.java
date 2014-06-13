package org.dspace.content;

import org.dspace.checker.MostRecentChecksum;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.BitstreamService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Event;
import org.hibernate.annotations.CollectionId;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 27/02/14
 * Time: 09:25
 */
@Entity
@Table(name="bitstream", schema = "public")
public class Bitstream extends DSpaceObject{


    @Id
    @Column(name="bitstream_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="bitstream_seq")
    @SequenceGenerator(name="bitstream_seq", sequenceName="bitstream_seq")
    private Integer id;

    @Column(name = "sequence_id")
    private Integer sequenceId = -1;

    @Column(name = "name", length = 256)
    private String name;

    @Column(name = "source", length = 256)
    private String source;

    @Column(name = "description")
    @Lob //Generates a TEXT or LONGTEXT data type
    private String description;

    @Column(name = "checksum", length = 64)
    private String checksum;

    @Column(name = "checksum_algorithm", length = 32)
    private String checksumAlgorithm;

    @Column(name = "size_bytes")
    private long sizeBytes;

    @Column(name = "deleted")
    private boolean deleted;

    @Column(name = "internal_id", length = 256)
    private String internalId;

    @Column(name = "store_number")
    private int storeNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bitstream_format_id")
    private BitstreamFormat bitstreamFormat;

    @OneToMany(mappedBy = "bitstream", fetch = FetchType.LAZY)
    @OrderBy("bitstreamOrder asc")
    private List<BundleBitstream> bundles = new ArrayList<BundleBitstream>();

    @OneToOne(fetch = FetchType.LAZY, mappedBy="logo")
    private Community community;

    @OneToOne(fetch = FetchType.LAZY, mappedBy="logo")
    private Collection collection;

    @Column(name = "user_format_description")
    @Lob //Generates a TEXT or LONGTEXT data type
    private String userFormatDescription;


    /** Flag set when data is modified, for events */
    @Transient
    private boolean modified;

    /** Flag set when metadata is modified, for events */
    @Transient
    private boolean modifiedMetadata;

    @Transient
    private BitstreamService bitstreamService = ContentServiceFactory.getInstance().getBitstreamService();

    /**
     * Get the internal identifier of this bitstream
     *
     * @return the internal identifier
     */
    public int getID()
    {
        return id;
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
        addDetails("SequenceID");
    }

    /**
     * Get the name of this bitstream - typically the filename, without any path
     * information
     *
     * @return the name of the bitstream
     */
    String getNameInternal()
    {
        return name;
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
        addDetails("Name");
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
        addDetails("Source");
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
        addDetails("Description");
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
    public List<BundleBitstream> getBundles() {
        return bundles;
    }

    void setBundles(List<BundleBitstream> bundles) {
        this.bundles = bundles;
    }

    public Collection getCollection() {
        return collection;
    }

    public Community getCommunity() {
        return community;
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

    void setUserFormatDescription(String userFormatDescription) {
        this.userFormatDescription = userFormatDescription;
        modifiedMetadata = true;
        addDetails("UserFormatDescription");
    }

    boolean isModifiedMetadata() {
        return modifiedMetadata;
    }

    void setModifiedMetadata(boolean modifiedMetadata) {
        this.modifiedMetadata = modifiedMetadata;
    }

    boolean isModified() {
        return modified;
    }

    void setModified(boolean modified) {
        this.modified = modified;
    }

    /*
        Getters & setters which should be removed on the long run, they are just here to provide all getters & setters to the item object
     */


    public void setUserFormatDescription(Context context, String desc) throws SQLException
    {
        bitstreamService.setUserFormatDescription(context, this, desc);
    }

    public String getFormatDescription()
    {
        return bitstreamService.getFormatDescription(this);
    }

    public void setFormat(Context context, BitstreamFormat f) throws SQLException
    {
        bitstreamService.setFormat(context, this, f);
    }

    public String getName()
    {
        return bitstreamService.getName(this);
    }
}
