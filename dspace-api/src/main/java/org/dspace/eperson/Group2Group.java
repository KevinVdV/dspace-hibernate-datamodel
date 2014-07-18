package org.dspace.eperson;

import javax.persistence.*;
import java.io.Serializable;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 16/05/14
 * Time: 10:12
 */
@Entity
@Table(name = "group2group", schema = "public" )
public class Group2Group implements Serializable {

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

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final Group2GroupCache other = (Group2GroupCache) obj;
        if(!parent.equals(other.getParent()))
        {
            return false;
        }
        if(!child.equals(other.getChild()))
        {
            return false;
        }
        return true;
    }
}
