package com.pathonxgen.modules.user.dto;

import java.util.Set;

public record TenantRoleDto(
        String roleName,
        String displayName,
        String category,
        Set<String> permissions
) {}
