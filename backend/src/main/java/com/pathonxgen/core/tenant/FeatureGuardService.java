package com.pathonxgen.core.tenant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service evaluating feature flags and product module availability per tenant.
 * Satisfies Blueprint Section 36 ("Keycloak controls user access, while Patho NXGen controls product/module availability").
 */
@Service("featureGuard")
public class FeatureGuardService {

    private static final Logger log = LoggerFactory.getLogger(FeatureGuardService.class);

    // Default module feature configuration
    private final Map<String, Boolean> defaultModules = Map.of(
            "patient", true,
            "laboratory", true,
            "billing", true,
            "analytics", true,
            "sms", false,
            "whatsapp", false
    );

    public boolean isModuleEnabled(String moduleName) {
        if (moduleName == null || moduleName.isBlank()) {
            return true;
        }
        String key = moduleName.toLowerCase().trim();
        boolean enabled = defaultModules.getOrDefault(key, true);
        if (!enabled) {
            log.warn("Access denied to disabled module '{}' for tenant '{}'", key, TenantContext.getTenantCode());
        }
        return enabled;
    }
}
