package org.dspace.xmlworkflow.storedcomponents;

import org.dspace.content.Collection;
import org.dspace.eperson.Group;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 21/04/14
 * Time: 14:54
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name="cwf_collectionrole", schema = "public")
public class CollectionRole {

    @Id
    @Column(name="collectionrole_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="cwf_collectionrole_seq")
    @SequenceGenerator(name="cwf_collectionrole_seq", sequenceName="cwf_collectionrole_seq", allocationSize = 1)
    private int id;

    @Column(name = "role_id")
    @Lob
    private String roleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    private Collection collection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    public int getId() {
        return id;
    }

    public String getRoleId() {
        return roleId;
    }

    void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public Collection getCollection() {
        return collection;
    }

    void setCollection(Collection collection) {
        this.collection = collection;
    }

    public Group getGroup() {
        return group;
    }

    void setGroup(Group group) {
        this.group = group;
    }
}
