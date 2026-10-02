package com.pathonxgen.modules.auth.controller;

import com.pathonxgen.common.ApiResponse;
import com.pathonxgen.modules.auth.dto.CurrentUserResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Identity is entirely delegated to Keycloak; this endpoint just reflects
 * back the validated JWT claims the frontend needs to render role-aware UI.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Direct Auth", description = "Tenant-aware custom login & authenticated user context endpoints")
public class CurrentUserController {

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        @SuppressWarnings("unchecked")
        List<String> roles = realmAccess != null ? (List<String>) realmAccess.getOrDefault("roles", List.of()) : List.of();

        return ApiResponse.ok(new CurrentUserResponse(
                jwt.getSubject(),
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("tenant"),
                roles
        ));
    }
}
