package org.dspace.harvest;

import org.dspace.content.Collection;

import javax.persistence.*;
import java.util.Date;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 23/04/14
 * Time: 12:47
 */
@Entity
@Table(name="harvested_collection", schema = "public")
public class HarvestedCollection
{
    @Id
    @Column(name="item_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="harvested_collection_seq")
    @SequenceGenerator(name="harvested_collection_seq", sequenceName="harvested_collection_seq")
    private Integer id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "collection_id")
    private Collection collection;

    @Column(name = "harvest_type")
    private int harvestType;

    @Column(name = "oai_source")
    private String oaiSource;

    @Column(name = "oai_set_id")
    private String oaiSetId;

    @Column(name = "harvest_message")
    private String harvestMessage;

    @Column(name = "metadata_config_id")
    private String metadataConfigId;

    @Column(name = "harvest_status")
    private int harvestStatus;

    //TODO: uses "timestamp with time zone" will need to be changed
    @Column(name = "harvest_start_time")
    private Date harvestStartTime;

    //TODO: uses "timestamp with time zone" will need to be changed
    @Column(name = "last_harvested")
    private Date lastHarvested;

    public Integer getId() {
        return id;
    }

    public Collection getCollection() {
        return collection;
    }

    void setCollection(Collection collection) {
        this.collection = collection;
    }

    public int getHarvestType() {
        return harvestType;
    }

    public void setHarvestType(int harvestType) {
        this.harvestType = harvestType;
    }

    public String getOaiSource() {
        return oaiSource;
    }

    public void setOaiSource(String oaiSource) {
        this.oaiSource = oaiSource;
    }

    public String getOaiSetId() {
        return oaiSetId;
    }

    public void setOaiSetId(String oaiSetId) {
        this.oaiSetId = oaiSetId;
    }

    public String getHarvestMessage() {
        return harvestMessage;
    }

    public void setHarvestMessage(String harvestMessage) {
        this.harvestMessage = harvestMessage;
    }

    public String getMetadataConfigId() {
        return metadataConfigId;
    }

    public void setMetadataConfigId(String metadataConfigId) {
        this.metadataConfigId = metadataConfigId;
    }

    public int getHarvestStatus() {
        return harvestStatus;
    }

    public void setHarvestStatus(int harvestStatus) {
        this.harvestStatus = harvestStatus;
    }

    public Date getHarvestStartTime() {
        return harvestStartTime;
    }

    public void setHarvestStartTime(Date harvestStartTime) {
        this.harvestStartTime = harvestStartTime;
    }

    public Date getLastHarvested() {
        return lastHarvested;
    }

    public void setLastHarvested(Date lastHarvested) {
        this.lastHarvested = lastHarvested;
    }
}
