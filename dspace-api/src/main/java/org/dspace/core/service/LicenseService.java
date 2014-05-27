package org.dspace.core.service;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 27/05/14
 * Time: 10:47
 */
public interface LicenseService {

    public void writeLicenseFile(String licenseFile, String newLicense);

    public String getLicenseText(String licenseFile);

    public String getDefaultSubmissionLicense();
}
