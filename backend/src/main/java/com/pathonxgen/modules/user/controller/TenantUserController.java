package com.pathonxgen.modules.user.controller;

import com.pathonxgen.common.ApiResponse;
import com.pathonxgen.modules.user.dto.*;
import com.pathonxgen.modules.user.service.TenantUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller exposing Tenant User Access & Role Privilege Operations.
 */
@RestController
@RequestMapping("/api/v1/tenant/users")
@Tag(name = "User Management", description = "Tenant user account and role assignment operations")
public class TenantUserController {

    private final TenantUserService tenantUserService;

    public TenantUserController(TenantUserService tenantUserService) {
        this.tenantUserService = tenantUserService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('users:manage') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<List<TenantUserDto>> searchUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String department
    ) {
        List<TenantUserDto> users = tenantUserService.searchUsers(query, status, department);
        return ApiResponse.ok(users, "Tenant users retrieved successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('users:manage') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<TenantUserDto> getUserById(@PathVariable UUID id) {
        return ApiResponse.ok(tenantUserService.getUserById(id), "User retrieved successfully");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('users:manage') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<TenantUserDto> createUser(@Valid @RequestBody CreateTenantUserRequest request) {
        TenantUserDto created = tenantUserService.createUser(request);
        return ApiResponse.ok(created, "Tenant user created successfully");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('users:manage') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<TenantUserDto> updateUser(@PathVariable UUID id, @RequestBody UpdateTenantUserRequest request) {
        TenantUserDto updated = tenantUserService.updateUser(id, request);
        return ApiResponse.ok(updated, "Tenant user profile updated successfully");
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('users:manage') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<TenantUserDto> updateUserStatus(@PathVariable UUID id, @Valid @RequestBody UpdateUserStatusRequest request) {
        TenantUserDto updated = tenantUserService.updateUserStatus(id, request.status());
        return ApiResponse.ok(updated, "User status updated to " + updated.status());
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('users:manage') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<String> resetPassword(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("Password string is required");
        }
        tenantUserService.resetPassword(id, newPassword);
        return ApiResponse.ok("Password reset successfully", "Password reset successfully");
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('roles:manage') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<TenantUserDto> assignUserRoles(@PathVariable UUID id, @Valid @RequestBody AssignUserRolesRequest request) {
        TenantUserDto updated = tenantUserService.assignUserRoles(id, request.roles());
        return ApiResponse.ok(updated, "User roles updated successfully");
    }
}
