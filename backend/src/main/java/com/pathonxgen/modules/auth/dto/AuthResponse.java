package com.pathonxgen.modules.auth.dto;

import java.util.List;

/**
 * Authenticated session response payload.
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        long refreshExpiresIn,
        String tokenType,
        String username,
        String tenantSlug,
        String tenantName,
        List<String> roles
) {}
