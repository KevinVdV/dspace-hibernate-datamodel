package org.dspace.identifier.service;

import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.identifier.DOI;
import org.dspace.identifier.IdentifierException;
import org.dspace.identifier.doi.DOIIdentifierException;
import org.dspace.service.DSpaceCRUDService;

import java.sql.SQLException;

/**
 * Created by kevin on 01/05/14.
 */
public interface DOIService extends DSpaceCRUDService<DOI>{

    public static final String SCHEME = "doi:";
    public static final String RESOLVER = "http://dx.doi.org";

    public String formatIdentifier(String identifier) throws DOIIdentifierException;

    public DOI findByDoi(Context context, String doi) throws SQLException;

    public DOI findDOIByDSpaceObject(Context context, DSpaceObject dso) throws SQLException;

    public String DOIFromExternalFormat(String identifier) throws DOIIdentifierException;

    public String DOIToExternalForm(String identifier) throws IdentifierException;
}
