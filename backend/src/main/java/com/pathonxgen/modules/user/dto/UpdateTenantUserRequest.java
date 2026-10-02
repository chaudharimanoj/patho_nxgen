package com.pathonxgen.modules.user.dto;

public record UpdateTenantUserRequest(
        String displayName,
        String email,
        String employeeCode,
        String department,
        String designation
) {}
