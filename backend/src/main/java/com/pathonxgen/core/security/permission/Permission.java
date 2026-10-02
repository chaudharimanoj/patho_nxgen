package com.pathonxgen.core.security.permission;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Granular application permissions registry for Patho NXGen.
 * Mapped from Keycloak realm roles to explicit business operation privileges.
 */
public enum Permission {
    PATIENTS_READ("patients:read", "View Patient Records"),
    PATIENTS_CREATE("patients:create", "Register New Patients"),
    PATIENTS_UPDATE("patients:update", "Update Patient Profile"),
    
    SAMPLES_READ("samples:read", "View Laboratory Samples"),
    SAMPLES_CREATE("samples:create", "Collect / Register Samples"),
    SAMPLES_UPDATE("samples:update", "Update Sample Status & Workflows"),
    
    REPORTS_READ("reports:read", "View Diagnostic Reports"),
    REPORTS_APPROVE("reports:approve", "Approve & Sign Diagnostic Reports"),
    
    USERS_MANAGE("users:manage", "Manage Tenant Users"),
    ROLES_MANAGE("roles:manage", "Manage Tenant Roles & Access Matrix"),
    TENANT_CONFIGURE("tenant:configure", "Configure Tenant Settings & Preferences");

    private final String key;
    private final String description;

    Permission(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }

    private static final Map<String, Set<Permission>> ROLE_PERMISSIONS = Map.of(
            "SUPER_ADMIN", Set.of(Permission.values()),
            "PLATFORM_ADMIN", Set.of(Permission.values()),
            "TENANT_ADMIN", Set.of(Permission.values()),
            "MANAGER", Set.of(
                    PATIENTS_READ, PATIENTS_CREATE, PATIENTS_UPDATE,
                    SAMPLES_READ, SAMPLES_CREATE, SAMPLES_UPDATE,
                    REPORTS_READ, REPORTS_APPROVE
            ),
            "PATHOLOGIST", Set.of(
                    PATIENTS_READ, SAMPLES_READ, REPORTS_READ, REPORTS_APPROVE
            ),
            "DOCTOR", Set.of(
                    PATIENTS_READ, SAMPLES_READ, REPORTS_READ
            ),
            "LAB_TECHNICIAN", Set.of(
                    PATIENTS_READ, SAMPLES_READ, SAMPLES_CREATE, SAMPLES_UPDATE, REPORTS_READ
            ),
            "RECEPTIONIST", Set.of(
                    PATIENTS_READ, PATIENTS_CREATE, PATIENTS_UPDATE
            )
    );

    public static Set<Permission> getPermissionsForRole(String role) {
        if (role == null) return Collections.emptySet();
        String normalizedRole = role.toUpperCase().replace("ROLE_", "");
        return ROLE_PERMISSIONS.getOrDefault(normalizedRole, Collections.emptySet());
    }

    public static Set<String> getPermissionKeysForRoles(Iterable<String> roles) {
        Set<String> keys = new HashSet<>();
        if (roles == null) return keys;
        for (String role : roles) {
            for (Permission p : getPermissionsForRole(role)) {
                keys.add(p.getKey());
            }
        }
        return keys;
    }
}
