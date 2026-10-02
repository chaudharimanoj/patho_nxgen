package com.pathonxgen.modules.user.controller;

import com.pathonxgen.common.ApiResponse;
import com.pathonxgen.core.security.permission.Permission;
import com.pathonxgen.modules.user.dto.TenantRoleDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller exposing available Tenant Roles and Granular Permission Registry.
 */
@RestController
@RequestMapping("/api/v1/tenant/roles")
public class TenantRoleController {

    private static final List<TenantRoleDto> STANDARD_TENANT_ROLES = List.of(
            new TenantRoleDto(
                    "TENANT_ADMIN",
                    "Tenant Administrator",
                    "Tenant Administration",
                    Permission.getPermissionsForRole("TENANT_ADMIN").stream().map(Permission::getKey).collect(Collectors.toSet())
            ),
            new TenantRoleDto(
                    "MANAGER",
                    "Operations Manager",
                    "Operational Management",
                    Permission.getPermissionsForRole("MANAGER").stream().map(Permission::getKey).collect(Collectors.toSet())
            ),
            new TenantRoleDto(
                    "PATHOLOGIST",
                    "Consultant Pathologist",
                    "Pathology Operations",
                    Permission.getPermissionsForRole("PATHOLOGIST").stream().map(Permission::getKey).collect(Collectors.toSet())
            ),
            new TenantRoleDto(
                    "DOCTOR",
                    "Referring / Clinical Doctor",
                    "Clinical Access",
                    Permission.getPermissionsForRole("DOCTOR").stream().map(Permission::getKey).collect(Collectors.toSet())
            ),
            new TenantRoleDto(
                    "LAB_TECHNICIAN",
                    "Laboratory Technician",
                    "Laboratory Operations",
                    Permission.getPermissionsForRole("LAB_TECHNICIAN").stream().map(Permission::getKey).collect(Collectors.toSet())
            ),
            new TenantRoleDto(
                    "RECEPTIONIST",
                    "Receptionist / Registration Clerk",
                    "Registration Operations",
                    Permission.getPermissionsForRole("RECEPTIONIST").stream().map(Permission::getKey).collect(Collectors.toSet())
            )
    );

    @GetMapping
    @PreAuthorize("hasAuthority('roles:manage') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<List<TenantRoleDto>> getTenantRoles() {
        return ApiResponse.ok(STANDARD_TENANT_ROLES, "Tenant roles retrieved successfully");
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('roles:manage') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<List<Map<String, String>>> getAllPermissions() {
        List<Map<String, String>> list = Arrays.stream(Permission.values())
                .map(p -> Map.of("key", p.getKey(), "description", p.getDescription()))
                .toList();
        return ApiResponse.ok(list, "Permission registry retrieved successfully");
    }
}
