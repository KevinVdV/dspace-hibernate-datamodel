/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.embargo;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dspace.authorize.*;
import org.dspace.authorize.factory.AuthorizeServiceFactory;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.authorize.service.ResourcePolicyService;
import org.dspace.content.*;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.Constants;
import org.dspace.embargo.factory.EmbargoServiceFactory;
import org.dspace.embargo.service.EmbargoService;
import org.dspace.eperson.Group;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.GroupService;
import org.dspace.license.CreativeCommons;

/**
 * Default plugin implementation of the embargo setting function.
 * The parseTerms() provides only very rudimentary terms logic - entry
 * of a configurable string (in terms field) for 'unlimited' embargo, otherwise
 * a standard ISO 8601 (yyyy-mm-dd) date is assumed. Users are encouraged 
 * to override this method for enhanced functionality.
 *
 * @author Larry Stone
 * @author Richard Rodgers
 */
public class DefaultEmbargoSetter implements EmbargoSetter
{
    protected GroupService groupService = EPersonServiceFactory.getInstance().getGroupService();
    protected ResourcePolicyService resourcePolicyService = AuthorizeServiceFactory.getInstance().getResourcePolicyService();
    protected AuthorizeService authorizeService = AuthorizeServiceFactory.getInstance().getAuthorizeService();
    protected EmbargoService embargoService = EmbargoServiceFactory.getInstance().getEmbargoService();
    protected String termsOpen = null;
	
    public DefaultEmbargoSetter()
    {
        super();
        termsOpen = ConfigurationManager.getProperty("embargo.terms.open");
    }
    
    /**
     * Parse the terms into a definite date. Terms are expected to consist of
     * either: a token (value configured in 'embargo.terms.open' property) to indicate
     * indefinite embargo, or a literal lift date formatted in ISO 8601 format (yyyy-mm-dd)
     * 
     * @param context the DSpace context
     * @param item the item to embargo
     * @param terms the embargo terms
     * @return parsed date in DCDate format
     */
    @Override
    public DCDate parseTerms(Context context, Item item, String terms)
        throws SQLException, AuthorizeException
    {
    	if (terms != null && terms.length() > 0)
    	{
    		if (termsOpen.equals(terms))
            {
                return EmbargoServiceImpl.FOREVER;
            }
            else
            {
                return new DCDate(terms);
            }
    	}
        return null;
    }

    /**
     * Enforce embargo by turning off all read access to bitstreams in
     * this Item.
     *
     * @param context the DSpace context
     * @param item the item to embargo
     */
    @Override
    public void setEmbargo(Context context, Item item)
        throws SQLException, AuthorizeException
    {
        DCDate liftDate = embargoService.getEmbargoTermsAsDate(context, item);
        for (Bundle bn : item.getBundles())
        {
            // Skip the LICENSE and METADATA bundles, they stay world-readable
            String bnn = bn.getName();
            if (!(bnn.equals(Constants.LICENSE_BUNDLE_NAME) || bnn.equals(Constants.METADATA_BUNDLE_NAME) || bnn.equals(CreativeCommons.CC_BUNDLE_NAME)))
            {
                //AuthorizeManager.removePoliciesActionFilter(context, bn, Constants.READ);
                generatePolicies(context, liftDate.toDate(), null, bn, item.getOwningCollection());
                for (BundleBitstream bs : bn.getBitstreams())
                {
                    //AuthorizeManager.removePoliciesActionFilter(context, bs, Constants.READ);
                    generatePolicies(context, liftDate.toDate(), null, bs.getBitstream(), item.getOwningCollection());
                }
            }
        }
    }

    protected void generatePolicies(Context context, Date embargoDate,
                                        String reason, DSpaceObject dso, Collection owningCollection) throws SQLException, AuthorizeException {

        // add only embargo policy
        if(embargoDate!=null){

            List<Group> authorizedGroups = authorizeService.getAuthorizedGroups(context, owningCollection, Constants.DEFAULT_ITEM_READ);

            // look for anonymous
            boolean isAnonymousInPlace=false;
            for(Group g : authorizedGroups){
                if(StringUtils.equals(Group.ANONYMOUS, g.getName())){
                    isAnonymousInPlace=true;
                }
            }

            if(!isAnonymousInPlace){
                // add policies for all the groups
                for(Group g : authorizedGroups){
                    ResourcePolicy rp = authorizeService.createOrModifyPolicy(null, context, null, g, null, embargoDate, Constants.READ, reason, dso);
                    if(rp!=null)
                        resourcePolicyService.update(context, rp);
                }

            }
            else{
                // add policy just for anonymous
                ResourcePolicy rp = authorizeService.createOrModifyPolicy(null, context, null, groupService.findByName(context, Group.ANONYMOUS), null, embargoDate, Constants.READ, reason, dso);
                if(rp!=null)
                    resourcePolicyService.update(context, rp);
            }
        }

    }





    /**
     * Check that embargo is properly set on Item: no read access to bitstreams.
     *
     * @param context the DSpace context
     * @param item the item to embargo
     */
    @Override
    public void checkEmbargo(Context context, Item item)
        throws SQLException, AuthorizeException
    {
        for (Bundle bn : item.getBundles())
        {
            // Skip the LICENSE and METADATA bundles, they stay world-readable
            String bnn = bn.getName();
            if (!(bnn.equals(Constants.LICENSE_BUNDLE_NAME) || bnn.equals(Constants.METADATA_BUNDLE_NAME) || bnn.equals(CreativeCommons.CC_BUNDLE_NAME)))
            {
                // don't report on "TEXT" or "THUMBNAIL" bundles; those
                // can have READ long as the bitstreams in them do not.
                if (!(bnn.equals("TEXT") || bnn.equals("THUMBNAIL")))
                {
                    // check for ANY read policies and report them:
                    for (ResourcePolicy rp : authorizeService.getPoliciesActionFilter(context, bn, Constants.READ))
                    {
                        System.out.println("CHECK WARNING: Item "+item.getHandle(context)+", Bundle "+bn.getName()+" allows READ by "+
                          ((rp.getEPerson() == null) ? "Group "+rp.getGroup().getName() :
                                                      "EPerson "+rp.getEPerson().getFullName()));
                    }
                }

                for (BundleBitstream bundleBitstream : bn.getBitstreams())
                {
                    Bitstream bitstream = bundleBitstream.getBitstream();
                    for (ResourcePolicy rp : authorizeService.getPoliciesActionFilter(context, bitstream, Constants.READ))
                    {
                        System.out.println("CHECK WARNING: Item "+item.getHandle(context)+", Bitstream "+ bitstream.getName()+" (in Bundle "+bn.getName()+") allows READ by "+
                          ((rp.getEPerson() == null) ? "Group "+rp.getGroup().getName() :
                                                      "EPerson "+rp.getEPerson().getFullName()));
                    }
                }
            }
        }
    }
}