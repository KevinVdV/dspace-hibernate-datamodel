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
    private Integer resource_type_id;

    @Column(name = "resource_id")
    private Integer resource_id;

    public Integer getId() {
        return id;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public Integer getResource_type_id() {
        return resource_type_id;
    }

    public void setResource_type_id(Integer resource_type_id) {
        this.resource_type_id = resource_type_id;
    }

    public Integer getResource_id() {
        return resource_id;
    }

    public void setResource_id(Integer resource_id) {
        this.resource_id = resource_id;
    }
}
