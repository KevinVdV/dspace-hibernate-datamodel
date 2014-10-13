package org.dspace.handle;

import org.dspace.content.DSpaceObject;

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

    @Column(name = "handle", unique = true, length = 256)
    private String handle;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dspace_object")
    private DSpaceObject dspaceObject;

    @Column(name = "resource_type_id")
    private Integer resourceTypeId;

    public Integer getId() {
        return id;
    }

    public String getHandle() {
        return handle;
    }

    void setHandle(String handle) {
        this.handle = handle;
    }

    public Integer getResourceTypeId() {
        return resourceTypeId;
    }

    void setResourceTypeId(Integer resource_type_id) {
        this.resourceTypeId = resource_type_id;
    }


    public DSpaceObject getDSpaceObject() {
        return dspaceObject;
    }

    void setDSpaceObject(DSpaceObject dspaceObject) {
        this.dspaceObject = dspaceObject;
    }
}
