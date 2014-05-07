package org.dspace.identifier;

import org.dspace.core.Context;
import org.dspace.identifier.doi.DOIIdentifierException;
import org.dspace.service.DSpaceCRUDService;

import java.sql.SQLException;

/**
 * Created by kevin on 01/05/14.
 */
public interface DoiService extends DSpaceCRUDService<DOI>{

    public static final String SCHEME = "doi:";
    public static final String RESOLVER = "http://dx.doi.org";

    public void update(Context context, DOI doi) throws SQLException;

    public String formatIdentifier(String identifier) throws DOIIdentifierException;

    public DOI findByDoi(Context context, String doi) throws SQLException;
}
