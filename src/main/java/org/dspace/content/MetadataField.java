package org.dspace.content;

import javax.persistence.*;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 27/02/14
 * Time: 13:06
 */
@Entity
@Table(name="metadatafieldregistry", schema = "public")
public class MetadataField {

    @Id
    @Column(name="metadata_field_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="metadatafieldregistry_seq")
    @SequenceGenerator(name="metadatafieldregistry_seq", sequenceName="metadatafieldregistry_seq")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metadata_schema_id",nullable = false)
    private MetadataSchema metadataSchema;

    @Column(name = "element")
    private String element;

    @Column(name = "qualifier")
    private String qualifier = null;

    @Column(name = "scope_note")
    private String scopeNote;


    /**
     * Get the metadata field id.
     *
     * @return metadata field id
     */
    public int getFieldID()
    {
        return id;
    }

    /**
     * Get the element name.
     *
     * @return element name
     */
    public String getElement()
    {
        return element;
    }

    /**
     * Set the element name.
     *
     * @param element new value for element
     */
    public void setElement(String element)
    {
        this.element = element;
    }

    /**
     * Get the qualifier.
     *
     * @return qualifier
     */
    public String getQualifier()
    {
        return qualifier;
    }

    /**
     * Set the qualifier.
     *
     * @param qualifier new value for qualifier
     */
    public void setQualifier(String qualifier)
    {
        this.qualifier = qualifier;
    }

    /**
     * Get the scope note.
     *
     * @return scope note
     */
    public String getScopeNote()
    {
        return scopeNote;
    }

    /**
     * Set the scope note.
     *
     * @param scopeNote new value for scope note
     */
    public void setScopeNote(String scopeNote)
    {
        this.scopeNote = scopeNote;
    }

    /**
     * Get the schema .
     *
     * @return schema record
     */
    public MetadataSchema getMetadataSchema() {
        return metadataSchema;
    }

    /**
     * Set the schema record key.
     *
     * @param metadataSchema new value for key
     */
    public void setMetadataSchema(MetadataSchema metadataSchema) {
        this.metadataSchema = metadataSchema;
    }



    /**
     * Return <code>true</code> if <code>other</code> is the same MetadataField
     * as this object, <code>false</code> otherwise
     *
     * @param obj
     *            object to compare to
     *
     * @return <code>true</code> if object passed in represents the same
     *         MetadataField as this object
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
        final MetadataField other = (MetadataField) obj;
        if (this.getFieldID() != other.getFieldID())
        {
            return false;
        }
        if (!getMetadataSchema().equals(other.getMetadataSchema()))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 47 * hash + getFieldID();
        hash = 47 * hash + getMetadataSchema().getSchemaID();
        return hash;
    }

    @Override
    public String toString() {
        if (qualifier == null)
        {
            return getMetadataSchema().getName() + "_" + element;
        }
        else
        {
            return getMetadataSchema().getName() + "_" + element + "_" + qualifier;
        }
    }
}
