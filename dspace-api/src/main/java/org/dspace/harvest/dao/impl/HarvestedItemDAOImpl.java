package org.dspace.harvest.dao.impl;

import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.harvest.HarvestedItem;
import org.dspace.harvest.dao.HarvestedItemDAO;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 23/04/14
 * Time: 12:25
 */
public class HarvestedItemDAOImpl extends AbstractHibernateDAO<HarvestedItem> implements HarvestedItemDAO {

    @Override
    public HarvestedItem findByItem(Context context, Item item) throws SQLException {
        Criteria criteria = createCriteria(context, HarvestedItem.class);
        criteria.add(Restrictions.eq("item", item));
        return uniqueResult(criteria);
    }

    @Override
    public HarvestedItem findByOAIId(Context context, String itemOaiID, Collection collection) throws SQLException {
        Criteria criteria = createCriteria(context, HarvestedItem.class);
        criteria.add(
                Restrictions.and(
                        Restrictions.eq("oaiId", itemOaiID),
                        Restrictions.eq("item.owningCollection", collection)
                )
        );
        return uniqueResult(criteria);
    }
}
