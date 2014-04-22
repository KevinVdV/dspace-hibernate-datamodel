package org.dspace.xmlworkflow.storedcomponents.dao.impl;

import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.dao.AbstractHibernateDAO;
import org.dspace.eperson.EPerson;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;
import org.dspace.xmlworkflow.storedcomponents.dao.XmlWorkflowItemDAO;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 21/04/14
 * Time: 09:59
 * To change this template use File | Settings | File Templates.
 */
public class XmlWorkflowItemDAOImpl extends AbstractHibernateDAO<XmlWorkflowItem> implements XmlWorkflowItemDAO {

    @Override
    public List<XmlWorkflowItem> findAllInCollection(Context context, Integer offset, Integer limit, Collection collection) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(XmlWorkflowItem.class);
        if(collection != null)
        {
            criteria.add(Restrictions.eq("collection", collection));
        }

        if(offset != null)
        {
            criteria.setFirstResult(offset);
        }
        if(limit != null)
        {
            criteria.setMaxResults(limit);
        }

        @SuppressWarnings("unchecked")
        List<XmlWorkflowItem> results = (List<XmlWorkflowItem>) criteria.list();
        return results;
    }

    @Override
    public int countAll(Context context) throws SQLException {
        return countAllInCollection(context, null);
    }

    @Override
    public int countAllInCollection(Context context, Collection collection) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(XmlWorkflowItem.class);
        if(collection != null)
        {
            criteria.add(Restrictions.eq("collection", collection));
        }
        return ((Long) criteria.setProjection(Projections.rowCount()).uniqueResult()).intValue();
    }

    @Override
    public List<XmlWorkflowItem> findBySubmitter(Context context, EPerson ep) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(XmlWorkflowItem.class);
        criteria.add(Restrictions.eq("item.submitter", ep));

        @SuppressWarnings("unchecked")
        List<XmlWorkflowItem> results = (List<XmlWorkflowItem>) criteria.list();
        return results;
    }

    @Override
    public List<XmlWorkflowItem> findByCollection(Context context, Collection collection) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(XmlWorkflowItem.class);
        criteria.add(Restrictions.eq("collection", collection));

        @SuppressWarnings("unchecked")
        List<XmlWorkflowItem> results = (List<XmlWorkflowItem>) criteria.list();
        return results;
    }

    @Override
    public XmlWorkflowItem findByItem(Context context, Item item) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(XmlWorkflowItem.class);
        criteria.add(Restrictions.eq("item", item));

        @SuppressWarnings("unchecked")
        XmlWorkflowItem result = (XmlWorkflowItem) criteria.uniqueResult();
        return result;
    }
}
