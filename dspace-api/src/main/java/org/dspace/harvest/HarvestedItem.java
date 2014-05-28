package org.dspace.harvest;

import org.dspace.content.Item;

import javax.persistence.*;
import java.util.Date;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 23/04/14
 * Time: 12:05
 */
@Entity
@Table(name="harvested_item", schema = "public")
public class HarvestedItem  {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="harvested_item_seq")
    @SequenceGenerator(name="harvested_item_seq", sequenceName="harvested_item_seq", allocationSize = 1)
    private int id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="item_id")
    private Item item;

    @Column(name = "last_harvested", columnDefinition="timestamp with time zone")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastHarvested;

    @Column(name = "oai_id")
    private String oaiId;

    public int getId() {
        return id;
    }

    public Item getItem() {
        return item;
    }

    void setItem(Item item) {
        this.item = item;
    }

    public Date getLastHarvested() {
        return lastHarvested;
    }

    public void setLastHarvested(Date lastHarvested) {
        this.lastHarvested = lastHarvested;
    }

    public String getOaiId() {
        return oaiId;
    }

    void setOaiId(String oaiId) {
        this.oaiId = oaiId;
    }
}
