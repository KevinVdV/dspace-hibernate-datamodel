package org.dspace.embargo;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.dspace.content.DCDate;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.embargo.factory.EmbargoServiceFactory;
import org.dspace.embargo.service.EmbargoService;
import org.dspace.handle.factory.HandleServiceFactory;
import org.dspace.handle.service.HandleService;

import java.util.Date;
import java.util.Iterator;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 28/05/14
 * Time: 09:20
 */
public class EmbargoCLITool {

    private static final Logger log = Logger.getLogger(EmbargoCLITool.class);
    protected static final HandleService HANDLE_SERVICE = HandleServiceFactory.getInstance().getHandleService();
    protected static final EmbargoService EMBARGO_SERVICE = EmbargoServiceFactory.getInstance().getEmbargoService();

    /**
     * Command-line service to scan for every Item with an expired embargo,
     * and then lift that embargo.
     * <p>
     * Options:
     * <dl>
     *   <dt>-c,--check</dt>
     *   <dd>         Function: ONLY check the state of embargoed Items, do
     *                      NOT lift any embargoes.</dd>
     *   <dt>-h,--help</dt>
     *   <dd>         Help.</dd>
     *   <dt>-i,--identifier</dt>
     *   <dd>         Process ONLY this Handle identifier(s), which must be
     *                      an Item.  Can be repeated.</dd>
     *   <dt>-l,--lift</dt>
     *   <dd>         Function: ONLY lift embargoes, do NOT check the state
     *                      of any embargoed Items.</dd>
     *   <dt>-n,--dryrun</dt>
     *   <dd>         Do not change anything in the data model; print
     *                      message instead.</dd>
     *   <dt>-v,--verbose</dt>
     *   <dd>         Print a line describing action taken for each
     *                      embargoed Item found.</dd>
     *   <dt>-q,--quiet</dt>
     *   <dd>         No output except upon error.</dd>
     * </dl>
     */
    public static void main(String argv[])
    {
        int status = 0;

        Options options = new Options();
        options.addOption("v", "verbose", false,
                "Print a line describing action taken for each embargoed Item found.");
        options.addOption("q", "quiet", false,
                "Do not print anything except for errors.");
        options.addOption("n", "dryrun", false,
                "Do not change anything in the data model, print message instead.");
        options.addOption("i", "identifier", true,
                        "Process ONLY this Handle identifier(s), which must be an Item.  Can be repeated.");
        options.addOption("c", "check", false,
                        "Function: ONLY check the state of embargoed Items, do NOT lift any embargoes.");
        options.addOption("l", "lift", false,
                        "Function: ONLY lift embargoes, do NOT check the state of any embargoed Items.");

        options.addOption("a", "adjust", false,
                "Function: Adjust bitstreams policies");

        options.addOption("h", "help", false, "help");
        CommandLine line = null;
        try
        {
            line = new PosixParser().parse(options, argv);
        }
        catch(ParseException e)
        {
            System.err.println("Command error: " + e.getMessage());
            new HelpFormatter().printHelp(EmbargoServiceImpl.class.getName(), options);
            System.exit(1);
        }

        if (line.hasOption('h'))
        {
            new HelpFormatter().printHelp(EmbargoServiceImpl.class.getName(), options);
            System.exit(0);
        }

        // sanity check, --lift and --check are mutually exclusive:
        if (line.hasOption('l') && line.hasOption('c'))
        {
            System.err.println("Command error: --lift and --check are mutually exclusive, try --help for assistance.");
            System.exit(1);
        }

        Context context = null;
        try
        {
            context = new Context();
            context.setIgnoreAuthorization(true);
            Date now = new Date();

            // scan items under embargo
            if (line.hasOption('i'))
            {
                for (String handle : line.getOptionValues('i'))
                {
                    DSpaceObject dso = HANDLE_SERVICE.resolveToObject(context, handle);
                    if (dso == null)
                    {
                        System.err.println("Error, cannot resolve handle="+handle+" to a DSpace Item.");
                        status = 1;
                    }
                    else if (dso.getType() != Constants.ITEM)
                    {
                        System.err.println("Error, the handle="+handle+" is not a DSpace Item.");
                        status = 1;
                    }
                    else
                    {
                        if (processOneItem(context, (Item)dso, line, now))
                        {
                            status = 1;
                        }
                    }
                }
            }
            else
            {
                Iterator<Item> ii = EMBARGO_SERVICE.findItemsWithEmbargo(context);
                while (ii.hasNext())
                {
                    if (processOneItem(context, ii.next(), line, now))
                    {
                        status = 1;
                    }
                }
            }
            context.complete();
            context = null;
        }
        catch (Exception e)
        {
            System.err.println("ERROR, got exception: "+e);
            e.printStackTrace();
            status = 1;
        }
        finally
        {
            if (context != null)
            {
                try
                {
                    context.abort();
                }
                catch (Exception e)
                {
                }
            }
        }
        System.exit(status);
    }

    // lift or check embargo on one Item, handle exceptions
    // return false on success, true if there was fatal exception.
    protected static boolean processOneItem(Context context, Item item, CommandLine line, Date now)
        throws Exception
    {
        boolean status = false;

        DCDate liftDate = EMBARGO_SERVICE.getActualEmbargoLiftDate(item);

        if (liftDate != null)
        {
            // need to survive any failure on a single item, go on to process the rest.
            try
            {
                if (line.hasOption('a')){
                    EMBARGO_SERVICE.getSetter().setEmbargo(context, item);
                }
                else{
                    log.debug("Testing embargo on item="+item.getHandle(context)+", date="+liftDate.toString());
                    if (liftDate.toDate().before(now))
                    {
                        if (line.hasOption('v'))
                        {
                            System.err.println("Lifting embargo from Item handle=" + item.getHandle(context) + ", lift date=" + liftDate.toString());
                        }
                        if (line.hasOption('n'))
                        {
                            if (!line.hasOption('q'))
                            {
                                System.err.println("DRY RUN: would have lifted embargo from Item handle=" + item.getHandle(context) + ", lift date=" + liftDate.toString());
                            }
                        }
                        else if (!line.hasOption('c'))
                        {
                            EMBARGO_SERVICE.liftEmbargo(context, item);
                        }
                    }
                    else if (!line.hasOption('l'))
                    {
                        if (line.hasOption('v'))
                        {
                            System.err.println("Checking current embargo on Item handle=" + item.getHandle(context) + ", lift date=" + liftDate.toString());
                        }
                        EMBARGO_SERVICE.getSetter().checkEmbargo(context, item);
                    }
                }
            }
            catch (Exception e)
            {
                log.error("Failed attempting to lift embargo, item="+item.getHandle(context)+": ", e);
                System.err.println("Failed attempting to lift embargo, item="+item.getHandle(context)+": "+ e);
                status = true;
            }
        }
        return status;
    }



}
