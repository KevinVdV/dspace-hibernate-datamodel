package org.dspace.content;

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
 * Time: 13:59
 */
@Entity
@Table(name="collection")
public class Collection extends DSpaceObject {

    @Id
    @Column(name="collection_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="collection_seq")
    @SequenceGenerator(name="collection_seq", sequenceName="collection_seq")
    private Integer id;

    @OneToOne
    @JoinColumn(name = "submitter")
    /** The default group of administrators */
    private Group submitters;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin")
    /** The default group of administrators */
    private Group admins;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_step_1")
    private Group workflowStep1;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_step_2")
    private Group workflowStep2;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_step_3")
    private Group workflowStep3;

    @Column(name = "name")
    private String name;

    @Column(name = "short_description")
    private String shortDescription;

    @Column(name = "provenance_description")
    private String provenanceDescription;

    @Column(name = "license")
    private String license;

    @Column(name = "copyright_text")
    private String copyrightText;

    @Column(name = "side_bar_text")
    private String sideBarText;

    @Column(name = "introductory_text")
    private String introductoryText;

    /** The logo bitstream */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logo_bitstream_id")
    private Bitstream logo;

    /** The item template */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_item_id")
    private Item template;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "community2collection",
            joinColumns = {@JoinColumn(name = "collection_id") },
            inverseJoinColumns = {@JoinColumn(name = "community_id") }
    )
    private List<Community> communities = new ArrayList<Community>();



    /** Flag set when data is modified, for events */
    @Transient
    private boolean modified;

    /** Flag set when metadata is modified, for events */
    @Transient
    private boolean modifiedMetadata;



    /**
    * Get the internal ID of this collection
    *
    * @return the internal identifier
    */
    @Override
    public int getID() {
        return id;
    }

    @Override
    public String getHandle(Context context) throws SQLException {
        //TODO: HIBERNATE: REMOVE THIS
        return HandleManager.findHandle(context, this);
    }

    /**
     * return type found in Constants
     *
     * @return int Constants.COLLECTION
     */
    @Override
    public int getType() {
        return Constants.COLLECTION;
    }

    /**
     * Return <code>true</code> if <code>other</code> is the same Collection
     * as this object, <code>false</code> otherwise
     *
     * @param other
     *            object to compare to
     *
     * @return <code>true</code> if object passed in represents the same
     *         collection as this object
     */
     @Override
     public boolean equals(Object other)
     {
         if (other == null)
         {
             return false;
         }
         if (getClass() != other.getClass())
         {
             return false;
         }
         final Collection otherCollection = (Collection) other;
         return this.getID() == otherCollection.getID();

     }

     @Override
     public int hashCode()
     {
         return new HashCodeBuilder().append(getID()).toHashCode();
     }

    /**
     * Get the default group of submitters, if there is one. Note that the
     * authorization system may allow others to submit to the collection, so
     * this is not necessarily a definitive list of potential submitters.
     * <P>
     * The default group of submitters for collection 100 is the one called
     * <code>collection_100_submit</code>.
     *
     * @return the default group of submitters, or <code>null</code> if there
     *         is no default group.
     */
    public Group getSubmitters() {
        return submitters;
    }

    /**
     * Set the default group of submitters
     *
     * Package protected in order to preven unauthorized calls to this method
     *
     * @param submitters the group of submitters
     */
    void setSubmitters(Group submitters) {
        this.submitters = submitters;
        modified = true;
    }

    /**
     * Get the default group of administrators, if there is one. Note that the
     * authorization system may allow others to be administrators for the
     * collection.
     * <P>
     * The default group of administrators for collection 100 is the one called
     * <code>collection_100_admin</code>.
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

    public Group getWorkflowStep1() {
        return workflowStep1;
    }

    public Group getWorkflowStep2() {
        return workflowStep2;
    }

    public Group getWorkflowStep3() {
        return workflowStep3;
    }

    public void setWorkflowStep1(Group workflowStep1) {
        this.workflowStep1 = workflowStep1;
        this.modified = true;
    }

    public void setWorkflowStep2(Group workflowStep2) {
        this.workflowStep2 = workflowStep2;
        this.modified = true;
    }

    public void setWorkflowStep3(Group workflowStep3) {
        this.workflowStep3 = workflowStep3;
        this.modified = true;
    }

    String getLicense() {
        return license;
    }

    /**
     * Set the license for this collection. Passing in <code>null</code> means
     * that the site-wide default will be used.
     *
     * @param license
     *            the license, or <code>null</code>
     */
    public void setLicense(String license) {
        this.license = license;
    }

    public boolean isModifiedMetadata() {
        return modifiedMetadata;
    }

    public void clearModifiedMetadata() {
        this.modifiedMetadata = false;
    }

    public boolean isModified() {
        return modified;
    }

    public void clearModified() {
        this.modified = false;
    }

    public String getSideBarText() {
        return sideBarText;
    }

    public void setSideBarText(String sideBarText) {
        this.sideBarText = sideBarText;
        modifiedMetadata = true;
    }

    public String getCopyrightText() {
        return copyrightText;
    }

    public void setCopyrightText(String copyrightText) {
        this.copyrightText = copyrightText;
        modifiedMetadata = true;
    }

    public String getProvenanceDescription() {
        return provenanceDescription;
    }

    public void setProvenanceDescription(String provenanceDescription) {
        this.provenanceDescription = provenanceDescription;
        modifiedMetadata = true;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
        modifiedMetadata = true;
    }

    public String getName() {
        return name;
    }

    @Override
    public void updateLastModified(Context context) {
        //Also fire a modified event since the collection HAS been modified
        context.addEvent(new Event(Event.MODIFY, Constants.COLLECTION, getID(), null));
    }

    protected void setName(String name) {
        this.name = name;
        modifiedMetadata = true;
    }

    public List<Community> getCommunities() {
        return communities;
    }

    protected void addCommunity(Community community) {
        this.communities.add(community);
    }

    protected void removeCommunity(Community community){
        this.communities.remove(community);
    }

    /**
     * Get the logo for the collection. <code>null</code> is returned if the
     * collection does not have a logo.
     *
     * @return the logo of the collection, or <code>null</code>
     */
    public Bitstream getLogo() {
        return logo;
    }

    protected void setLogo(Bitstream logo) {
        this.logo = logo;
        this.modified = true;
    }

    /**
     * Get the template item for this collection. <code>null</code> is
     * returned if the collection does not have a template. Submission
     * mechanisms may copy this template to provide a convenient starting point
     * for a submission.
     *
     * @return the item template, or <code>null</code>
     */
    public Item getTemplateItem() {
        return template;
    }

    protected void setTemplate(Item template) {
        this.template = template;
        modified = true;
    }

    public String getIntroductoryText() {
        return introductoryText;
    }

    public void setIntroductoryText(String introductoryText) {
        this.introductoryText = introductoryText;
    }
}
