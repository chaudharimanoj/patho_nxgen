package com.pathonxgen.modules.tenantadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TenantProvisionRequest(
        @NotBlank @Pattern(regexp = "^[a-z0-9-]{3,32}$", message = "Slug must be lowercase alphanumeric with hyphens") String slug,
        String tenantCode,
        String domain,
        @NotBlank String legalName,
        String adminEmail,
        String subscriptionPlan,
        String volumeTierCode
) {
}

