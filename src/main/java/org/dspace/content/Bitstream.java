package org.dspace.content;

import javax.persistence.*;
import java.sql.SQLException;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bitstream_format_id")
    private BitstreamFormat bitstreamFormat;




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
    public String getName()
    {
        return name;
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

    /**
     * Get the algorithm used to calculate the checksum
     *
     * @return the algorithm, e.g. "MD5"
     */
    public String getChecksumAlgorithm()
    {
        return checksumAlgorithm;
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

    public int getStoreNumber() {
        return storeNumber;
    }

    public void setStoreNumber(int storeNumber) {
        this.storeNumber = storeNumber;
    }

    public BitstreamFormat getFormat() {
        return bitstreamFormat;
    }

    public void setFormat(BitstreamFormat bitstreamFormat) {
        this.bitstreamFormat = bitstreamFormat;
    }
}
