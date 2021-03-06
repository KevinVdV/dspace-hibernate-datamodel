/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.license;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.BitstreamFormatService;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.BundleService;
import org.dspace.content.service.ItemService;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.Utils;

public class CreativeCommons
{
    /** log4j category */
    private static Logger log = Logger.getLogger(CreativeCommons.class);

    /**
     * The Bundle Name
     */
    public static final String CC_BUNDLE_NAME = "CC-LICENSE";

    private static final String CC_BS_SOURCE = "org.dspace.license.CreativeCommons";

    /**
     * Some BitStream Names (BSN)
     */
    private static final String BSN_LICENSE_URL = "license_url";

    private static final String BSN_LICENSE_TEXT = "license_text";

    private static final String BSN_LICENSE_RDF = "license_rdf";

    protected static final Templates templates;

    protected static final ItemService ITEM_SERVICE = ContentServiceFactory.getInstance().getItemService();
    protected static final BitstreamFormatService BITSTREAM_FORMAT_SERVICE = ContentServiceFactory.getInstance().getBitstreamFormatService();
    protected static final BitstreamService BITSTREAM_SERVICE = ContentServiceFactory.getInstance().getBitstreamService();
    protected static final BundleService BUNDLE_SERVICE = ContentServiceFactory.getInstance().getBundleService();

    static
    {
        // if defined, set a proxy server for http requests to Creative
        // Commons site
        String proxyHost = ConfigurationManager.getProperty("http.proxy.host");
        String proxyPort = ConfigurationManager.getProperty("http.proxy.port");

        if (StringUtils.isNotBlank(proxyHost) && StringUtils.isNotBlank(proxyPort))
        {
            System.setProperty("http.proxyHost", proxyHost);
            System.setProperty("http.proxyPort", proxyPort);
        }

        try
        {
            templates = TransformerFactory.newInstance().newTemplates(
                        new StreamSource(CreativeCommons.class
                                .getResourceAsStream("CreativeCommons.xsl")));
        }
        catch (TransformerConfigurationException e)
        {
            throw new RuntimeException(e.getMessage(),e);
        }


    }

    /**
     * Simple accessor for enabling of CC
     */
    public static boolean isEnabled()
    {
        return true;
    }

        // create the CC bundle if it doesn't exist
        // If it does, remove it and create a new one.
    private static Bundle getCcBundle(Context context, Item item)
        throws SQLException, AuthorizeException, IOException
    {
        List<Bundle> bundles = ITEM_SERVICE.getBundles(item, CC_BUNDLE_NAME);

        if ((bundles.size() > 0) && (bundles.get(0) != null))
        {
            ITEM_SERVICE.removeBundle(context, item, bundles.get(0));
        }
        return BUNDLE_SERVICE.create(context, item, CC_BUNDLE_NAME);
    }


     /** setLicenseRDF
     *
     * CC Web Service method for setting the RDF bitstream
     *
     */
    public static void setLicenseRDF(Context context, Item item, String licenseRdf)
    	throws SQLException, IOException,
            AuthorizeException
    {
        Bundle bundle = getCcBundle(context, item);
        // set the format
        BitstreamFormat bs_rdf_format = BITSTREAM_FORMAT_SERVICE.findByShortDescription(context, "RDF XML");
        // set the RDF bitstream
        setBitstreamFromBytes(context, bundle, BSN_LICENSE_RDF, bs_rdf_format, licenseRdf.getBytes());
    }


    /**
     * This is a bit of the "do-the-right-thing" method for CC stuff in an item
     */
    public static void setLicense(Context context, Item item,
            String cc_license_url) throws SQLException, IOException,
            AuthorizeException
    {
        Bundle bundle = getCcBundle(context, item);

        // get some more information
        String license_text = fetchLicenseText(cc_license_url);
        String license_rdf = fetchLicenseRDF(cc_license_url);

        // set the formats
        BitstreamFormat bs_url_format = BITSTREAM_FORMAT_SERVICE.findByShortDescription(
                context, "License");
        BitstreamFormat bs_text_format = BITSTREAM_FORMAT_SERVICE.findByShortDescription(
                context, "CC License");
        BitstreamFormat bs_rdf_format = BITSTREAM_FORMAT_SERVICE.findByShortDescription(
                context, "RDF XML");

        // set the URL bitstream
        setBitstreamFromBytes(context, bundle, BSN_LICENSE_URL, bs_url_format,
                cc_license_url.getBytes());

        // set the license text bitstream
        setBitstreamFromBytes(context, bundle, BSN_LICENSE_TEXT, bs_text_format,
                license_text.getBytes());

        // set the RDF bitstream
        setBitstreamFromBytes(context, bundle, BSN_LICENSE_RDF, bs_rdf_format,
                license_rdf.getBytes());
    }

    /**
     * Used by DSpaceMetsIngester
     *
     * @param context
     * @param item
     * @param licenseStm
     * @param mimeType
     * @throws SQLException
     * @throws IOException
     * @throws AuthorizeException
     *
     * * // PATCHED 12/01 FROM JIRA re: mimetypes for CCLicense and License RDF wjb
     */

    public static void setLicense(Context context, Item item,
                                  InputStream licenseStm, String mimeType)
            throws SQLException, IOException, AuthorizeException
    {
        Bundle bundle = getCcBundle(context, item);

     // set the format
        BitstreamFormat bs_format;
        if (mimeType.equalsIgnoreCase("text/xml"))
        {
        	bs_format = BITSTREAM_FORMAT_SERVICE.findByShortDescription(context, "CC License");
        } else if (mimeType.equalsIgnoreCase("text/rdf")) {
            bs_format = BITSTREAM_FORMAT_SERVICE.findByShortDescription(context, "RDF XML");
        } else {
        	bs_format = BITSTREAM_FORMAT_SERVICE.findByShortDescription(context, "License");
        }

        Bitstream bs = BITSTREAM_SERVICE.create(context, bundle, licenseStm);
        bs.setSource(CC_BS_SOURCE);
        bs.setName((mimeType != null &&
                (mimeType.equalsIgnoreCase("text/xml") ||
                        mimeType.equalsIgnoreCase("text/rdf"))) ?
                BSN_LICENSE_RDF : BSN_LICENSE_TEXT);
        BITSTREAM_SERVICE.setFormat(context, bs, bs_format);
        BUNDLE_SERVICE.update(context, bundle);
    }


    public static void removeLicense(Context context, Item item)
            throws SQLException, IOException, AuthorizeException
    {
        // remove CC license bundle if one exists
        List<Bundle> bundles = ITEM_SERVICE.getBundles(item, CC_BUNDLE_NAME);

        if ((bundles.size() > 0) && (bundles.get(0) != null))
        {
            ITEM_SERVICE.removeBundle(context, item, bundles.get(0));
        }
    }

    public static boolean hasLicense(Context context, Item item)
            throws SQLException, IOException
    {
        // try to find CC license bundle
        List<Bundle> bundles = ITEM_SERVICE.getBundles(item, CC_BUNDLE_NAME);

        if (bundles.size() == 0)
        {
            return false;
        }

        // verify it has correct contents
        try
        {
            if ((getLicenseURL(context, item) == null) || (getLicenseText(context, item) == null)
                    || (getLicenseRDF(context, item) == null))
            {
                return false;
            }
        }
        catch (AuthorizeException ae)
        {
            return false;
        }

        return true;
    }

    public static String getLicenseURL(Context context, Item item) throws SQLException,
            IOException, AuthorizeException
    {
        return getStringFromBitstream(context, item, BSN_LICENSE_URL);
    }

    public static String getLicenseText(Context context, Item item) throws SQLException,
            IOException, AuthorizeException
    {
        return getStringFromBitstream(context, item, BSN_LICENSE_TEXT);
    }

    public static String getLicenseRDF(Context context, Item item) throws SQLException,
            IOException, AuthorizeException
    {
        return getStringFromBitstream(context, item, BSN_LICENSE_RDF);
    }

    /**
     * Get Creative Commons license RDF, returning Bitstream object.
     * @return bitstream or null.
     */
    public static Bitstream getLicenseRdfBitstream(Item item) throws SQLException,
            IOException, AuthorizeException
    {
        return getBitstream(item, BSN_LICENSE_RDF);
    }

    /**
     * Get Creative Commons license Text, returning Bitstream object.
     * @return bitstream or null.
     */
    public static Bitstream getLicenseTextBitstream(Item item) throws SQLException,
            IOException, AuthorizeException
    {
        return getBitstream(item, BSN_LICENSE_TEXT);
    }

    public static String fetchLicenseRdf(String ccResult) {
    	StringWriter result 			= new StringWriter();
    	String licenseRdfString 		= new String("");
        try {
    		InputStream inputstream = new ByteArrayInputStream(ccResult.getBytes("UTF-8"));
    		templates.newTransformer().transform(new StreamSource(inputstream), new StreamResult(result));
    	} catch (TransformerException te) {
    		throw new RuntimeException("Transformer exception " + te.getMessage(), te);
    	} catch (IOException ioe) {
    		throw new RuntimeException("IOexception " + ioe.getCause().toString(), ioe);
    	} finally {
    		return result.getBuffer().toString();
    	}
    }


    /**
    *
    *  The next two methods are old CC.
    * Remains until prev. usages are eliminated.
    * @Deprecated
    *
    */
    /**
     * Get a few license-specific properties. We expect these to be cached at
     * least per server run.
     */
    public static String fetchLicenseText(String license_url)
    {
        String text_url = license_url;
        byte[] urlBytes = fetchURL(text_url);

        return (urlBytes != null) ? new String(urlBytes) : "";
    }

    public static String fetchLicenseRDF(String license_url)
    {
        StringWriter result = new StringWriter();

        try
        {
            templates.newTransformer().transform(
                    new StreamSource(license_url + "rdf"),
                    new StreamResult(result)
                    );
        }
        catch (TransformerException e)
        {
            throw new IllegalStateException(e.getMessage(),e);
        }

        return result.getBuffer().toString();
    }

    // The following two helper methods assume that the CC
    // bitstreams are short and easily expressed as byte arrays in RAM

    /**
     * This helper method takes some bytes and stores them as a bitstream for an
     * item, under the CC bundle, with the given bitstream name
     */
    private static void setBitstreamFromBytes(Context context, Bundle bundle,
            String bitstream_name, BitstreamFormat format, byte[] bytes)
            throws SQLException, IOException, AuthorizeException
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        Bitstream bs = BITSTREAM_SERVICE.create(context, bundle, bais);

        bs.setName(bitstream_name);
        bs.setSource(CC_BS_SOURCE);
        BITSTREAM_SERVICE.setFormat(context, bs, format);

        // commit everything
        BITSTREAM_SERVICE.update(context, bs);
    }

    /**
     * This helper method wraps a String around a byte array returned from the
     * bitstream method further down
     */
    private static String getStringFromBitstream(Context context, Item item,
            String bitstream_name) throws SQLException, IOException,
            AuthorizeException
    {
        byte[] bytes = getBytesFromBitstream(context, item, bitstream_name);

        if (bytes == null)
        {
            return null;
        }

        return new String(bytes);
    }

    /**
     * This helper method retrieves the bytes of a bitstream for an item under
     * the CC bundle, with the given bitstream name
     */
    private static Bitstream getBitstream(Item item, String bitstream_name)
            throws SQLException, IOException, AuthorizeException
    {
        Bundle cc_bundle = null;

        // look for the CC bundle
        try
        {
            List<Bundle> bundles = ITEM_SERVICE.getBundles(item, CC_BUNDLE_NAME);

            if ((bundles != null) && (bundles.size() > 0))
            {
                cc_bundle = bundles.get(0);
            }
            else
            {
                return null;
            }
        }
        catch (Exception exc)
        {
            // this exception catching is a bit generic,
            // but basically it happens if there is no CC bundle
            return null;
        }

        return BUNDLE_SERVICE.getBitstreamByName(cc_bundle, bitstream_name);
    }

    private static byte[] getBytesFromBitstream(Context context, Item item, String bitstream_name)
            throws SQLException, IOException, AuthorizeException
    {
        Bitstream bs = getBitstream(item, bitstream_name);

        // no such bitstream
        if (bs == null)
        {
            return null;
        }

        // create a ByteArrayOutputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Utils.copy(BITSTREAM_SERVICE.retrieve(context, bs), baos);

        return baos.toByteArray();
    }

    /**
     * Fetch the contents of a URL
     */
    private static byte[] fetchURL(String url_string)
    {
        try
        {
            String line = "";
            URL url = new URL(url_string);
            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();

            while ((line = reader.readLine()) != null)
            {
                sb.append(line);
            }

            return sb.toString().getBytes();
        }
        catch (Exception exc)
        {
            log.error(exc.getMessage());
            return null;
        }
    }
    /**
     * Returns a metadata field handle for given field Id
     */
    public static MdField getCCField(String fieldId)
    {
    	return new MdField(ConfigurationManager.getProperty("cc.license." + fieldId));
    }

    // Shibboleth for Creative Commons license data - i.e. characters that reliably indicate CC in a URI
    private static final String ccShib = "creativecommons";

    /**
     * Helper class for using CC-related Metadata fields
     *
     */
    public static class MdField
    {
    	private String[] params = new String[4];

    	public MdField(String fieldName)
    	{
    		if (fieldName != null && fieldName.length() > 0)
    		{
    			String[] fParams = fieldName.split("\\.");
    			for (int i = 0; i < fParams.length; i++)
    			{
    				params[i] = fParams[i];
    			}
    			params[3] = Item.ANY;
    		}
    	}

    	/**
    	 * Returns first value that matches Creative Commons 'shibboleth',
    	 * or null if no matching values.
    	 * NB: this method will succeed only for metadata fields holding CC URIs
    	 *
    	 * @param item - the item to read
    	 * @return value - the first CC-matched value, or null if no such value
    	 */
    	public String ccItemValue(Item item)
    	{
            List<MetadataValue> dcvalues = ITEM_SERVICE.getMetadata(item, params[0], params[1], params[2], params[3]);
            for (MetadataValue dcvalue : dcvalues)
            {
                if ((dcvalue.getValue()).contains(ccShib))
                {
                	// return first value that matches the shib
                	return dcvalue.getValue();
                }
            }
            return null;
    	}

    	/**
    	 * Returns the value that matches the value mapped to the passed key if any.
    	 * NB: this only delivers a license name (if present in field) given a license URI
    	 *
    	 * @param item - the item to read
    	 * @param key - the key for desired value
    	 * @return value - the value associated with key or null if no such value
    	 */
    	public String keyedItemValue(Item item, String key)
    		throws AuthorizeException, IOException, SQLException
    	{
    		 CCLookup ccLookup = new CCLookup();
             ccLookup.issue(key);
             String matchValue = ccLookup.getLicenseName();
            List<MetadataValue> dcvalues = ITEM_SERVICE.getMetadata(item, params[0], params[1], params[2], params[3]);
             for (MetadataValue dcvalue : dcvalues)
             {
            	 if (dcvalue.getValue().equals(matchValue))
            	 {
            		 return dcvalue.getValue();
            	 }
             }
    		return null;
    	}

    	/**
    	 * Removes the passed value from the set of values for the field in passed item.
    	 *
    	 * @param item - the item to update
    	 * @param value - the value to remove
    	 */
    	public void removeItemValue(Context context, Item item, String value)
    			throws AuthorizeException, IOException, SQLException
    	{
    		if (value != null)
    		{
    			 List<MetadataValue> dcvalues  = ITEM_SERVICE.getMetadata(item, params[0], params[1], params[2], params[3]);
                 ArrayList<String> valuesToRemove = new ArrayList<String>();
                 for (MetadataValue dcvalue : dcvalues)
                 {
                     if (! dcvalue.getValue().equals(value))
                     {
                         valuesToRemove.add(dcvalue.getValue());
                     }
                  }
                ITEM_SERVICE.clearMetadata(context, item, params[0], params[1], params[2], params[3]);
                ITEM_SERVICE.addMetadata(context, item, params[0], params[1], params[2], params[3], valuesToRemove);
    		}
    	}

    	/**
    	 * Adds passed value to the set of values for the field in passed item.
    	 *
    	 * @param item - the item to update
    	 * @param value - the value to add in this field
    	 */
    	public void addItemValue(Context context, Item item, String value) throws SQLException {
            ITEM_SERVICE.addMetadata(context, item, params[0], params[1], params[2], params[3], value);
    	}
    }
}