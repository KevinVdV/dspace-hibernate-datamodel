package org.dspace.authorize;

import org.apache.commons.lang.ObjectUtils;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

import javax.persistence.*;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 22/02/14
 * Time: 13:40
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name="resourcepolicy", schema = "public")
//TODO: HIBERNATE, MAKE FUTURE PARENT OBJECT CONABLE
public class ResourcePolicy implements Cloneable{
    public static String TYPE_SUBMISSION = "TYPE_SUBMISSION";
    public static String TYPE_WORKFLOW = "TYPE_WORKFLOW";
    public static String TYPE_CUSTOM= "TYPE_CUSTOM";
    public static String TYPE_INHERITED= "TYPE_INHERITED";

    @Id
    @Column(name="policy_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="resourcepolicy_seq")
    @SequenceGenerator(name="resourcepolicy_seq", sequenceName="resourcepolicy_seq", allocationSize = 1)
    private Integer id;

    @Column(name="resource_type_id")
    private int resourceTypeId;

    @Column(name="resource_id")
    private int resourceId;

    @Column(name="action_id")
    private int actionId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eperson_id")
    private EPerson eperson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="epersongroup_id")
    private Group epersonGroup;

    @Column(name="start_date")
    private Date startDate;

    @Column(name="end_date")
    private Date endDate;

    @Column(name="rpname", length = 30)
    private String rpname;


    @Column(name="rptype", length = 30)
    private String rptype;

    @Column(name="rpdescription", length = 100)
    private String rpdescription;

    /**
     * Return true if this object equals obj, false otherwise.
     *
     * @param obj
     * @return true if ResourcePolicy objects are equal
     */
    @Override
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
        final ResourcePolicy other = (ResourcePolicy) obj;
        if (getAction() != other.getAction())
        {
            return false;
        }
        if (ObjectUtils.equals(getEPerson(), other.getEPerson()))
        {
            return false;
        }
        if (ObjectUtils.equals(getGroup(), other.getGroup()))
        {
            return false;
        }
        if (getStartDate() != other.getStartDate() && (this.getStartDate() == null || !this.getStartDate().equals(other.getStartDate())))
        {
            return false;
        }
        if (getEndDate() != other.getEndDate() && (this.getEndDate() == null || !this.getEndDate().equals(other.getEndDate())))
        {
            return false;
        }
        return true;
    }

    /**
     * Return a hash code for this object.
     *
     * @return int hash of object
     */
    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 19 * hash + this.getAction();
        if(this.getGroup() != null)
        {
            hash = 19 * hash + this.getGroup().hashCode();
        }else{
            hash = 19 * hash + -1;
        }

        if(this.epersonGroup != null)
        {
            hash = 19 * hash + this.getEPerson().hashCode();
        }else{
            hash = 19 * hash + -1;

        }

        hash = 19 * hash + (this.getStartDate() != null? this.getStartDate().hashCode():0);
        hash = 19 * hash + (this.getEndDate() != null? this.getEndDate().hashCode():0);
        return hash;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        ResourcePolicy clone = (ResourcePolicy) super.clone();
        // Only primitive types can be cloned by default, clone the object types
        clone.setGroup(this.getGroup());
        clone.setEPerson(this.getEPerson());
        clone.setStartDate((Date) ObjectUtils.clone(this.getStartDate()));
        clone.setEndDate((Date) ObjectUtils.clone(this.getEndDate()));

        return clone;
    }

    public Integer getID() {
        return id;
    }

    /**
     * Get the type of the objects referred to by policy
     *
     * @return type of object/resource
     */
    public int getResourceType() {
        return resourceTypeId;
    }

    /**
     * Set the type of the resource referred to by the policy
     *
     * @param mytype
     *            type of the resource
     */
    public void setResourceType(int mytype)
    {
        this.resourceTypeId = mytype;
    }

    /**
     * If the policy refers to a single resource, this is the ID of that
     * resource.
     *
     * @param myid   id of resource (database primary key)
     */
    public void setResourceID(int myid)
    {
        this.resourceId = myid;
    }

    /**
     * Get the ID of a resource pointed to by the policy (is null if policy
     * doesn't apply to a single resource.)
     *
     * @return resource_id
     */
    public int getResourceID()
    {
        return resourceId;
    }

    /**
     * set the action this policy authorizes
     *
     * @param myid  action ID from <code>org.dspace.core.Constants</code>
     */
    public void setAction(int myid)
    {
        this.actionId = myid;
    }

    /**
     * @return get the action this policy authorizes
     */
    public int getAction()
    {
        return actionId;
    }

    /**
     * @return eperson, null if EPerson not set
     */
    public EPerson getEPerson()
    {
        return eperson;
    }

    /**
     * assign an EPerson to this policy
     */
    public void setEPerson(EPerson eperson)
    {
        this.eperson = eperson;
    }

    /**
     * gets ID for Group referred to by this policy
     *
     * @return groupID, or null if no group set
     */
    public Group getGroup()
    {
        return epersonGroup;
    }

    /**
     * sets ID for Group referred to by this policy
     *
     * @return groupID, or null if no group set
     */
    public void setGroup(Group epersonGroup)
    {
        this.epersonGroup = epersonGroup;
    }

    /**
     * Get the start date of the policy
     *
     * @return start date, or null if there is no start date set (probably most
     *         common case)
     */
    public java.util.Date getStartDate()
    {
        return startDate;
    }

    /**
     * Set the start date for the policy
     *
     * @param d
     *            date, or null for no start date
     */
    public void setStartDate(java.util.Date d)
    {
        startDate = d;
    }

    /**
     * Get end date for the policy
     *
     * @return end date or null for no end date
     */
    public java.util.Date getEndDate()
    {
        return endDate;
    }

    /**
     * Set end date for the policy
     *
     * @param d
     *            end date, or null
     */
    public void setEndDate(java.util.Date d)
    {
        this.endDate = d;
    }

    public String getRpName(){
        return rpname;
    }
    public void setRpName(String name){
        this.rpname = name;
    }

    public String getRpType(){
        return rptype;
    }
    public void setRpType(String type){
        this.rptype = type;
    }

    public String getRpDescription(){
        return rpdescription;
    }
    public void setRpDescription(String description){
        this.rpdescription = description;
    }
}