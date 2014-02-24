package org.dspace.authorize;

import javax.persistence.*;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 22/02/14
 * Time: 13:40
 * To change this template use File | Settings | File Templates.
 */
public class ResourcePolicy {
    public static String TYPE_SUBMISSION = "TYPE_SUBMISSION";
    public static String TYPE_WORKFLOW = "TYPE_WORKFLOW";
    public static String TYPE_CUSTOM= "TYPE_CUSTOM";
    public static String TYPE_INHERITED= "TYPE_INHERITED";

    @Id
    @Column(name="policy_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="resourcepolicy_seq")
    @SequenceGenerator(name="resourcepolicy_seq", sequenceName="resourcepolicy_seq")
    private Integer id;

    @Column(name="resource_type_id")
    private int resource_type_id;

    @Column(name="resource_id")
    private int resource_id;

    @Column(name="action_id")
    private int action_id;

    @Column(name="eperson_id")
    private Integer eperson_id;

    @Column(name="epersongroup_id")
    private Integer epersongroup_id;

    @Column(name="start_date")
    private Date start_date;

    @Column(name="end_date")
    private Date end_date;

    @Column(name="rpname")
    private String rpname;


    @Column(name="rptype")
    private String rptype;

    @Column(name="rpdescription")
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
        if (getEPersonID() != other.getEPersonID())
        {
            return false;
        }
        if (getGroupID() != other.getGroupID())
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
        hash = 19 * hash + (this.getEPersonID());
        hash = 19 * hash + (this.getGroupID());
        hash = 19 * hash + (this.getStartDate() != null? this.getStartDate().hashCode():0);
        hash = 19 * hash + (this.getEndDate() != null? this.getEndDate().hashCode():0);
        return hash;
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
        return resource_type_id;
    }

    /**
     * Set the type of the resource referred to by the policy
     *
     * @param mytype
     *            type of the resource
     */
    public void setResourceType(int mytype)
    {
        this.resource_type_id = mytype;
    }

    /**
     * If the policy refers to a single resource, this is the ID of that
     * resource.
     *
     * @param myid   id of resource (database primary key)
     */
    public void setResourceID(int myid)
    {
        this.resource_id = myid;
    }

    /**
     * Get the ID of a resource pointed to by the policy (is null if policy
     * doesn't apply to a single resource.)
     *
     * @return resource_id
     */
    public int getResourceID()
    {
        return resource_id;
    }

    /**
     * set the action this policy authorizes
     *
     * @param myid  action ID from <code>org.dspace.core.Constants</code>
     */
    public void setAction(int myid)
    {
        this.action_id = myid;
    }

    /**
     * @return get the action this policy authorizes
     */
    public int getAction()
    {
        return action_id;
    }

    /**
     * @return eperson ID, or -1 if EPerson not set
     */
    public int getEPersonID()
    {
        return eperson_id;
    }

    /**
     * assign an EPerson to this policy
     */
    public void setEPersonID(Integer eperson_id)
    {
        if(eperson_id == null)
        {
            eperson_id = -1;
        }
        this.eperson_id = eperson_id;
    }

    /**
     * gets ID for Group referred to by this policy
     *
     * @return groupID, or -1 if no group set
     */
    public int getGroupID()
    {
        return epersongroup_id;
    }

    /**
     * sets ID for Group referred to by this policy
     *
     * @return groupID, or -1 if no group set
     */
    public void setGroupID(Integer epersongroup_id)
    {
        if(epersongroup_id == null)
        {
            epersongroup_id = -1;
        }
        this.epersongroup_id = epersongroup_id;
    }

    /**
     * Get the start date of the policy
     *
     * @return start date, or null if there is no start date set (probably most
     *         common case)
     */
    public java.util.Date getStartDate()
    {
        return start_date;
    }

    /**
     * Set the start date for the policy
     *
     * @param d
     *            date, or null for no start date
     */
    public void setStartDate(java.util.Date d)
    {
        start_date = d;
    }

    /**
     * Get end date for the policy
     *
     * @return end date or null for no end date
     */
    public java.util.Date getEndDate()
    {
        return end_date;
    }

    /**
     * Set end date for the policy
     *
     * @param d
     *            end date, or null
     */
    public void setEndDate(java.util.Date d)
    {
        this.end_date = d;
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