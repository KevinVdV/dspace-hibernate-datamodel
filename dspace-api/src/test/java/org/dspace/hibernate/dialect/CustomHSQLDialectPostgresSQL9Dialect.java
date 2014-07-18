package org.dspace.hibernate.dialect;

import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.PostgreSQL9Dialect;
import org.hibernate.metamodel.spi.TypeContributions;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.PostgresUUIDType;
import org.hibernate.type.UUIDCharType;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 18/07/14
 * Time: 10:50
 */
public class CustomHSQLDialectPostgresSQL9Dialect extends PostgreSQL9Dialect {

    @Override
    public void contributeTypes(final TypeContributions typeContributions, final ServiceRegistry serviceRegistry) {
        super.contributeTypes(typeContributions, serviceRegistry);
        typeContributions.contributeType(new InternalPostgresUUIDType());
    }

    protected static class InternalPostgresUUIDType extends PostgresUUIDType {

        @Override
        protected boolean registerUnderJavaType() {
            return true;
        }
    }
}
