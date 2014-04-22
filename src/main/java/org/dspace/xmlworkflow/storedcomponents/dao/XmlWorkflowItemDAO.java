package org.dspace.xmlworkflow.storedcomponents.dao;

import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.dao.GenericDAO;
import org.dspace.eperson.EPerson;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;

import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 21/04/14
 * Time: 09:51
 * To change this template use File | Settings | File Templates.
 */
public interface XmlWorkflowItemDAO extends GenericDAO<XmlWorkflowItem> {

    public List<XmlWorkflowItem> findAllInCollection(Context context, Integer offset, Integer limit, Collection collection) throws SQLException;

    public int countAll(Context context) throws SQLException;

    public int countAllInCollection(Context context, Collection collection) throws SQLException;

    public List<XmlWorkflowItem> findBySubmitter(Context context, EPerson ep) throws SQLException;

    public List<XmlWorkflowItem> findByCollection(Context context, Collection collection) throws SQLException;

    public XmlWorkflowItem findByItem(Context context, Item item) throws SQLException;
}
