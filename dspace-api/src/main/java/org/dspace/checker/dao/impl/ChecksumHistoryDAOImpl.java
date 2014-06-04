package org.dspace.checker.dao.impl;

import org.dspace.checker.ChecksumHistory;
import org.dspace.checker.ChecksumResultCode;
import org.dspace.checker.dao.ChecksumHistoryDAO;
import org.dspace.content.Bitstream;
import org.dspace.core.Context;
import org.dspace.core.AbstractHibernateDAO;
import org.hibernate.Query;

import java.sql.SQLException;
import java.util.Date;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 25/04/14
 * Time: 15:57
 */
public class ChecksumHistoryDAOImpl extends AbstractHibernateDAO<ChecksumHistory> implements ChecksumHistoryDAO {

    @Override
    public int deleteByDateAndCode(Context context, Date retentionDate, ChecksumResultCode resultCode) throws SQLException {
        String hql = "delete from ChecksumHistory where processEndDate < :processEndDate AND checksumResult.resultCode=:resultCode";
        Query query = createQuery(context, hql);
        query.setParameter("processEndDate", retentionDate);
        query.setParameter("result", resultCode);
        return query.executeUpdate();
    }

    @Override
    public void deleteByBitstream(Context context, Bitstream bitstream) throws SQLException {
        String hql = "delete from ChecksumHistory where bitstream=:bitstream";
        Query query = createQuery(context, hql);
        query.setParameter("bitstream", bitstream);
        query.executeUpdate();
    }
}
