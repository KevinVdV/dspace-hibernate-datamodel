package org.dspace.content;

import javax.persistence.*;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 27/02/14
 * Time: 13:38
 */
@Entity
@Table(name="metadataschemaregistry", schema = "public")
public class MetadataSchema {

    /** Short Name of built-in Dublin Core schema. */
    public static final String DC_SCHEMA = "dc";

    @Id
    @Column(name="metadata_schema_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="metadataschemaregistry_seq")
    @SequenceGenerator(name="metadataschemaregistry_seq", sequenceName="metadataschemaregistry_seq", allocationSize = 1)
    private Integer id;

    @Column(name = "namespace", unique = true, length = 256)
    private String namespace;

    @Column(name = "short_id", unique = true, length = 32)
    private String name;


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
        final MetadataSchema other = (MetadataSchema) obj;
        if (!this.id.equals(other.id))
        {
            return false;
        }
        if ((this.namespace == null) ? (other.namespace != null) : !this.namespace.equals(other.namespace))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 67 * hash + this.id;
        hash = 67 * hash + (this.namespace != null ? this.namespace.hashCode() : 0);
        return hash;
    }



    /**
     * Get the schema namespace.
     *
     * @return namespace String
     */
    public String getNamespace()
    {
        return namespace;
    }

    /**
     * Set the schema namespace.
     *
     * @param namespace  XML namespace URI
     */
    public void setNamespace(String namespace)
    {
        this.namespace = namespace;
    }

    /**
     * Get the schema name.
     *
     * @return name String
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the schema name.
     *
     * @param name  short name of schema
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Get the schema record key number.
     *
     * @return schema record key
     */
    public int getSchemaID()
    {
        return id;
    }



}
