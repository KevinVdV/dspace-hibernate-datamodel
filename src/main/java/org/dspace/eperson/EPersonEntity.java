package org.dspace.eperson;

import org.dspace.content.DSpaceObjectEntity;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by kevin on 08/02/14.
 */
@Entity
@Table(name="eperson")
public class EPersonEntity extends DSpaceObjectEntity {

    @Id
    @Column(name="eperson_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE ,generator="my_seq")
    @SequenceGenerator(name="my_seq", sequenceName="eperson_seq")
    private Integer id;

    @Column(name="language")
    private String language;

    @Column(name="netid")
    private String netid;

    @Column(name="last_active")
    private Date last_active;

    @Column(name="can_log_in", nullable = true)
    private Boolean can_log_in;

    @Column(name="lastname")
    private String lastname;

    @Column(name="firstname")
    private String firstname;

    @Column(name="email")
    private String email;

    @Column(name="require_certificate", nullable = true)
    private Boolean require_certificate;

    @Column(name="self_registered", nullable = true)
    private Boolean self_registered;

    @Column(name="password")
    private String password;

    @Column(name="salt")
    private String salt;

    @Column(name="digest_algorithm")
    private String digest_algorithm;

    //TODO: Hibernate move to enum ?
    @Column(name="phone")
    private String phone;


    //TODO: HIBERNATE: modified get it out of here ?
    /** Flag set when data is modified, for events */
    @Transient
    private boolean modified;

    /** Flag set when metadata is modified, for events */
    @Transient
    private boolean modifiedMetadata;

    public EPersonEntity() {
        //TODO: HIBERNATE CACHE CONTEXT
        //context.cache(this, row.getIntColumn("eperson_id"));
        modified = false;
        modifiedMetadata = false;
        // Cache ourselves
        //HIBERNATE: Implement the details !
        //clearDetails();
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
        final EPersonEntity other = (EPersonEntity) obj;
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
        //TODO: HIBERNATE mail to lowercase
        if (email != null)
        {
            email = email.toLowerCase();
        }

        this.email = email;
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
    public String getHandle() {
        return null;
    }

    @Override
    public String getName() {
        return getEmail();
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
        require_certificate = isrequired;
        modified = true;
    }

    /**
     * Get require certificate or not
     *
     * @return boolean, yes/no (or false if the column is an SQL NULL)
     */
    public boolean getRequireCertificate()
    {
        return require_certificate;
    }

    /**
     * Indicate whether the user self-registered
     *
     * @param self_registered boolean yes/no
     */
    public void setSelfRegistered(boolean self_registered)
    {
        this.self_registered = self_registered;
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
    public boolean getSelfRegistered()
    {
        return self_registered;
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

    public boolean isModified() {
        return modified;
    }

    public boolean isModifiedMetadata() {
        return modifiedMetadata;
    }

    public void setModifiedMetadata(boolean modifiedMetadata) {
        this.modifiedMetadata = modifiedMetadata;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
        modifiedMetadata = true;
    }
}
