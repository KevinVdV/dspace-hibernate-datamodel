/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.eperson;

import java.sql.SQLException;
import java.util.*;

import org.apache.commons.codec.DecoderException;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.DSpaceObjectServiceImpl;
import org.dspace.content.Item;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.SubscriptionService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.core.Utils;
import org.dspace.eperson.dao.EPersonDAO;
import org.dspace.eperson.service.EPersonService;
import org.dspace.event.Event;
import org.dspace.workflow.WorkflowService;
import org.dspace.workflow.factory.WorkflowServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class representing an e-person.
 *
 * @author David Stuve
 * @version $Revision$
 */
public class EPersonServiceImpl extends DSpaceObjectServiceImpl<EPerson> implements EPersonService
{
    /** log4j logger */
    protected static Logger log = Logger.getLogger(EPersonServiceImpl.class);

    @Autowired(required= true)
    protected EPersonDAO ePersonDAO;

    @Autowired(required = true)
    protected SubscriptionService subscriptionService;

    @Autowired(required = true)
    protected ItemService itemService;

    @Autowired(required = true)
    protected AuthorizeService authorizeService;

    public EPersonServiceImpl()
    {
    }

    /**
     * Get an EPerson from the database.
     *
     * @param context
     *            DSpace context object
     * @param id
     *            ID of the EPerson
     *
     * @return the EPerson format, or null if the ID is invalid.
     */
    @Override
    public EPerson find(Context context, UUID id) throws SQLException
    {
        // First check the cache
        return ePersonDAO.findByID(context, EPerson.class, id);
    }

    @Override
    public String getName(EPerson dso) {
        return dso.getEmail();
    }

    /**
     * Find the eperson by their email address.
     *
     * @return EPerson, or {@code null} if none such exists.
     */
    @Override
    public EPerson findByEmail(Context context, String email) throws SQLException
    {
        if (email == null)
        {
            return null;
        }

        // All email addresses are stored as lowercase, so ensure that the email address is lowercased for the lookup
        return ePersonDAO.findByEmail(context, email);
    }

    /**
     * Find the eperson by their netid.
     *
     * @param context
     *            DSpace context
     * @param netid
     *            Network ID
     *
     * @return corresponding EPerson, or <code>null</code>
     */
    @Override
    public EPerson findByNetid(Context context, String netid) throws SQLException
    {
        if (netid == null)
        {
            return null;
        }

        return ePersonDAO.findByNetid(context, netid);
    }

    @Override
    public List<EPerson> findByGroups(Context context, Set<Group> groups) throws SQLException {
        return ePersonDAO.findByGroups(context, groups);
    }

    /**
     * Find the epeople that match the search query across firstname, lastname or email.
     *
     * @param context
     *            DSpace context
     * @param query
     *            The search string
     *
     * @return array of EPerson objects
     */
    @Override
    public List<EPerson> search(Context context, String query) throws SQLException
    {
        return search(context, query, -1, -1);
    }

    /**
     * Find the epeople that match the search query across firstname, lastname or email. 
     * This method also allows offsets and limits for pagination purposes. 
     *
     * @param context
     *            DSpace context
     * @param query
     *            The search string
     * @param offset
     *            Inclusive offset 
     * @param limit
     *            Maximum number of matches returned
     *
     * @return array of EPerson objects
     */
    @Override
    public List<EPerson> search(Context context, String query, int offset, int limit) throws SQLException
    {
        return ePersonDAO.search(context, query, offset, limit);
    }

    /**
     * Returns the total number of epeople returned by a specific query, without the overhead 
     * of creating the EPerson objects to store the results.
     *
     * @param context
     *            DSpace context
     * @param query
     *            The search string
     *
     * @return the number of epeople matching the query
     */
    @Override
    public int searchResultCount(Context context, String query) throws SQLException
    {
        return ePersonDAO.searchResultCount(context, query);

    }



    /**
     * Find all the epeople that match a particular query
     * <ul>
     * <li><code>ID</code></li>
     * <li><code>LASTNAME</code></li>
     * <li><code>EMAIL</code></li>
     * <li><code>NETID</code></li>
     * </ul>
     *
     * @return array of EPerson objects
     */
    @Override
    public List<EPerson> findAll(Context context, int sortField) throws SQLException
    {
        String sortColumn;

        switch (sortField)
        {
            case ID:
                sortColumn = "eperson_id";
                break;

            case EMAIL:
                sortColumn = "email";
                break;

            case LANGUAGE:
                sortColumn = "language";
                break;
            case NETID:
                sortColumn = "netid";
                break;

            default:
                sortColumn = "lastname";
        }
        return ePersonDAO.findAll(context, sortColumn);
    }

    /**
     * Create a new eperson
     *
     * @param context
     *            DSpace context object
     */
    @Override
    public EPerson create(Context context) throws SQLException, AuthorizeException
    {
        // authorized?
        if (!authorizeService.isAdmin(context))
        {
            throw new AuthorizeException(
                    "You must be an admin to create an EPerson");
        }


        EPerson e = ePersonDAO.create(context, new EPerson());

        log.info(LogManager.getHeader(context, "create_eperson", "eperson_id="
                + e.getID()));

        context.addEvent(new Event(Event.CREATE, Constants.EPERSON, e.getID(), null));

        return e;
    }

    /**
     * Delete an eperson
     *
     */
    @Override
    public void delete(Context context, EPerson ePerson) throws SQLException, AuthorizeException, EPersonDeletionException
    {
        // authorized?
        if (!authorizeService.isAdmin(context))
        {
            throw new AuthorizeException(
                    "You must be an admin to delete an EPerson");
        }

        // check for presence of eperson in tables that
        // have constraints on eperson_id
        List<String> constraintList = getDeleteConstraints(context, ePerson);

        // if eperson exists in tables that have constraints
        // on eperson, throw an exception
        if (constraintList.size() > 0)
        {
            throw new EPersonDeletionException(constraintList);
        }

        context.addEvent(new Event(Event.DELETE, Constants.EPERSON, ePerson.getID(), ePerson.getEmail()));

        // XXX FIXME: This sidesteps the object model code so it won't
        // generate  REMOVE events on the affected Groups.

        // Remove any group memberships first
        Iterator<Group> groups = ePerson.getGroups().iterator();
        while (groups.hasNext()) {
            Group group = groups.next();
            groups.remove();
            group.getEpeople().remove(ePerson);
        }

        subscriptionService.delete(context, ePerson);

        // Remove ourself
        //Clear the link to any groups we belong to
//        HibernateQueryUtil.refresh(myContext, ePersonEntity);
//        ePersonEntity.getGroups().clear();
        ePersonDAO.delete(context, ePerson);

        log.info(LogManager.getHeader(context, "delete_eperson",
                "eperson_id=" + ePerson.getID()));
    }

    /**
     * Set the EPerson's password.
     *
     * @param s
     *            the new password.
     */
    @Override
    public void setPassword(EPerson epersonEntity, String s)
    {
        PasswordHash hash = new PasswordHash(s);
        epersonEntity.setPassword(Utils.toHex(hash.getHash()));
        epersonEntity.setSalt(Utils.toHex(hash.getSalt()));
        epersonEntity.setDigest_algorithm(hash.getAlgorithm());
    }

    /**
     * Set the EPerson's password hash.
     *
     * @param password
     *          hashed password, or null to set row data to NULL.
     */
    @Override
    public void setPasswordHash(EPerson epersonEntity, PasswordHash password)
    {
        if (null == password)
        {
            epersonEntity.setDigest_algorithm(null);
            epersonEntity.setSalt(null);
            epersonEntity.setPassword(null);
        }
        else
        {
            epersonEntity.setDigest_algorithm(password.getAlgorithm());
            epersonEntity.setSalt(password.getSaltString());
            epersonEntity.setPassword(password.getHashString());
        }
    }

    /**
     * Return the EPerson's password hash.
     *
     * @return hash of the password, or null on failure (such as no password).
     */
    @Override
    public PasswordHash getPasswordHash(EPerson ePersonEntity)
    {
        PasswordHash hash = null;
        try {
            hash = new PasswordHash(ePersonEntity.getDigest_algorithm(),
                    ePersonEntity.getSalt(),
                    ePersonEntity.getPassword());
        } catch (DecoderException ex) {
            log.error("Problem decoding stored salt or hash:  " + ex.getMessage());
        }
        return hash;
    }

    /**
     * Check EPerson's password.  Side effect:  original unsalted MD5 hashes are
     * converted using the current algorithm.
     *
     * @param attempt
     *            the password attempt
     * @return boolean successful/unsuccessful
     */
    @Override
    public boolean checkPassword(Context context, EPerson ePersonEntity, String attempt)
    {
        PasswordHash myHash;
        try
        {
            myHash = new PasswordHash(
                    ePersonEntity.getDigest_algorithm(),
                    ePersonEntity.getSalt(),
                    ePersonEntity.getPassword());
        } catch (DecoderException ex)
        {
            log.error(ex.getMessage());
            return false;
        }
        boolean answer = myHash.matches(attempt);

        // If using the old unsalted hash, and this password is correct, update to a new hash
        if (answer && (null == ePersonEntity.getDigest_algorithm()))
        {
            log.info("Upgrading password hash for EPerson " + ePersonEntity.getID());
            setPassword(ePersonEntity, attempt);
            try {
                context.turnOffAuthorisationSystem();
                update(context, ePersonEntity);
            } catch (SQLException ex) {
                log.error("Could not update password hash", ex);
            } catch (AuthorizeException ex) {
                log.error("Could not update password hash", ex);
            } finally {
                context.restoreAuthSystemState();
            }
        }

        return answer;
    }

    @Override
    public void updateLastModified(Context context, EPerson ePerson) {
        // Not required for eperson
    }

    /**
     * Update the EPerson
     */
    @Override
    public void update(Context context, EPerson eperson) throws SQLException, AuthorizeException
    {
        // Check authorisation - if you're not the eperson
        // see if the authorization system says you can
        if (!context.ignoreAuthorization()
                && ((context.getCurrentUser() == null) || (eperson.getID() != context
                .getCurrentUser().getID())))
        {
            authorizeService.authorizeAction(context, eperson, Constants.WRITE);
        }

        ePersonDAO.save(context, eperson);

        log.info(LogManager.getHeader(context, "update_eperson",
                "eperson_id=" + eperson.getID()));

        if (eperson.isModified())
        {
            context.addEvent(new Event(Event.MODIFY, Constants.EPERSON, eperson.getID(), null));
            eperson.setModified(false);
        }
        if (eperson.isModifiedMetadata())
        {
            context.addEvent(new Event(Event.MODIFY_METADATA, Constants.EPERSON, eperson.getID(), eperson.getDetails()));
            eperson.setModifiedMetadata(false);
            eperson.clearDetails();
        }
    }

    /**
     * Check for presence of EPerson in tables that have constraints on
     * EPersons. Called by delete() to determine whether the eperson can
     * actually be deleted.
     *
     * An EPerson cannot be deleted if it exists in the item, workflowitem, or
     * tasklistitem tables.
     *
     * @return List of tables that contain a reference to the eperson.
     */
    protected List<String> getDeleteConstraints(Context context, EPerson ePerson) throws SQLException
    {
        List<String> tableList = new ArrayList<String>();


        // check for eperson in item table
        Iterator<Item> itemsBySubmitter = itemService.findBySubmitter(context, ePerson);
        if (itemsBySubmitter.hasNext())
        {
            tableList.add("item");
        }

        WorkflowService workflowService = WorkflowServiceFactory.getInstance().getWorkflowService();
        List<String> workflowConstraints = workflowService.getEPersonDeleteConstraints(context, ePerson);
        tableList.addAll(workflowConstraints);

        // the list of tables can be used to construct an error message
        // explaining to the user why the eperson cannot be deleted.
        return tableList;
    }
}