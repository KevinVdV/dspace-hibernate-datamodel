package org.dspace.content.dao;

import org.dspace.content.DSpaceObject;
import org.dspace.dao.GenericDAO;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 3/06/14
 * Time: 13:12
 */
public interface DSpaceObjectDAO<T extends DSpaceObject> extends GenericDAO<T> {
}
