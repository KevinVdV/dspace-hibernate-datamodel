package org.dspace.identifier;

import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.identifier.dao.DoiDAO;
import org.dspace.identifier.doi.DOIIdentifierException;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;

/**
 * Created by kevin on 01/05/14.
 */
public class DoiServiceImpl implements DoiService {

    @Autowired(required = true)
    protected DoiDAO doiDAO;

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
        if (identifier.startsWith(DoiService.SCHEME)) {
            return identifier;
        }
        if (identifier.isEmpty()) {
            throw new IllegalArgumentException("Cannot format an empty identifier.");
        }
        if (identifier.startsWith("10.") && identifier.contains("/")) {
            return DoiService.SCHEME + identifier;
        }
        if (identifier.startsWith(RESOLVER + "/10.")) {
            return DoiService.SCHEME + identifier.substring(18);
        }
        throw new DOIIdentifierException(identifier + "does not seem to be a DOI.",
                DOIIdentifierException.UNRECOGNIZED);
    }

    @Override
    public DOI findByDoi(Context context, String doi) throws SQLException {
        return doiDAO.findByDoi(context, doi);
    }
}
