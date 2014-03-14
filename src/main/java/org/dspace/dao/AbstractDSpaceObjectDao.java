package org.dspace.dao;

import org.dspace.content.DSpaceObject;
import org.hibernate.criterion.Criterion;

import java.sql.SQLException;


/**
 * User: kevin (kevin at atmire.com)
 * Date: 14/03/14
 * Time: 14:23
 */
public abstract class AbstractDSpaceObjectDao<T extends DSpaceObject> extends AbstractHibernateDAO<T> {


}
