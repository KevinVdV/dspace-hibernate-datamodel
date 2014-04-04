package org.dspace.content;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.Group;
import org.dspace.event.Event;
import org.dspace.handle.HandleManager;
import org.hibernate.annotations.CollectionId;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 24/02/14
 * Time: 09:59
 */
@Entity
@Table(name="community")
public class Community extends DSpaceObject{

    @Id
    @Column(name="community_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="community_seq")
    @SequenceGenerator(name="community_seq", sequenceName="community_seq")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(
            name = "community2community",
            joinColumns = {@JoinColumn(name = "parent_comm_id") },
            inverseJoinColumns = {@JoinColumn(name = "child_comm_id") }
    )
    private Community parentCommunity;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parentCommunity")
    private List<Community> subCommunities = new ArrayList<Community>();

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "communities")
    private List<Collection> collections = new ArrayList<Collection>();

    @Column(name = "name")
    private String name = "";

    @Column(name = "short_description")
    private String shortDescription = "";

    @Column(name = "copyright_text")
    private String copyrightText ="";

    @Column(name = "side_bar_text")
    private String sideBarText = "";

    @Column(name = "introductory_text")
    private String introductoryText = "";


    @OneToOne
    @JoinColumn(name = "admin")
    /** The default group of administrators */
    private Group admins;

    /** The logo bitstream */
    @OneToOne
    @JoinColumn(name = "logo_bitstream_id")
    private Bitstream logo = null;


    //TODO: HIBERNATE: modified get it out of here ?
    /** Flag set when data is modified, for events */
    @Transient
    private boolean modified = false;

    @Transient
    /** Flag set when metadata is modified, for events */
    private boolean modifiedMetadata = false;

    /**
     * Get the internal ID of this community
     *
     * @return the internal identifier
     */
    public int getID()
    {
        return id;
    }

    /**
     * return type found in Constants
     */
    public int getType()
    {
        return Constants.COMMUNITY;
    }

    void addSubCommunity(Community subCommunity)
    {
        getSubCommunities().add(subCommunity);
    }

    void removeSubCommunity(Community subCommunity)
    {
        getSubCommunities().remove(subCommunity);
    }

    /**
     * Get the immediate sub-communities of this community. Throws an
     * SQLException because creating a community object won't load in all
     * collections.
     *
     * @return array of Community objects
     */
    public List<Community> getSubCommunities() {
        return subCommunities;
    }



    /**
     * Return <code>true</code> if <code>other</code> is the same Community
     * as this object, <code>false</code> otherwise
     *
     * @param other
     *            object to compare to
     *
     * @return <code>true</code> if object passed in represents the same
     *         community as this object
     */
    public boolean equals(Object other)
    {
        if (!(other instanceof Community))
        {
            return false;
        }

        return (getID() == ((Community) other).getID());
    }

    public int hashCode()
    {
        return new HashCodeBuilder().append(getID()).toHashCode();
    }

    /**
     * @see org.dspace.content.DSpaceObject#getHandle(Context context)
     */
    public String getHandle(Context context) throws SQLException {
        //TODO: HIBERNATE: REMOVE THIS
        return HandleManager.findHandle(context, this);
    }

    @Override
    public void updateLastModified(Context context)
    {
        //Also fire a modified event since the community HAS been modified
        context.addEvent(new Event(Event.MODIFY, Constants.COMMUNITY, getID(), null));
    }

    /**
     * Get the default group of administrators, if there is one. Note that the
     * authorization system may allow others to be administrators for the
     * community.
     * <P>
     * The default group of administrators for community 100 is the one called
     * <code>community_100_admin</code>.
     *
     * @return group of administrators, or <code>null</code> if there is no
     *         default group.
     */
    public Group getAdministrators() {
        return admins;
    }

    void setAdmins(Group admins) {
        this.admins = admins;
        modified = true;
    }


    /**
     * Return the parent community of this community, or null if the community
     * is top-level
     *
     * @return the immediate parent community, or null if top-level
     */
    public Community getParentCommunity() {
        return parentCommunity;
    }


    void setParentCommunity(Community parentCommunity) {
        this.parentCommunity = parentCommunity;
    }

    public boolean isModified() {
        return modified;
    }

    public boolean isModifiedMetadata() {
        return modifiedMetadata;
    }

    public void clearModifiedMetadata() {
        this.modifiedMetadata = false;
    }

    public void clearModified() {
        this.modified = false;
    }


    /**
     * Get the collections in this community. Throws an SQLException because
     * creating a community object won't load in all collections.
     *
     * @return array of Collection objects
     */
    public List<Collection> getCollections() {
        return collections;
    }

    void addCollection(Collection collection)
    {
        getCollections().add(collection);
    }

    void removeCollection(Collection collection)
    {
        getCollections().remove(collection);
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getCopyrightText() {
        return copyrightText;
    }

    public void setCopyrightText(String copyrightText) {
        this.copyrightText = copyrightText;
    }

    public String getSideBarText() {
        return sideBarText;
    }

    public void setSideBarText(String sideBarText) {
        this.sideBarText = sideBarText;
    }

    public String getIntroductoryText() {
        return introductoryText;
    }

    public void setIntroductoryText(String introductoryText) {
        this.introductoryText = introductoryText;
    }

    /**
     * Get the logo for the community. <code>null</code> is return if the
     * community does not have a logo.
     *
     * @return the logo of the community, or <code>null</code>
     */
    public Bitstream getLogo() {
        return logo;
    }

    void setLogo(Bitstream logo) {
        this.logo = logo;
        modified = true;
    }
}
