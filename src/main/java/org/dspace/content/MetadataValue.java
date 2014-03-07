/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.core.LogManager;

import javax.persistence.*;

/**
 * Database access class representing a Dublin Core metadata value.
 * It represents a value of a given <code>MetadataField</code> on an Item.
 * (The Item can have many values of the same field.)  It contains element, qualifier, value and language.
 * the field (which names the schema, element, and qualifier), language,
 * and a value.
 *
 * @author Martin Hald
 * @see org.dspace.content.MetadataSchema
 * @see org.dspace.content.MetadataField
 */
//TODO: ADD toHashSet & equals methods
@Entity
@Table(name="metadatavalue")
public class MetadataValue
{
    /** The reference to the metadata field */
    @Id
    @Column(name="metadata_value_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="metadatavalue_seq")
    @SequenceGenerator(name="metadatavalue_seq", sequenceName="metadatavalue_seq")
    private int fieldId;

    /** The primary key for the metadata value */
    @ManyToOne
    @JoinColumn(name = "metadata_field_id")
    private MetadataField metadataField = null;

    /** The reference to the DSpace item */
    @Column(name = "item_id")
    private int itemId;

    /** The value of the field */
    @Column(name = "text_value")
    public String value;

    /** The language of the field, may be <code>null</code> */
    @Column(name = "text_lang")
    public String language;

    /** The position of the record. */
    @Column(name = "place")
    public int place = 1;

    /** Authority key, if any */
    @Column(name = "authority")
    public String authority = null;

    /** Authority confidence value -- see Choices class for values */
    @Column(name = "confidence")
    public int confidence = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="item_id", insertable = false, updatable = false)
    public Item item;

    /**
     * Constructor to create a value for a given field.
     *
     * @param field initial value for field
     */
    public MetadataValue(MetadataField field)
    {
        this.fieldId = field.getFieldID();
    }

    public MetadataValue() {
    }

    /**
     * Get the field ID the metadata value represents.
     *
     * @return metadata field ID
     */
    public int getFieldId()
    {
        return fieldId;
    }

    /**
     * Set the field ID that the metadata value represents.
     *
     * @param fieldId new field ID
     */
    public void setFieldId(int fieldId)
    {
        this.fieldId = fieldId;
    }

    /**
     * Get the item ID.
     *
     * @return item ID
     */
    public int getItemId()
    {
        return itemId;
    }

    /**
     * Set the item ID.
     *
     * @param itemId new item ID
     */
    public void setItemId(int itemId)
    {
        this.itemId = itemId;
    }

    /**
     * Get the language (e.g. "en").
     *
     * @return language
     */
    public String getLanguage()
    {
        return language;
    }

    /**
     * Set the language (e.g. "en").
     *
     * @param language new language
     */
    public void setLanguage(String language)
    {
        this.language = language;
    }

    /**
     * Get the place ordering.
     *
     * @return place ordering
     */
    public int getPlace()
    {
        return place;
    }

    /**
     * Set the place ordering.
     *
     * @param place new place (relative order in series of values)
     */
    public void setPlace(int place)
    {
        this.place = place;
    }

    public MetadataField getMetadataField() {
        return metadataField;
    }

    public void setMetadataField(MetadataField metadataField) {
        this.metadataField = metadataField;
    }

    /**
     * Get the metadata value.
     *
     * @return metadata value
     */
    public String getValue()
    {
        return value;
    }

    /**
     * Set the metadata value
     *
     * @param value new metadata value
     */
    public void setValue(String value)
    {
        this.value = value;
    }

    /**
     * Get the metadata authority
     *
     * @return metadata authority
     */
    public String getAuthority ()
    {
        return authority ;
    }

    /**
     * Set the metadata authority
     *
     * @param value new metadata authority
     */
    public void setAuthority (String value)
    {
        this.authority  = value;
    }

    /**
     * Get the metadata confidence
     *
     * @return metadata confidence
     */
    public int getConfidence()
    {
        return confidence;
    }

    /**
     * Set the metadata confidence
     *
     * @param value new metadata confidence
     */
    public void setConfidence(int value)
    {
        this.confidence = value;
    }


    /**
     * Return <code>true</code> if <code>other</code> is the same MetadataValue
     * as this object, <code>false</code> otherwise
     *
     * @param obj
     *            object to compare to
     *
     * @return <code>true</code> if object passed in represents the same
     *         MetadataValue as this object
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
        final MetadataValue other = (MetadataValue) obj;
        if (this.fieldId != other.fieldId)
        {
            return false;
        }
        if (this.getFieldId() != other.getFieldId())
        {
            return false;
        }
        if (this.itemId != other.itemId)
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 47 * hash + this.fieldId;
        hash = 47 * hash + this.getFieldId();
        hash = 47 * hash + this.itemId;
        return hash;
    }
}