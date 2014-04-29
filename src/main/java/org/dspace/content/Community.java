package org.dspace.content;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.service.CommunityService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.Group;
import org.dspace.factory.DSpaceServiceFactory;
import org.dspace.handle.HandleServiceImpl;
import org.dspace.handle.service.HandleService;

import javax.persistence.*;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "community2community",
            joinColumns = {@JoinColumn(name = "parent_comm_id") },
            inverseJoinColumns = {@JoinColumn(name = "child_comm_id") }
    )
    private List<Community> parentCommunities;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "parentCommunities")
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

    @Transient
    private CommunityService communityService = DSpaceServiceFactory.getInstance().getCommunityService();

    @Transient
    private HandleService handleService = DSpaceServiceFactory.getInstance().getHandleService();


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
     * @param obj
     *            object to compare to
     *
     * @return <code>true</code> if object passed in represents the same
     *         community as this object
     */
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final Community other = (Community) obj;
        if (this.getType() != other.getType())
        {
            return false;
        }
        if (this.getID() != other.getID())
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash += 71 * hash + getType();
        hash += 71 * hash + getID();
        return hash;
    }

    /**
     * @see org.dspace.content.DSpaceObject#getHandle(Context context)
     */
    public String getHandle(Context context) throws SQLException {
        return handleService.findHandle(context, this);
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
    public List<Community> getParentCommunities() {
        return parentCommunities;
    }


    void setParentCommunities(List<Community> parentCommunities) {
        this.parentCommunities = parentCommunities;
    }

    boolean isModified() {
        return modified;
    }

    boolean isModifiedMetadata() {
        return modifiedMetadata;
    }

    void clearModifiedMetadata() {
        this.modifiedMetadata = false;
    }

    void clearModified() {
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

    String getNameInternal() {
        return name;
    }

    void setNameInternal(String name) {
        this.name = name;
        addDetails("name");
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


    /*
        Getters & setters which should be removed on the long run, they are just here to provide all getters & setters to the item object
     */


    public final Bitstream setLogo(Context context, InputStream is) throws AuthorizeException,
                IOException, SQLException
    {
        return communityService.setLogo(context, this, is);
    }

    public final void setName(String value)throws MissingResourceException{
        communityService.setName(this, value);
    }

    public final String getName()
    {
        return communityService.getName(this);
    }

    public final Community getParentCommunity() throws SQLException {
        return (Community) communityService.getParentObject(this);
    }
}
