package com.pathonxgen.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Direct login request payload.
 * Section 7: Contains username & password ONLY.
 * Tenant ID is derived authoritatively by the backend from the HTTP request Host/Headers.
 */
public record LoginRequest(
        @NotBlank(message = "Username or email is required")
        String username,

        @NotBlank(message = "Password is required")
        String password
) {}
