package com.pathonxgen.modules.user.service;

import com.pathonxgen.core.security.KeycloakAdminClientService;
import com.pathonxgen.core.tenant.TenantContext;
import com.pathonxgen.modules.user.dto.*;
import com.pathonxgen.modules.user.entity.UserProfile;
import com.pathonxgen.modules.user.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TenantUserService {

    private static final Logger log = LoggerFactory.getLogger(TenantUserService.class);

    private static final Set<String> PLATFORM_ROLES = Set.of(
            "SUPER_ADMIN", "PLATFORM_ADMIN", "SUPPORT_ADMIN", "BILLING_ADMIN"
    );

    private final UserProfileRepository userProfileRepository;
    private final KeycloakAdminClientService keycloakAdminClientService;

    // In-memory tenant user roles map for fast multi-tenant lookup
    private final Map<UUID, List<String>> userRolesMap = new java.util.concurrent.ConcurrentHashMap<>();

    public TenantUserService(UserProfileRepository userProfileRepository,
                             KeycloakAdminClientService keycloakAdminClientService) {
        this.userProfileRepository = userProfileRepository;
        this.keycloakAdminClientService = keycloakAdminClientService;
    }

    @Transactional(readOnly = true)
    public List<TenantUserDto> searchUsers(String query, String status, String department) {
        List<UserProfile> profiles = userProfileRepository.searchUsers(
                (query != null && !query.isBlank()) ? query.trim() : null,
                (status != null && !status.isBlank()) ? status.trim().toUpperCase() : null,
                (department != null && !department.isBlank()) ? department.trim() : null
        );

        return profiles.stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public TenantUserDto getUserById(UUID id) {
        UserProfile profile = userProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant user not found with ID: " + id));
        return toDto(profile);
    }

    @Transactional
    public TenantUserDto createUser(CreateTenantUserRequest request) {
        validateRoles(request.roles());

        if (userProfileRepository.existsByEmail(request.email().trim().toLowerCase())) {
            throw new IllegalArgumentException("User with email '" + request.email() + "' already exists");
        }

        if (request.employeeCode() != null && !request.employeeCode().isBlank() &&
            userProfileRepository.existsByEmployeeCode(request.employeeCode().trim())) {
            throw new IllegalArgumentException("Employee code '" + request.employeeCode() + "' is already in use");
        }

        String realmName = TenantContext.getKeycloakRealm() != null ? TenantContext.getKeycloakRealm() : "pathonxgen-tenant-demo";

        // 1. Provision user in Keycloak Realm
        String keycloakUserId = keycloakAdminClientService.createUser(
                realmName,
                request.username().trim(),
                request.email().trim().toLowerCase(),
                request.displayName().trim(),
                true
        );

        // 2. Assign initial roles
        keycloakAdminClientService.assignRolesToUser(realmName, keycloakUserId, request.roles());

        // 3. Save business profile in PostgreSQL
        UserProfile profile = new UserProfile(
                keycloakUserId,
                request.displayName().trim(),
                request.email().trim().toLowerCase(),
                request.department() != null ? request.department().trim() : null
        );
        profile.setEmployeeCode(request.employeeCode() != null ? request.employeeCode().trim() : null);
        profile.setDesignation(request.designation() != null ? request.designation().trim() : null);
        profile.setStatus("ACTIVE");

        UserProfile saved = userProfileRepository.save(profile);
        userRolesMap.put(saved.getId(), request.roles());

        log.info("[TenantUserService] Successfully created tenant user '{}' (ID: {})", saved.getDisplayName(), saved.getId());
        return toDto(saved);
    }

    @Transactional
    public TenantUserDto updateUser(UUID id, UpdateTenantUserRequest request) {
        UserProfile profile = userProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant user not found with ID: " + id));

        if (request.displayName() != null && !request.displayName().isBlank()) {
            profile.setDisplayName(request.displayName().trim());
        }

        if (request.email() != null && !request.email().isBlank()) {
            String newEmail = request.email().trim().toLowerCase();
            if (!newEmail.equalsIgnoreCase(profile.getEmail()) && userProfileRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Email '" + newEmail + "' is already in use");
            }
            profile.setEmail(newEmail);
        }

        if (request.employeeCode() != null) {
            String newCode = request.employeeCode().trim();
            if (!newCode.equalsIgnoreCase(profile.getEmployeeCode()) && !newCode.isBlank() && userProfileRepository.existsByEmployeeCode(newCode)) {
                throw new IllegalArgumentException("Employee code '" + newCode + "' is already in use");
            }
            profile.setEmployeeCode(newCode.isBlank() ? null : newCode);
        }

        if (request.department() != null) {
            profile.setDepartment(request.department().trim().isBlank() ? null : request.department().trim());
        }

        if (request.designation() != null) {
            profile.setDesignation(request.designation().trim().isBlank() ? null : request.designation().trim());
        }

        profile.setUpdatedAt(java.time.Instant.now());
        UserProfile updated = userProfileRepository.save(profile);

        String realmName = TenantContext.getKeycloakRealm() != null ? TenantContext.getKeycloakRealm() : "pathonxgen-tenant-demo";
        keycloakAdminClientService.updateUser(realmName, profile.getKeycloakUserId(), profile.getEmail(), profile.getDisplayName());

        return toDto(updated);
    }

    @Transactional
    public TenantUserDto updateUserStatus(UUID id, String newStatus) {
        UserProfile profile = userProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant user not found with ID: " + id));

        String statusNormalized = newStatus.trim().toUpperCase();
        if (!Set.of("ACTIVE", "DISABLED", "SUSPENDED").contains(statusNormalized)) {
            throw new IllegalArgumentException("Invalid status: " + newStatus);
        }

        profile.setStatus(statusNormalized);
        profile.setUpdatedAt(java.time.Instant.now());
        UserProfile updated = userProfileRepository.save(profile);

        String realmName = TenantContext.getKeycloakRealm() != null ? TenantContext.getKeycloakRealm() : "pathonxgen-tenant-demo";
        boolean enabled = "ACTIVE".equalsIgnoreCase(statusNormalized);
        keycloakAdminClientService.setUserEnabled(realmName, profile.getKeycloakUserId(), enabled);

        log.warn("[TenantUserService] Tenant user '{}' (ID: {}) status updated to {}", profile.getDisplayName(), id, statusNormalized);
        return toDto(updated);
    }

    @Transactional
    public void resetPassword(UUID id, String newPassword) {
        UserProfile profile = userProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant user not found with ID: " + id));

        String realmName = TenantContext.getKeycloakRealm() != null ? TenantContext.getKeycloakRealm() : "pathonxgen-tenant-demo";
        keycloakAdminClientService.resetUserPassword(realmName, profile.getKeycloakUserId(), newPassword);
        log.info("[TenantUserService] Reset password for tenant user '{}'", profile.getDisplayName());
    }

    @Transactional
    public TenantUserDto assignUserRoles(UUID id, List<String> roles) {
        UserProfile profile = userProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant user not found with ID: " + id));

        validateRoles(roles);

        List<String> normalizedRoles = roles.stream().map(r -> r.trim().toUpperCase()).toList();
        userRolesMap.put(profile.getId(), normalizedRoles);

        String realmName = TenantContext.getKeycloakRealm() != null ? TenantContext.getKeycloakRealm() : "pathonxgen-tenant-demo";
        keycloakAdminClientService.assignRolesToUser(realmName, profile.getKeycloakUserId(), normalizedRoles);

        log.info("[TenantUserService] Assigned roles {} to user '{}'", normalizedRoles, profile.getDisplayName());
        return toDto(profile);
    }

    private void validateRoles(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("At least one role must be specified");
        }
        for (String r : roles) {
            String roleUpper = r.trim().toUpperCase();
            if (PLATFORM_ROLES.contains(roleUpper)) {
                throw new IllegalArgumentException("Platform role '" + roleUpper + "' cannot be assigned to normal tenant users");
            }
        }
    }

    private TenantUserDto toDto(UserProfile profile) {
        List<String> roles = userRolesMap.getOrDefault(profile.getId(), List.of("LAB_TECHNICIAN"));
        return new TenantUserDto(
                profile.getId(),
                profile.getKeycloakUserId(),
                profile.getEmail() != null ? profile.getEmail().split("@")[0] : "user",
                profile.getDisplayName(),
                profile.getEmail(),
                profile.getEmployeeCode(),
                profile.getDepartment(),
                profile.getDesignation(),
                profile.getStatus(),
                roles,
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
