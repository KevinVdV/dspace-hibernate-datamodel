package org.dspace.authorize;

import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.eperson.Group;

import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 31/03/14
 * Time: 13:32
 */
public interface ResourcePolicyRepo {

    public ResourcePolicy find(Context context, int id) throws SQLException;

    public ResourcePolicy create(Context context) throws SQLException, AuthorizeException;

    public void delete(Context context, ResourcePolicy resourcePolicy) throws SQLException;

    public void setResource(ResourcePolicy resourcePolicy, DSpaceObject o);

    public String getActionText(ResourcePolicy resourcePolicy);

    public void setGroup(ResourcePolicy resourcePolicy, Group g);

    public boolean isDateValid(ResourcePolicy resourcePolicy);

    public void update(Context context, ResourcePolicy resourcePolicy) throws SQLException, AuthorizeException;
}
