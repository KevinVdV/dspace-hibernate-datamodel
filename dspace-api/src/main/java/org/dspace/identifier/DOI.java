package org.dspace.identifier;

import org.dspace.content.DSpaceObject;

import javax.persistence.*;

/**
 * Created by kevin on 01/05/14.
 */
@Entity
@Table(name = "Doi", schema = "public" )
public class DOI implements Identifier{

    @Transient
    public static final Integer TO_BE_REGISTERED = 1;
    @Transient
    public static final Integer TO_BE_RESERVERED = 2;
    @Transient
    public static final Integer IS_REGISTERED = 3;
    @Transient
    public static final Integer IS_RESERVED = 4;
    @Transient
    public static final Integer UPDATE_RESERVERED = 5;
    @Transient
    public static final Integer UPDATE_REGISTERED = 6;
    @Transient
    public static final Integer UPDATE_BEFORE_REGISTERATION = 7;
    @Transient
    public static final Integer TO_BE_DELETED = 8;
    @Transient
    public static final Integer DELETED = 9;

    @Id
    @Column(name="doi_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="doi_seq")
    @SequenceGenerator(name="doi_seq", sequenceName="doi_seq", allocationSize = 1)
    private Integer id;

    @Column(name = "doi", unique = true, length = 256)
    private String doi;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dspace_object_id")
    private DSpaceObject dSpaceObject;

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

    public DSpaceObject getDSpaceObject() {
        return dSpaceObject;
    }

    public void setDSpaceObject(DSpaceObject dSpaceObject) {
        this.dSpaceObject = dSpaceObject;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
