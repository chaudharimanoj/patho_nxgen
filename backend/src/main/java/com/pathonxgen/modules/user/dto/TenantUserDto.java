package com.pathonxgen.modules.user.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TenantUserDto(
        UUID id,
        String keycloakUserId,
        String username,
        String displayName,
        String email,
        String employeeCode,
        String department,
        String designation,
        String status,
        List<String> roles,
        Instant createdAt,
        Instant updatedAt
) {}
