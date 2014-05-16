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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.codec.DecoderException;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.DSpaceObjectServiceImpl;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.core.Utils;
import org.dspace.eperson.dao.EPersonDAO;
import org.dspace.eperson.service.EPersonService;
import org.dspace.event.Event;
import org.dspace.factory.DSpaceServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class representing an e-person.
 *
 * @author David Stuve
 * @version $Revision$
 */
public class EPersonServiceImpl extends DSpaceObjectServiceImpl<EPerson> implements EPersonService
{
    /** The e-mail field (for sorting) */
    public static final int EMAIL = 1;

    /** The last name (for sorting) */
    public static final int LASTNAME = 2;

    /** The e-mail field (for sorting) */
    public static final int ID = 3;

    /** The netid field (for sorting) */
    public static final int NETID = 4;

    /** The e-mail field (for sorting) */
    public static final int LANGUAGE = 5;

    /** log4j logger */
    protected static Logger log = Logger.getLogger(EPersonServiceImpl.class);

    @Autowired(required= true)
    protected EPersonDAO ePersonDAO;

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
    public EPerson find(Context context, int id) throws SQLException
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
        if (!AuthorizeManager.isAdmin(context))
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
    public void delete(Context context, EPerson ePersonEntity) throws SQLException, AuthorizeException, EPersonDeletionException
    {
        // authorized?
        if (!AuthorizeManager.isAdmin(context))
        {
            throw new AuthorizeException(
                    "You must be an admin to delete an EPerson");
        }

        // check for presence of eperson in tables that
        // have constraints on eperson_id
        //TODO: Hibernate delete when constraints are ready
        /*List<String> constraintList = getDeleteConstraints();

        // if eperson exists in tables that have constraints
        // on eperson, throw an exception
        if (constraintList.size() > 0)
        {
            throw new EPersonDeletionException(constraintList);
        }
        */
        context.addEvent(new Event(Event.DELETE, Constants.EPERSON, ePersonEntity.getID(), ePersonEntity.getEmail()));

        // XXX FIXME: This sidesteps the object model code so it won't
        // generate  REMOVE events on the affected Groups.

        // Remove any group memberships first
        //TODO: Hibernate delete when group is ready
        /*
        DatabaseManager.updateQuery(myContext,
                "DELETE FROM EPersonGroup2EPerson WHERE eperson_id= ? ",
                getID());

        // Remove any subscriptions
        DatabaseManager.updateQuery(myContext,
                "DELETE FROM subscription WHERE eperson_id= ? ",
                getID());

        */
        // Remove ourself
        //Clear the link to any groups we belong to
//        HibernateQueryUtil.refresh(myContext, ePersonEntity);
//        ePersonEntity.getGroups().clear();
        ePersonDAO.delete(context, ePersonEntity);

        log.info(LogManager.getHeader(context, "delete_eperson",
                "eperson_id=" + ePersonEntity.getID()));
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
            AuthorizeManager.authorizeAction(context, eperson, Constants.WRITE);
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
    //TODO: Hibernate fix this
    /*
    public List<String> getDeleteConstraints() throws SQLException
    {
        List<String> tableList = new ArrayList<String>();

        // check for eperson in item table
        TableRowIterator tri = DatabaseManager.query(myContext,
                "SELECT * from item where submitter_id= ? ",
                getID());

        try
        {
            if (tri.hasNext())
            {
                tableList.add("item");
            }
        }
        finally
        {
            // close the TableRowIterator to free up resources
            if (tri != null)
            {
                tri.close();
            }
        }

        if(ConfigurationManager.getProperty("workflow","workflow.framework").equals("xmlworkflow")){
            getXMLWorkflowConstraints(tableList);
        }else{
            getOriginalWorkflowConstraints(tableList);

        }
        // the list of tables can be used to construct an error message
        // explaining to the user why the eperson cannot be deleted.
        return tableList;
    }

    private void getXMLWorkflowConstraints(List<String> tableList) throws SQLException {
        TableRowIterator tri;
        // check for eperson in claimtask table
        tri = DatabaseManager.queryTable(myContext, "cwf_claimtask",
                "SELECT * from cwf_claimtask where owner_id= ? ",
                getID());

        try
        {
            if (tri.hasNext())
            {
                tableList.add("cwf_claimtask");
            }
        }
        finally
        {
            // close the TableRowIterator to free up resources
            if (tri != null)
            {
                tri.close();
            }
        }

        // check for eperson in pooltask table
        tri = DatabaseManager.queryTable(myContext, "cwf_pooltask",
                "SELECT * from cwf_pooltask where eperson_id= ? ",
                getID());

        try
        {
            if (tri.hasNext())
            {
                tableList.add("cwf_pooltask");
            }
        }
        finally
        {
            // close the TableRowIterator to free up resources
            if (tri != null)
            {
                tri.close();
            }
        }

        // check for eperson in workflowitemrole table
        tri = DatabaseManager.queryTable(myContext, "cwf_workflowitemrole",
                "SELECT * from cwf_workflowitemrole where eperson_id= ? ",
                getID());

        try
        {
            if (tri.hasNext())
            {
                tableList.add("cwf_workflowitemrole");
            }
        }
        finally
        {
            // close the TableRowIterator to free up resources
            if (tri != null)
            {
                tri.close();
            }
        }

    }

    private void getOriginalWorkflowConstraints(List<String> tableList) throws SQLException {
        TableRowIterator tri;
        // check for eperson in workflowitem table
        tri = DatabaseManager.query(myContext,
                "SELECT * from workflowitem where owner= ? ",
                getID());

        try
        {
            if (tri.hasNext())
            {
                tableList.add("workflowitem");
            }
        }
        finally
        {
            // close the TableRowIterator to free up resources
            if (tri != null)
            {
                tri.close();
            }
        }

        // check for eperson in tasklistitem table
        tri = DatabaseManager.query(myContext,
                "SELECT * from tasklistitem where eperson_id= ? ",
                getID());

        try
        {
            if (tri.hasNext())
            {
                tableList.add("tasklistitem");
            }
        }
        finally
        {
            // close the TableRowIterator to free up resources
            if (tri != null)
            {
                tri.close();
            }
        }
    }
*/

            //TODO: MOVE THIS CHUNK OF CODE OUT OF HERE
    /*
     * Commandline tool for manipulating EPersons.
     */

    private static final Option VERB_ADD = new Option("a", "add", false, "create a new EPerson");
    private static final Option VERB_DELETE = new Option("d", "delete", false, "delete an existing EPerson");
    private static final Option VERB_LIST = new Option("L", "list", false, "list EPersons");
    private static final Option VERB_MODIFY = new Option("M", "modify", false, "modify an EPerson");

    private static final Option OPT_GIVENNAME = new Option("g", "givenname", true, "the person's actual first or personal name");
    private static final Option OPT_SURNAME = new Option("s", "surname", true, "the person's actual last or family name");
    private static final Option OPT_PHONE = new Option("t", "telephone", true, "telephone number, empty for none");
    private static final Option OPT_LANGUAGE = new Option("l", "language", true, "the person's preferred language");
    private static final Option OPT_REQUIRE_CERTIFICATE = new Option("c", "requireCertificate", true, "if 'true', an X.509 certificate will be required for login");
    private static final Option OPT_CAN_LOGIN = new Option("C", "canLogIn", true, "'true' if the user can log in");

    private static final Option OPT_EMAIL = new Option("m", "email", true, "the user's email address, empty for none");
    private static final Option OPT_NETID = new Option("n", "netid", true, "network ID associated with the person, empty for none");

    private static final Option OPT_NEW_EMAIL = new Option("i", "newEmail", true, "new email address");
    private static final Option OPT_NEW_NETID = new Option("I", "newNetid", true, "new network ID");

    /**
     * Tool for manipulating user accounts.
     */
    public static void main(String argv[])
            throws ParseException, SQLException
    {
        final OptionGroup VERBS = new OptionGroup();
        VERBS.addOption(VERB_ADD);
        VERBS.addOption(VERB_DELETE);
        VERBS.addOption(VERB_LIST);
        VERBS.addOption(VERB_MODIFY);

        final Options globalOptions = new Options();
        globalOptions.addOptionGroup(VERBS);
        globalOptions.addOption("h", "help", false, "explain options");

        GnuParser parser = new GnuParser();
        CommandLine command = parser.parse(globalOptions, argv, true);

        Context context = new Context();

        // Disable authorization since this only runs from the local commandline.
        context.turnOffAuthorisationSystem();


        int status = 0;
        if (command.hasOption(VERB_ADD.getOpt()))
        {
            status = cmdAdd(context, argv);
        }
        else if (command.hasOption(VERB_DELETE.getOpt()))
        {
            status = cmdDelete(context, argv);
        }
        else if (command.hasOption(VERB_MODIFY.getOpt()))
        {
            status = cmdModify(context, argv);
        }
        else if (command.hasOption(VERB_LIST.getOpt()))
        {
            status = cmdList(context, argv);
        }
        else if (command.hasOption('h'))
        {
            new HelpFormatter().printHelp("user [options]", globalOptions);
        }
        else
        {
            System.err.println("Unknown operation.");
            new HelpFormatter().printHelp("user [options]", globalOptions);
            context.abort();
            status = 1;
            throw new IllegalArgumentException();
        }

        if (context.isValid())
        {
            try {
                context.complete();
            } catch (SQLException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }

    /** Command to create an EPerson. */
    private static int cmdAdd(Context context, String[] argv)
    {
        Options options = new Options();

        options.addOption(VERB_ADD);

        final OptionGroup identityOptions = new OptionGroup();
        identityOptions.addOption(OPT_EMAIL);
        identityOptions.addOption(OPT_NETID);

        options.addOptionGroup(identityOptions);

        options.addOption(OPT_GIVENNAME);
        options.addOption(OPT_SURNAME);
        options.addOption(OPT_PHONE);
        options.addOption(OPT_LANGUAGE);
        options.addOption(OPT_REQUIRE_CERTIFICATE);

        Option option = new Option("p", "password", true, "password to match the EPerson name");
        options.addOption(option);

        options.addOption("h", "help", false, "explain --add options");

        // Rescan the command for more details.
        GnuParser parser = new GnuParser();
        CommandLine command;
        try {
            command = parser.parse(options, argv);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            return 1;
        }

        if (command.hasOption('h'))
        {
            new HelpFormatter().printHelp("user --add [options]", options);
            return 0;
        }

        // Check that we got sufficient credentials to define a user.
        if ((!command.hasOption(OPT_EMAIL.getOpt())) && (!command.hasOption(OPT_NETID.getOpt())))
        {
            System.err.println("You must provide an email address or a netid to identify the new user.");
            return 1;
        }

        if (!command.hasOption('p'))
        {
            System.err.println("You must provide a password for the new user.");
            return 1;
        }

        // Create!
        EPerson eperson = null;
        EPersonService epersonService = DSpaceServiceFactory.getInstance().getEPersonService();
        try {
            eperson = epersonService.create(context);
        } catch (SQLException ex) {
            context.abort();
            System.err.println(ex.getMessage());
            return 1;
        } catch (AuthorizeException ex) { /* XXX SNH */ }
        eperson.setCanLogIn(true);
        eperson.setSelfRegistered(false);

        eperson.setEmail(command.getOptionValue(OPT_EMAIL.getOpt()));
        eperson.setFirstName(command.getOptionValue(OPT_GIVENNAME.getOpt()));
        eperson.setLastName(command.getOptionValue(OPT_SURNAME.getOpt()));
        eperson.setLanguage(command.getOptionValue(OPT_LANGUAGE.getOpt(),
                Locale.getDefault().getLanguage()));
        eperson.setPhone(command.getOptionValue(OPT_PHONE.getOpt()));
        eperson.setNetid(command.getOptionValue(OPT_NETID.getOpt()));
        eperson.setPassword(command.getOptionValue('p'));
        if (command.hasOption(OPT_REQUIRE_CERTIFICATE.getOpt()))
        {
            eperson.setRequireCertificate(Boolean.valueOf(command.getOptionValue(
                    OPT_REQUIRE_CERTIFICATE.getOpt())));
        }
        else
        {
            eperson.setRequireCertificate(false);
        }

        try {
            epersonService.update(context, eperson);
            context.commit();
            System.out.printf("Created EPerson %d\n", eperson.getID());
        } catch (SQLException ex) {
            context.abort();
            System.err.println(ex.getMessage());
            return 1;
        } catch (AuthorizeException ex) { /* XXX SNH */ }

        return 0;
    }

    /** Command to delete an EPerson. */
    private static int cmdDelete(Context context, String[] argv)
    {
        Options options = new Options();

        options.addOption(VERB_DELETE);

        final OptionGroup identityOptions = new OptionGroup();
        identityOptions.addOption(OPT_EMAIL);
        identityOptions.addOption(OPT_NETID);

        options.addOptionGroup(identityOptions);

        options.addOption("h", "help", false, "explain --delete options");

        GnuParser parser = new GnuParser();
        CommandLine command;
        try {
            command = parser.parse(options, argv);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            return 1;
        }

        if (command.hasOption('h'))
        {
            new HelpFormatter().printHelp("user --delete [options]", options);
            return 0;
        }

        // Delete!
        EPersonService epersonService = DSpaceServiceFactory.getInstance().getEPersonService();
        EPerson eperson = null;
        try {
            if (command.hasOption(OPT_NETID.getOpt()))
            {
                eperson = epersonService.findByNetid(context, command.getOptionValue(OPT_NETID.getOpt()));
            }
            else if (command.hasOption(OPT_EMAIL.getOpt()))
            {
                eperson = epersonService.findByEmail(context, command.getOptionValue(OPT_EMAIL.getOpt()));
            }
            else
            {
                System.err.println("You must specify the user's email address or netid.");
                return 1;
            }
        } catch (SQLException e) {
            System.err.append(e.getMessage());
            return 1;
        }

        if (null == eperson)
        {
            System.err.println("No such EPerson");
            return 1;
        }

        try {
            epersonService.delete(context, eperson);
            context.commit();
            System.out.printf("Deleted EPerson %d\n", eperson.getID());
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            return 1;
        } catch (AuthorizeException ex) {
            System.err.println(ex.getMessage());
            return 1;
        } catch (EPersonDeletionException ex) {
            System.err.println(ex.getMessage());
            return 1;
        }

        return 0;
    }

    /** Command to modify an EPerson. */
    private static int cmdModify(Context context, String[] argv)
    {
        Options options = new Options();

        options.addOption(VERB_MODIFY);

        final OptionGroup identityOptions = new OptionGroup();
        identityOptions.addOption(OPT_EMAIL);
        identityOptions.addOption(OPT_NETID);

        options.addOptionGroup(identityOptions);

        options.addOption(OPT_GIVENNAME);
        options.addOption(OPT_SURNAME);
        options.addOption(OPT_PHONE);
        options.addOption(OPT_LANGUAGE);
        options.addOption(OPT_REQUIRE_CERTIFICATE);

        options.addOption(OPT_CAN_LOGIN);
        options.addOption(OPT_NEW_EMAIL);
        options.addOption(OPT_NEW_NETID);

        options.addOption("h", "help", false, "explain --modify options");

        GnuParser parser = new GnuParser();
        CommandLine command;
        try {
            command = parser.parse(options, argv);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            return 1;
        }

        if (command.hasOption('h'))
        {
            new HelpFormatter().printHelp("user --modify [options]", options);
            return 0;
        }

        EPersonService epersonService = DSpaceServiceFactory.getInstance().getEPersonService();
        // Modify!
        EPerson eperson = null;
        try {
            if (command.hasOption(OPT_NETID.getOpt()))
            {
                eperson = epersonService.findByNetid(context, command.getOptionValue(OPT_NETID.getOpt()));
            }
            else if (command.hasOption(OPT_EMAIL.getOpt()))
            {
                eperson = epersonService.findByEmail(context, command.getOptionValue(OPT_EMAIL.getOpt()));
            }
            else
            {
                System.err.println("No EPerson selected");
                return 1;
            }
        } catch (SQLException e) {
            System.err.append(e.getMessage());
            return 1;
        }

        boolean modified = false;
        if (null == eperson)
        {
            System.err.println("No such EPerson");
            return 1;
        }
        else
        {
            if (command.hasOption(OPT_NEW_EMAIL.getOpt()))
            {
                eperson.setEmail(command.getOptionValue(OPT_NEW_EMAIL.getOpt()));
                modified = true;
            }
            if (command.hasOption(OPT_NEW_NETID.getOpt()))
            {
                eperson.setNetid(command.getOptionValue(OPT_NEW_NETID.getOpt()));
                modified = true;
            }
            if (command.hasOption(OPT_GIVENNAME.getOpt()))
            {
                eperson.setFirstName(command.getOptionValue(OPT_GIVENNAME.getOpt()));
                modified = true;
            }
            if (command.hasOption(OPT_SURNAME.getOpt()))
            {
                eperson.setLastName(command.getOptionValue(OPT_SURNAME.getOpt()));
                modified = true;
            }
            if (command.hasOption(OPT_PHONE.getOpt()))
            {
                eperson.setPhone(command.getOptionValue(OPT_PHONE.getOpt()));
                modified = true;
            }
            if (command.hasOption(OPT_LANGUAGE.getOpt()))
            {
                eperson.setLanguage(command.getOptionValue(OPT_LANGUAGE.getOpt()));
                modified = true;
            }
            if (command.hasOption(OPT_REQUIRE_CERTIFICATE.getOpt()))
            {
                eperson.setRequireCertificate(Boolean.valueOf(command.getOptionValue(
                        OPT_REQUIRE_CERTIFICATE.getOpt())));
                modified = true;
            }
            if (command.hasOption(OPT_CAN_LOGIN.getOpt()))
            {
                eperson.setCanLogIn(Boolean.valueOf(command.getOptionValue(OPT_CAN_LOGIN.getOpt())));
                modified = true;
            }
            if (modified)
            {
                try {
                    epersonService.update(context, eperson);
                    context.commit();
                    System.out.printf("Modified EPerson %d\n", eperson.getID());
                } catch (SQLException ex) {
                    context.abort();
                    System.err.println(ex.getMessage());
                    return 1;
                } catch (AuthorizeException ex) { /* XXX SNH */ }
            }
            else
            {
                System.out.println("No changes.");
            }
        }

        return 0;
    }

    /** Command to list known EPersons. */
    private static int cmdList(Context context, String[] argv)
    {
        // XXX ideas:
        // specific user/netid
        // wild or regex match user/netid
        // select details (pseudo-format string)
        try {
            EPersonService epersonService = DSpaceServiceFactory.getInstance().getEPersonService();
            for (EPerson person : epersonService.findAll(context, EMAIL))
            {
                System.out.printf("%d\t%s/%s\t%s, %s\n",
                        person.getID(),
                        person.getEmail(),
                        person.getNetid(),
                        person.getLastName(), person.getFirstName()); // TODO more user details
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            return 1;
        }

        return 0;
    }
}