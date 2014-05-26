package org.dspace.workflow;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.service.InProgressSubmissionService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 23/05/14
 * Time: 15:57
 */
public interface WorkflowItemService<T extends WorkflowItem> extends InProgressSubmissionService<T> {

    public T create(Context context, Item item, Collection collection) throws SQLException, AuthorizeException;

    public T find(Context context, int id) throws SQLException;

    public List<T> findAll(Context context) throws SQLException;

    public List<T> findByCollection(Context context, Collection collection) throws SQLException;

    public T findByItem(Context context, Item item) throws SQLException;

    public List<T> findBySubmitter(Context context, EPerson ep) throws SQLException;

    public void deleteByCollection(Context context, Collection collection) throws SQLException, IOException, AuthorizeException;

}
