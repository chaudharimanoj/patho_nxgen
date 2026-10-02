package com.pathonxgen.core.cache;

import com.pathonxgen.core.tenant.TenantContext;

import java.util.UUID;

/**
 * Utility helper enforcing Section 38 & Rule 7:
 * "Every tenant-specific cache key must include tenant_id (e.g. tenant:10001:patient:123)".
 */
public final class TenantCacheUtils {

    private TenantCacheUtils() {
    }

    public static String formatKey(String rawKey) {
        UUID tenantId = TenantContext.getTenantId();
        String prefix = tenantId != null ? tenantId.toString() : "global";
        return "tenant:" + prefix + ":" + rawKey;
    }
}
