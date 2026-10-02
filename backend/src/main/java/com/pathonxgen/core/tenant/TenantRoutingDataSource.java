package com.pathonxgen.core.tenant;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Supplies the current tenant schema as the routing lookup key. Combined
 * with Hibernate's SCHEMA multi-tenancy strategy (configured in
 * application.yml), each query is transparently scoped to
 * "tenant_<slug>" without any repository code knowing tenancy exists.
 */
public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContext.getTenantSchema();
    }
}
