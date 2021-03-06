package org.dspace.content.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.core.Context;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 16/03/14
 * Time: 10:21
 * To change this template use File | Settings | File Templates.
 */
public interface BundleService extends DSpaceObjectService<Bundle> {

    public Bundle create(Context context, Item item, String name) throws SQLException, AuthorizeException;

    public Bitstream getBitstreamByName(Bundle bundle, String name);

    public void addBitstream(Context context, Bundle bundle, Bitstream b) throws SQLException, AuthorizeException;

    public void setOrder(Context context, Bundle bundle, UUID bitstreamIds[]) throws AuthorizeException, SQLException;

    public void removeBitstream(Context context, Bundle bundle, Bitstream b) throws AuthorizeException, SQLException, IOException;

    public void delete(Context context, Bundle bundle) throws SQLException, AuthorizeException, IOException;

    public void inheritCollectionDefaultPolicies(Context context, Bundle bundle, Collection c)
            throws SQLException, AuthorizeException;

    public void replaceAllBitstreamPolicies(Context context, Bundle bundle, List<ResourcePolicy> newpolicies)
            throws SQLException, AuthorizeException;

    public List<ResourcePolicy> getBitstreamPolicies(Context context, Bundle bundle) throws SQLException;
}
