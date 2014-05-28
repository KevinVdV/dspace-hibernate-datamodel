package org.dspace.content.authority.service;

import org.dspace.content.MetadataField;

import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 28/05/14
 * Time: 10:13
 */
public interface MetadataAuthorityService {

    public boolean isAuthorityControlled(MetadataField metadataField);

    public boolean isAuthorityControlled(String schema, String element, String qualifier);

    public boolean isAuthorityControlled(String fieldKey);

    public boolean isAuthorityRequired(MetadataField metadataField);

    public boolean isAuthorityRequired(String schema, String element, String qualifier);

    public boolean isAuthorityRequired(String fieldKey);

    public String makeFieldKey(String schema, String element, String qualifier);

    public int getMinConfidence(String schema, String element, String qualifier);

    public List<String> getAuthorityMetadata();
}
