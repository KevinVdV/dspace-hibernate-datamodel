package org.dspace.eperson;

import org.apache.commons.lang.StringUtils;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Constants;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.EPersonService;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by kevin on 08/02/14.
 */
@Entity
@Table(name="eperson", schema = "public")
public class EPerson extends DSpaceObject {

    @Id
    @Column(name="eperson_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="eperson_seq")
    @SequenceGenerator(name="eperson_seq", sequenceName="eperson_seq", allocationSize = 1)
    private Integer id;

    @Column(name="language", length = 64)
    private String language;

    @Column(name="netid", length = 64)
    private String netid;

    @Column(name="last_active")
    @Temporal(TemporalType.TIMESTAMP)
    private Date last_active;

    @Column(name="can_log_in", nullable = true)
    private Boolean can_log_in;

    @Column(name="lastname", length = 64)
    private String lastname;

    @Column(name="firstname", length = 64)
    private String firstname;

    @Column(name="email", unique=true, length = 64)
    private String email;

    @Column(name="require_certificate")
    private boolean requireCertificate = false;

    @Column(name="self_registered")
    private boolean selfRegistered = false;

    @Column(name="password", length = 128)
    private String password;

    @Column(name="salt", length = 32)
    private String salt;

    @Column(name="digest_algorithm", length = 16)
    private String digest_algorithm;

    @Column(name="phone", length = 32)
    private String phone;

    @Transient
    private EPersonService ePersonService = EPersonServiceFactory.getInstance().getEPersonService();


    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "epeople")
    private List<Group> groups = new ArrayList<Group>();

    /** Flag set when data is modified, for events */
    @Transient
    private boolean modified = false;

    /** Flag set when metadata is modified, for events */
    @Transient
    private boolean modifiedMetadata = false;

    public EPerson() {
    }

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
        final EPerson other = (EPerson) obj;
        if (this.getID() != other.getID())
        {
            return false;
        }
        if (!this.getEmail().equals(other.getEmail()))
        {
            return false;
        }
        if (!this.getFullName().equals(other.getFullName()))
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
        int hash = 5;
        hash = 89 * hash + this.getID();
        hash = 89 * hash + (this.getEmail() != null? this.getEmail().hashCode():0);
        hash = 89 * hash + (this.getFullName() != null? this.getFullName().hashCode():0);
        return hash;
    }



    /**
     * Get the e-person's full name, combining first and last name in a
     * displayable string.
     *
     * @return their full name (first + last name; if both are NULL, returns email)
     */
    public String getFullName()
    {
        String f = getFirstName();
        String l = getLastName();

        if ((l == null) && (f == null))
        {
            return getEmail();
        }
        else if (f == null)
        {
            return l;
        }
        else
        {
            return (f + " " + l);
        }
    }

    /**
     * Get the e-person's email address
     *
     * @return their email address (or null if the column is an SQL NULL)
     */
    public String getEmail()
    {
        return email;
    }

    /**
     * Set the EPerson's email
     *
     * @param email the new email
     */
    public void setEmail(String email)
    {
        this.email = StringUtils.lowerCase(email);
        modified = true;
    }



    /**
     * Get the e-person's language
     *
     * @return language code (or null if the column is an SQL NULL)
     */
    public String getLanguage()
    {
        return language;
    }

    /**
     * Get the e-person's internal identifier
     *
     * @return the internal identifier
     */
    public int getID()
    {
        return id;
    }

    @Override
    public int getType() {
        return Constants.EPERSON;
    }

    /**
     * Get the e-person's netid
     *
     * @return their netid (DB constraints ensure it's never NULL)
     */
    public String getNetid()
    {
        return netid;
    }


    /**
     * Set the EPerson's netid
     *
     * @param netid
     *            the new netid
     */
    public void setNetid(final String netid)
    {
        this.netid = netid;
        modified = true;
    }

    /**
     * Get the EPerson's last-active stamp.
     *
     * @return date when last logged on, or null.
     */
    public Date getLastActive()
    {
        return last_active;
    }


    /**
     * Stamp the EPerson's last-active date.
     *
     * @param last_active latest activity timestamp, or null to clear.
     */
    public void setLastActive(final Date last_active)
    {
        this.last_active = last_active;
    }

    /**
     * Indicate whether the user can log in
     *
     * @param login
     *            boolean yes/no
     */
    public void setCanLogIn(boolean login)
    {
        can_log_in = login;
        modified = true;
    }

    /**
     * Can the user log in?
     *
     * @return boolean, yes/no
     */
    public boolean canLogIn()
    {
        return can_log_in;
    }

    /**
     * Get the eperson's last name.
     *
     * @return their last name (or null if the column is an SQL NULL)
     */
    public String getLastName()
    {
        return lastname;
    }

    /**
     * Set the eperson's last name
     *
     * @param lastname
     *            the person's last name
     */
    public void setLastName(String lastname)
    {
        this.lastname = lastname;
        modified = true;
    }

    /**
     * Get the eperson's first name.
     *
     * @return their first name (or null if the column is an SQL NULL)
     */
    public String getFirstName()
    {
        return firstname;
    }

    /**
     * Set the eperson's first name
     *
     * @param firstname
     *            the person's first name
     */
    public void setFirstName(String firstname)
    {
        this.firstname = firstname;
        modified = true;
    }

    /**
     * Set require cert yes/no
     *
     * @param isrequired
     *            boolean yes/no
     */
    public void setRequireCertificate(final boolean isrequired)
    {
        requireCertificate = isrequired;
        modified = true;
    }

    /**
     * Get require certificate or not
     *
     * @return boolean, yes/no (or false if the column is an SQL NULL)
     */
    public Boolean getRequireCertificate()
    {
        return requireCertificate;
    }

    /**
     * Indicate whether the user self-registered
     *
     * @param self_registered boolean yes/no
     */
    public void setSelfRegistered(boolean self_registered)
    {
        this.selfRegistered = self_registered;
        modified = true;
    }

    /**
     * Set the EPerson's language.  Value is expected to be a Unix/POSIX
     * Locale specification of the form {language} or {language}_{territory},
     * e.g. "en", "en_US", "pt_BR" (the latter is Brazilian Portugese).
     *
     * @param language
     *            language code
     */
    public void setLanguage(String language)
    {
        this.language = language;
    }

    /**
     * Is the user self-registered?
     *
     * @return boolean, yes/no (or false if the column is an SQL NULL)
     */
    public Boolean getSelfRegistered()
    {
        return selfRegistered;
    }

    void setPassword(String password) {
        this.password = password;
        modified = true;
    }

    void setSalt(String salt) {
        this.salt = salt;
        modified = true;
    }

    void setDigest_algorithm(String digest_algorithm) {
        this.digest_algorithm = digest_algorithm;
        modified = true;
    }

    String getDigest_algorithm() {
        return digest_algorithm;
    }

    String getSalt() {
        return salt;
    }

    String getPassword() {
        return password;
    }

    boolean isModified() {
        return modified;
    }

    boolean isModifiedMetadata() {
        return modifiedMetadata;
    }

    void setModifiedMetadata(boolean modifiedMetadata) {
        this.modifiedMetadata = modifiedMetadata;
    }

    void setModified(boolean modified) {
        this.modified = modified;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
        modifiedMetadata = true;
    }

    public List<Group> getGroups() {
        return groups;
    }

    /*
        Getters & setters which should be removed on the long run, they are just here to provide all getters & setters to the item object
    */

    public String getName()
    {
        return ePersonService.getName(this);
    }
}
