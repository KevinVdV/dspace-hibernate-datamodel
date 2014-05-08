package org.dspace.checker;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 28/04/14
 * Time: 08:56
 */
public enum ChecksumResultCode {
    BITSTREAM_NOT_FOUND,
    BITSTREAM_INFO_NOT_FOUND,
    BITSTREAM_NOT_PROCESSED,
    BITSTREAM_MARKED_DELETED,
    CHECKSUM_MATCH,
    CHECKSUM_NO_MATCH,
    CHECKSUM_PREV_NOT_FOUND,
    CHECKSUM_ALGORITHM_INVALID
}
