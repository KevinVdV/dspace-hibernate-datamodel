package org.dspace.versioning;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 8/05/14
 * Time: 09:23
 */
@Entity
@Table(name="versionhistory", schema = "public")
public class VersionHistory {

    @Id
    @Column(name="versionhistory_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="versionhistory_seq")
    @SequenceGenerator(name="versionhistory_seq", sequenceName="versionhistory_seq", allocationSize = 1)
    private int id;

    //We use fetchtype eager for versions since we always require our versions when loading the history
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "versionHistory")
    @OrderBy(value = "versionNumber desc")
    private List<Version> versions = new ArrayList<Version>();

    public int getId() {
        return id;
    }

    public List<Version> getVersions() {
        return versions;
    }

    void setVersions(List<Version> versions) {
        this.versions = versions;
    }

    void addVersionAtStart(Version version)
    {
        this.versions.add(0, version);
    }

    void removeVersion(Version version) {
        this.versions.remove(version);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        VersionHistory that = (VersionHistory) o;
        return getId() == that.getId();
    }

    @Override
    public int hashCode()
    {
        int hash=7;
        hash=79*hash+ this.getId();
        return hash;
    }

}