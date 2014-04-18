package org.dspace.content.dao.impl;

import org.dspace.content.BitstreamFormat;
import org.dspace.content.dao.BitstreamFormatDAO;
import org.dspace.core.Context;
import org.dspace.dao.AbstractHibernateDAO;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 17/03/14
 * Time: 12:55
 */
public class BitstreamFormatDAOImpl extends AbstractHibernateDAO<BitstreamFormat> implements BitstreamFormatDAO {

    /**
     * Find a bitstream format by its (unique) MIME type.
     * If more than one bitstream format has the same MIME type, the
     * one returned is unpredictable.
     *
     * @param context
     *            DSpace context object
     * @param mimeType
     *            MIME type value
     *
     * @return the corresponding bitstream format, or <code>null</code> if
     *         there's no bitstream format with the given MIMEtype.
     * @throws java.sql.SQLException
     */
    public BitstreamFormat findByMIMEType(Context context, String mimeType) throws SQLException
    {
        // NOTE: Avoid internal formats since e.g. "License" also has
        // a MIMEtype of text/plain.
        Criteria criteria = context.getDBConnection().createCriteria(BitstreamFormat.class);
        criteria.add(Restrictions.and(
                Restrictions.eq("internal", false),
                Restrictions.like("mimetype", mimeType)
        ));

        return (BitstreamFormat) criteria.uniqueResult();
    }

    /**
     * Find a bitstream format by its (unique) short description
     *
     * @param context
     *            DSpace context object
     * @param desc
     *            the short description
     *
     * @return the corresponding bitstream format, or <code>null</code> if
     *         there's no bitstream format with the given short description
     * @throws SQLException
     */
    public BitstreamFormat findByShortDescription(Context context,
            String desc) throws SQLException
    {
        Criteria criteria = context.getDBConnection().createCriteria(BitstreamFormat.class);
        criteria.add(Restrictions.and(
                Restrictions.eq("short_description", desc)
        ));

        return (BitstreamFormat) criteria.uniqueResult();
    }

    public int updateRemovedBitstreamFormat(Context context, BitstreamFormat deletedBitstreamFormat, BitstreamFormat newBitstreamFormat) throws SQLException {
        // Set bitstreams with this format to "unknown"
        Query query = context.getDBConnection().createQuery("update Bitstream set bitstreamFormat = :unknown_format where bitstreamFormat = :deleted_format");
        query.setParameter("unknown_format", newBitstreamFormat);
        query.setParameter("deleted_format", deletedBitstreamFormat);

        return query.executeUpdate();
    }

    public List<BitstreamFormat> findNonInternal(Context context) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(BitstreamFormat.class);
        criteria.add(Restrictions.and(
                Restrictions.eq("internal", false),
                Restrictions.not(Restrictions.like("short_description", "Unknown"))
        ));
        criteria.addOrder(Order.desc("support_level")).addOrder(Order.asc("short_description"));


        @SuppressWarnings("unchecked")
        List<BitstreamFormat> formats = criteria.list();
        return formats;

    }

    @Override
    public List<BitstreamFormat> findAll(Context context, Class clazz) throws SQLException {
        Criteria criteria = context.getDBConnection().createCriteria(BitstreamFormat.class);
        criteria.addOrder(Order.asc("bitstream_format_id"));
        @SuppressWarnings("unchecked")
        List<BitstreamFormat> formats = criteria.list();
        return formats;
    }

}
