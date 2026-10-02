package com.pathonxgen.modules.tenantadmin.dto;

import java.util.Map;
import java.util.UUID;

/**
 * Public configuration payload for tenant branding.
 * Exposes ONLY safe public branding properties (never secrets or admin config).
 */
public record TenantPublicConfigDto(
        UUID tenantId,
        String tenantCode,
        String tenantName,
        String slug,
        String logoUrl,
        String status,
        Map<String, String> theme
) {
    public static TenantPublicConfigDto of(UUID tenantId, String tenantCode, String tenantName, String slug, String status) {
        String logoUrl = "/assets/logos/" + (slug != null ? slug : "default") + "-logo.svg";
        Map<String, String> theme = Map.of(
                "primaryColor", "#4F46E5",
                "accentColor", "#0EA5E9"
        );
        return new TenantPublicConfigDto(tenantId, tenantCode, tenantName, slug, logoUrl, status, theme);
    }
}
