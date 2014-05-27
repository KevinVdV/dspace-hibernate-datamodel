package org.dspace.core.service;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 27/05/14
 * Time: 11:04
 */
public interface NewsService {

    public String readNewsFile(String newsFile);

    public String writeNewsFile(String newsFile, String news);

    public String getNewsFilePath();
}
