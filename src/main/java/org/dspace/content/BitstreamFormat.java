package org.dspace.content;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CollectionId;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 25/02/14
 * Time: 14:19
 */
@Entity
@Table(name="bitstreamformatregistry")
public class BitstreamFormat {

    @Id
    @Column(name="bitstream_format_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="bitstreamformatregistry_seq")
    @SequenceGenerator(name="bitstreamformatregistry_seq", sequenceName="bitstreamformatregistry_seq")
    private Integer id;

    @Column(name="short_description")
    private String shortDescription;

    @Column(name="description")
    private String description;

    @Column(name="mimetype")
    private String mimetype;

    @Column(name="support_level")
    private int supportLevel;

    @Column(name="internal")
    private boolean internal;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name="fileextension", joinColumns=@JoinColumn(name="bitstream_format_id"))
        @CollectionId(
                columns = @Column(name="file_extension_id"),
                type=@Type(type="integer"),
                generator = "fileextension_seq"
        )
    @SequenceGenerator(name="fileextension_seq", sequenceName="fileextension_seq", allocationSize = 1)
    @Column(name="extension")
    @Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    private List<String> fileExtensions;


    /**
     * Get the internal identifier of this bitstream format
     *
     * @return the internal identifier
     */
    public final int getID()
    {
        return id;
    }

    /**
     * Get a short (one or two word) description of this bitstream format
     *
     * @return the short description
     */
    public final String getShortDescription()
    {
        return shortDescription;
    }

    void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    /**
     * Get a description of this bitstream format, including full application or
     * format name
     *
     * @return the description
     */
    public final String getDescription()
    {
        return description;
    }

    /**
     * Set the description of the bitstream format
     *
     * @param s
     *            the new description
     */
    public final void setDescription(String s)
    {
        this.description = s;
    }

    /**
     * Get the MIME type of this bitstream format, for example
     * <code>text/plain</code>
     *
     * @return the MIME type
     */
    public final String getMIMEType()
    {
        return mimetype;
    }

    /**
     * Set the MIME type of the bitstream format
     *
     * @param s
     *            the new MIME type
     */
    public final void setMIMEType(String s)
    {
        this.mimetype = s;
    }

    /**
     * Get the support level for this bitstream format - one of
     * <code>UNKNOWN</code>,<code>KNOWN</code> or <code>SUPPORTED</code>.
     *
     * @return the support level
     */
    public final int getSupportLevel()
    {
        return supportLevel;
    }

    /**
     * Set the support level for this bitstream format - one of
     * <code>UNKNOWN</code>,<code>KNOWN</code> or <code>SUPPORTED</code>.
     *
     * @param supportLevel the support level
     */
    public void setSupportLevel(int supportLevel) {
        this.supportLevel = supportLevel;
    }

    /**
     * Find out if the bitstream format is an internal format - that is, one
     * that is used to store system information, rather than the content of
     * items in the system
     *
     * @return <code>true</code> if the bitstream format is an internal type
     */
    public final boolean isInternal()
    {
        return internal;
    }

    /**
     * Set whether the bitstream format is an internal format
     *
     * @param b
     *            pass in <code>true</code> if the bitstream format is an
     *            internal type
     */
    public final void setInternal(boolean b)
    {
        this.internal = b;
    }

    /**
     * Get the filename extensions associated with this format
     *
     * @return the extensions
     */
    public List<String> getFileExtensions() {
        return fileExtensions;
    }


    /**
     * Set the filename extensions associated with this format
     *
     * @param exts
     *            String [] array of extensions
     */
    public void setFileExtensions(List<String> fileExtensions) {
        this.fileExtensions = fileExtensions;
    }
}