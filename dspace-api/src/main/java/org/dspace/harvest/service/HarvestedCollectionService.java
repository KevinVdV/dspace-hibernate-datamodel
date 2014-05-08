package org.dspace.harvest.service;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 23/04/14
 * Time: 13:04
 */
public interface HarvestedCollectionService {
    public static final int TYPE_NONE = 0;
    public static final int TYPE_DMD = 1;
    public static final int TYPE_DMDREF = 2;
    public static final int TYPE_FULL = 3;

    public static final int STATUS_READY = 0;
    public static final int STATUS_BUSY = 1;
    public static final int STATUS_QUEUED = 2;
    public static final int STATUS_OAI_ERROR = 3;
    public static final int STATUS_UNKNOWN_ERROR = -1;


}
