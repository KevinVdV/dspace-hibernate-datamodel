package org.dspace.checker;

import javax.persistence.*;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 23/04/14
 * Time: 15:26
 */
@Entity
@Table(name="checksum_results", schema = "public")
public final class ChecksumResult
{
    @Id
    @Column(name="result_code")
    @Enumerated(EnumType.STRING)
    private ChecksumResultCode resultCode;

    @Column(name = "result_description")
    private String resultDescription;

    public ChecksumResultCode getResultCode() {
        return resultCode;
    }

    public String getResultDescription() {
        return resultDescription;
    }
}
