package com.pathonxgen.modules.auth.controller;

import com.pathonxgen.modules.auth.dto.AuthResponse;
import com.pathonxgen.modules.auth.dto.LoginRequest;
import com.pathonxgen.modules.auth.service.CustomAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Direct Auth", description = "Tenant-aware custom login endpoint for direct authentication")
public class DirectAuthController {

    private final CustomAuthService customAuthService;

    public DirectAuthController(CustomAuthService customAuthService) {
        this.customAuthService = customAuthService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user against resolved tenant Keycloak realm")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            @RequestHeader(value = "X-Tenant-Slug", required = false) String tenantSlugHeader,
            HttpServletRequest request
    ) {
        String hostHeader = request.getHeader("Host");
        AuthResponse response = customAuthService.login(loginRequest, hostHeader, tenantSlugHeader);
        return ResponseEntity.ok(response);
    }
}
