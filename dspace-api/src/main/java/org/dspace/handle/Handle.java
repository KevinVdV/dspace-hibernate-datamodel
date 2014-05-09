package org.dspace.handle;

import javax.persistence.*;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 24/02/14
 * Time: 13:28
 */
@Entity
@Table(name="handle", schema = "public")
public class Handle {

    @Id
    @Column(name="handle_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="handle_seq")
    @SequenceGenerator(name="handle_seq", sequenceName="handle_seq", allocationSize = 1)
    private Integer id;

    @Column(name = "handle", unique = true)
    private String handle;

    @Column(name = "resource_type_id")
    private Integer resourceTypeId;

    @Column(name = "resource_id")
    private Integer resourceId;

    public Integer getId() {
        return id;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public Integer getResourceTypeId() {
        return resourceTypeId;
    }

    void setResourceTypeId(Integer resource_type_id) {
        this.resourceTypeId = resource_type_id;
    }

    public Integer getResourceId() {
        return resourceId;
    }

    void setResourceId(Integer resource_id) {
        this.resourceId = resource_id;
    }
}
