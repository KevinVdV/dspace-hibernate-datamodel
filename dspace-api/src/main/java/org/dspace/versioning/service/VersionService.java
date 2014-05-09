/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.versioning.service;

import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.versioning.Version;
import org.dspace.versioning.VersionHistory;

import java.sql.SQLException;
import java.util.Date;

/**
 *
 *
 * @author Fabio Bolognesi (fabio at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 */
public interface VersionService {

    public Version createVersion(Context c, Item item);

    public Version createVersion(Context c, Item item, String summary);

    public Version createVersion(Context c, VersionHistory vh, Item item, String summary, Date date, int versionNumber) throws SQLException;

    public void removeVersion(Context c, int versionID) throws SQLException;

    public void removeVersion(Context c, Item item) throws SQLException;

    public Version find(Context c, int versionID) throws SQLException;

    public Version restoreVersion(Context c, int versionID);

    public Version restoreVersion(Context c, int versionID, String summary);

    public Version updateVersion(Context c, Item item, String summary) throws SQLException;

    public Version findByItem(Context c, Item item) throws SQLException;
}
