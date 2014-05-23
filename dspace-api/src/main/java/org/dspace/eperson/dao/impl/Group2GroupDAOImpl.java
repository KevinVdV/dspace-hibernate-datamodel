package org.dspace.eperson.dao.impl;

import org.dspace.core.Context;
import org.dspace.dao.AbstractHibernateDAO;
import org.dspace.eperson.Group;
import org.dspace.eperson.Group2Group;
import org.dspace.eperson.dao.Group2GroupDAO;
import org.hibernate.Query;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 16/05/14
 * Time: 12:02
 */
public class Group2GroupDAOImpl extends AbstractHibernateDAO<Group2Group> implements Group2GroupDAO {

    @Override
    public void deleteByChild(Context context, Group child) throws SQLException {
        Query query = createQuery(context, "delete from Group2Group WHERE child=:child");
        query.setParameter("child", child);
        query.executeUpdate();
    }

    @Override
    public void deleteByParent(Context context, Group parent) throws SQLException {
        Query query = createQuery(context, "delete from Group2Group WHERE parent=:parent");
        query.setParameter("parent", parent);
        query.executeUpdate();
    }
}
