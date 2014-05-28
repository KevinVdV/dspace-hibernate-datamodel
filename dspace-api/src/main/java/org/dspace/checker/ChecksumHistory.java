package org.dspace.checker;

import javax.persistence.*;
import java.util.Date;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 25/04/14
 * Time: 08:33
 */
@Entity
@Table(name="checksum_history", schema = "public")
public class ChecksumHistory {

    @Id
    @Column(name="check_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="checksum_history_check_id_seq")
    @SequenceGenerator(name="checksum_history_check_id_seq", sequenceName="checksum_history_check_id_seq")
    private long id;

    @Column(name = "bitstream_id")
    private int bitstreamId;

    @Column(name = "process_start_date", nullable = false)
    private Date processStartDate;

    @Column(name = "process_end_date", nullable = false)
    private Date processEndDate;


    @Column(name= "checksum_expected", nullable = false)
    private String checksumExpected;

    @Column(name= "checksum_calculated", nullable = false)
    private String checksumCalculated;

    @ManyToOne
    @JoinColumn(name = "result")
    private ChecksumResult checksumResult;

    public long getId() {
        return id;
    }

    public int getBitstreamId() {
        return bitstreamId;
    }

    public void setBitstreamId(int bitstreamId) {
        this.bitstreamId = bitstreamId;
    }

    public Date getProcessStartDate() {
        return processStartDate;
    }

    public void setProcessStartDate(Date processStartDate) {
        this.processStartDate = processStartDate;
    }

    public Date getProcessEndDate() {
        return processEndDate;
    }

    public void setProcessEndDate(Date processEndDate) {
        this.processEndDate = processEndDate;
    }

    public String getChecksumExpected() {
        return checksumExpected;
    }

    public void setChecksumExpected(String checksumExpected) {
        this.checksumExpected = checksumExpected;
    }

    public String getChecksumCalculated() {
        return checksumCalculated;
    }

    public void setChecksumCalculated(String checksumCalculated) {
        this.checksumCalculated = checksumCalculated;
    }

    public ChecksumResult getChecksumResult() {
        return checksumResult;
    }

    public void setChecksumResult(ChecksumResult checksumResult) {
        this.checksumResult = checksumResult;
    }
}
