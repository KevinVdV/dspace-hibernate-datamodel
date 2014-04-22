package org.dspace.content;

import org.dspace.eperson.EPerson;

import javax.persistence.*;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 22/04/14
 * Time: 14:06
 */

@Entity
@Table(name = "subscription", schema = "public")
public class Subscription {

    @Id
    @Column(name = "subscription_id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="subscription_seq")
    @SequenceGenerator(name="subscription_seq", sequenceName="subscription_seq")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    private Collection collection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eperson_id")
    private EPerson ePerson;

    public int getId() {
        return id;
    }

    public Collection getCollection() {
        return collection;
    }

    void setCollection(Collection collection) {
        this.collection = collection;
    }

    public EPerson getePerson() {
        return ePerson;
    }

    void setePerson(EPerson ePerson) {
        this.ePerson = ePerson;
    }
}
