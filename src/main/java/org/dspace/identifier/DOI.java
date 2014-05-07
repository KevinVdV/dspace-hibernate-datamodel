package org.dspace.identifier;

import javax.persistence.*;

/**
 * Created by kevin on 01/05/14.
 */
@Entity
@Table(name = "Doi", schema = "public" )
public class DOI {
    @Id
    @Column(name="doi_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="doi_seq")
    @SequenceGenerator(name="doi_seq", sequenceName="doi_seq", initialValue = 1)
    private Integer id;

    @Column(name = "doi", unique = true, length = 256)
    private String doi;

    @Column(name = "resource_type_id")
    private Integer resourceTypeId;

    @Column(name = "resource_id")
    private Integer resourceId;

    @Column(name = "status")
    private Integer status;

    public Integer getId() {
        return id;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public Integer getResourceTypeId() {
        return resourceTypeId;
    }

    public void setResourceTypeId(Integer resourceTypeId) {
        this.resourceTypeId = resourceTypeId;
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
