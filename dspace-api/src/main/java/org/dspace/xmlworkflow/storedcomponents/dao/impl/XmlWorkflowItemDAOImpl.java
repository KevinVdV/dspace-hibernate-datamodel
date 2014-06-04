package org.dspace.xmlworkflow.storedcomponents.dao.impl;

import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.eperson.EPerson;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;
import org.dspace.xmlworkflow.storedcomponents.dao.XmlWorkflowItemDAO;
import org.hibernate.Criteria;
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
        Criteria criteria = createCriteria(context, XmlWorkflowItem.class);
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

        return list(criteria);
    }

    @Override
    public int countAll(Context context) throws SQLException {
        return countAllInCollection(context, null);
    }

    @Override
    public int countAllInCollection(Context context, Collection collection) throws SQLException {
        Criteria criteria = createCriteria(context, XmlWorkflowItem.class);
        if(collection != null)
        {
            criteria.add(Restrictions.eq("collection", collection));
        }
        return count(criteria);
    }

    @Override
    public List<XmlWorkflowItem> findBySubmitter(Context context, EPerson ep) throws SQLException {
        Criteria criteria = createCriteria(context, XmlWorkflowItem.class);
        criteria.add(Restrictions.eq("item.submitter", ep));

        return list(criteria);
    }

    @Override
    public List<XmlWorkflowItem> findByCollection(Context context, Collection collection) throws SQLException {
        Criteria criteria = createCriteria(context, XmlWorkflowItem.class);
        criteria.add(Restrictions.eq("collection", collection));

        return list(criteria);
    }

    @Override
    public XmlWorkflowItem findByItem(Context context, Item item) throws SQLException {
        Criteria criteria = createCriteria(context, XmlWorkflowItem.class);
        criteria.add(Restrictions.eq("item", item));

        return uniqueResult(criteria);
    }
}
