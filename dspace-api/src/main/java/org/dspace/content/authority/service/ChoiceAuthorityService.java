package org.dspace.content.authority.service;

import org.dspace.content.authority.Choices;

import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 27/05/14
 * Time: 15:41
 */
public interface ChoiceAuthorityService {

    public Choices getMatches(String schema, String element, String qualifier,
            String query, int collection, int start, int limit, String locale);

    public Choices getMatches(String fieldKey, String query, int collection,
            int start, int limit, String locale);

    public Choices getBestMatch(String fieldKey, String query, int collection,
            String locale);

    public String getLabel(String schema, String element, String qualifier,
                                                      String authKey, String locale);

    public String getLabel(String fieldKey, String authKey, String locale);

    public boolean isChoicesConfigured(String fieldKey);

    public String getPresentation(String fieldKey);

    public boolean isClosed(String fieldKey);

    public String makeFieldKey(String schema, String element, String qualifier);

    public String makeFieldKey(String dotty);

    public List<String> getVariants(String schema, String element, String qualifier, String authorityKey, String language);
}
