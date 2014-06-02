package org.dspace.identifier;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.identifier.dao.DOIDAO;
import org.dspace.identifier.doi.DOIIdentifierException;
import org.dspace.identifier.service.DOIService;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kevin on 01/05/14.
 */
public class DOIServiceImpl implements DOIService {

    @Autowired(required = true)
    protected DOIDAO doiDAO;

    @Override
    public DOI create(Context context) throws SQLException, AuthorizeException {
        return doiDAO.create(context, new DOI());
    }

    @Override
    public DOI find(Context context, int id) throws SQLException {
        return doiDAO.findByID(context, DOI.class, id);
    }

    @Override
    public void update(Context context, DOI doi) throws SQLException {
        doiDAO.save(context, doi);
    }

    @Override
    public void delete(Context context, DOI doi) throws SQLException, AuthorizeException {
        doiDAO.delete(context, doi);
    }

    /**
     * Recognize format of DOI and return it with leading doi-Scheme.
     * @param identifier Identifier to format, following format are accepted:
     *                   f.e. 10.123/456, doi:10.123/456, http://dx.doi.org/10.123/456.
     * @return Given Identifier with DOI-Scheme, f.e. doi:10.123/456.
     * @throws IllegalArgumentException If identifier is empty or null.
     * @throws DOIIdentifierException If DOI could not be recognized.
     */
    @Override
    public String formatIdentifier(String identifier) throws DOIIdentifierException {
        if (null == identifier) {
            throw new IllegalArgumentException("Identifier is null.", new NullPointerException());
        }
        if (identifier.startsWith(DOIService.SCHEME)) {
            return identifier;
        }
        if (identifier.isEmpty()) {
            throw new IllegalArgumentException("Cannot format an empty identifier.");
        }
        if (identifier.startsWith("10.") && identifier.contains("/")) {
            return DOIService.SCHEME + identifier;
        }
        if (identifier.startsWith(RESOLVER + "/10.")) {
            return DOIService.SCHEME + identifier.substring(18);
        }
        throw new DOIIdentifierException(identifier + "does not seem to be a DOI.",
                DOIIdentifierException.UNRECOGNIZED);
    }

    @Override
    public DOI findByDoi(Context context, String doi) throws SQLException {
        return doiDAO.findByDoi(context, doi);
    }

    @Override
    public DOI findDOIByDSpaceObject(Context context, DSpaceObject dso) throws SQLException {
        return doiDAO.findDOIByDSpaceObject(context, dso);
    }

    @Override
    public String DOIFromExternalFormat(String identifier) throws DOIIdentifierException {
        Pattern pattern = Pattern.compile("^" + RESOLVER + "/+(10\\..*)$");
        Matcher matcher = pattern.matcher(identifier);
        if (matcher.find())
        {
            return SCHEME + matcher.group(1);
        }

        throw new DOIIdentifierException("Cannot recognize DOI!", DOIIdentifierException.UNRECOGNIZED);
    }

    @Override
    public String DOIToExternalForm(String identifier) throws IdentifierException {
        if (null == identifier)
            throw new IllegalArgumentException("Identifier is null.", new NullPointerException());
        if (identifier.isEmpty())
            throw new IllegalArgumentException("Cannot format an empty identifier.");
        if (identifier.startsWith(SCHEME))
            return RESOLVER + "/" + identifier.substring(SCHEME.length());
        if (identifier.startsWith("10.") && identifier.contains("/"))
            return RESOLVER + "/" + identifier;
        if (identifier.startsWith(RESOLVER + "/10."))
            return identifier;

        throw new IdentifierException(identifier + "does not seem to be a DOI.");
    }

}
