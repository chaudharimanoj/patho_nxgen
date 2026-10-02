package com.pathonxgen.core.tenant;

import java.util.UUID;

/**
 * Holds the current request's resolved tenant details (tenantId, tenantCode, keycloakRealm, domain)
 * in ThreadLocal variables. Populated by {@link TenantFilter} after domain and JWT validation,
 * cleared at the end of every request.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_TENANT_CODE = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_KEYCLOAK_REALM = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_DOMAIN = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenantId(UUID tenantId) {
        CURRENT_TENANT_ID.set(tenantId);
    }

    public static UUID getTenantId() {
        return CURRENT_TENANT_ID.get();
    }

    public static void setTenantCode(String tenantCode) {
        CURRENT_TENANT_CODE.set(tenantCode);
    }

    public static String getTenantCode() {
        return CURRENT_TENANT_CODE.get();
    }

    public static void setKeycloakRealm(String realm) {
        CURRENT_KEYCLOAK_REALM.set(realm);
    }

    public static String getKeycloakRealm() {
        return CURRENT_KEYCLOAK_REALM.get();
    }

    public static void setDomain(String domain) {
        CURRENT_DOMAIN.set(domain);
    }

    public static String getDomain() {
        return CURRENT_DOMAIN.get();
    }

    public static void setTenantSchema(String schema) {
        setTenantCode(schema);
    }

    public static String getTenantSchema() {
        return getTenantCode();
    }

    public static void clear() {
        CURRENT_TENANT_ID.remove();
        CURRENT_TENANT_CODE.remove();
        CURRENT_KEYCLOAK_REALM.remove();
        CURRENT_DOMAIN.remove();
    }
}

