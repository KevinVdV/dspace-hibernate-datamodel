package org.dspace.content.dao.impl;

import org.dspace.content.WorkspaceItem;
import org.dspace.content.dao.SupervisedItemDAO;
import org.dspace.core.Context;
import org.dspace.dao.AbstractHibernateDAO;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 29/04/14
 * Time: 15:47
 */
public class SupervisedItemDAOImpl extends AbstractHibernateDAO<WorkspaceItem> implements SupervisedItemDAO {

    @Override
    public List<WorkspaceItem> findByEPerson(Context context, EPerson ePerson) throws SQLException {
        Criteria criteria = createCriteria(context, WorkspaceItem.class);
        criteria.createAlias("groups.epeople", "e");
        criteria.add(Restrictions.eq("e", ePerson));
        return list(criteria);
    }

    @Override
    public List<WorkspaceItem> findByGroup(Context context, Group group) throws SQLException {
        Criteria criteria = createCriteria(context, WorkspaceItem.class);
        criteria.createAlias("supervisorGroups", "sgs");
        criteria.add(Restrictions.eq("sgs.id",  group.getID()));
        return list(criteria);
    }

    @Override
    public WorkspaceItem findByWorkspaceItemAndGroup(Context context, WorkspaceItem workspaceItem, Group group) throws SQLException {
        Criteria criteria = createCriteria(context, WorkspaceItem.class);
        criteria.createAlias("supervisorGroups", "g");
        criteria.add(Restrictions.and(
                Restrictions.eq("g", group),
                Restrictions.eq("id", workspaceItem.getID())
        ));

        return uniqueResult(criteria);
    }



    @Override
    public List<WorkspaceItem> findAll(Context context, Class clazz) throws SQLException {
        Criteria criteria = createCriteria(context, WorkspaceItem.class);
        criteria.add(Restrictions.isNotEmpty("supervisorGroups"));
        return list(criteria);
    }
}
