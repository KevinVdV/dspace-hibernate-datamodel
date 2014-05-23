package org.dspace.eperson;

import javax.persistence.*;
import java.io.Serializable;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 16/05/14
 * Time: 10:22
 */
@Entity
@Table(name = "group2groupcache", schema = "public" )
public class Group2GroupCache implements Serializable {

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id", nullable = false)
    public Group parent;

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "child_id", nullable = false)
    public Group child;

    public Group getParent() {
        return parent;
    }

    public void setParent(Group parent) {
        this.parent = parent;
    }

    public Group getChild() {
        return child;
    }

    public void setChild(Group child) {
        this.child = child;
    }
}
