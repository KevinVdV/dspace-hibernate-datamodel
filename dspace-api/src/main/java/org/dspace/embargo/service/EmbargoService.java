package org.dspace.embargo.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DCDate;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.embargo.EmbargoSetter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 28/05/14
 * Time: 09:18
 */
public interface EmbargoService {

    public void setEmbargo(Context context, Item item) throws SQLException, AuthorizeException;

    public DCDate getEmbargoTermsAsDate(Context context, Item item) throws SQLException, AuthorizeException;

    public void liftEmbargo(Context context, Item item) throws SQLException, AuthorizeException, IOException;

    public EmbargoSetter getSetter();

    public DCDate getActualEmbargoLiftDate(Item item);

    public Iterator<Item> findItemsWithEmbargo(Context context) throws SQLException, AuthorizeException;
}
