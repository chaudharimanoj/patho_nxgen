package com.pathonxgen.modules.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pathonxgen.core.exception.AccountDisabledException;
import com.pathonxgen.core.exception.AuthServiceUnavailableException;
import com.pathonxgen.core.exception.InvalidCredentialsException;
import com.pathonxgen.core.exception.TenantInactiveException;
import com.pathonxgen.core.tenant.TenantContext;
import com.pathonxgen.core.tenant.TenantResolver;
import com.pathonxgen.modules.auth.dto.AuthResponse;
import com.pathonxgen.modules.auth.dto.LoginRequest;
import com.pathonxgen.modules.tenantadmin.entity.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class CustomAuthService {

    private static final Logger log = LoggerFactory.getLogger(CustomAuthService.class);

    private final TenantResolver tenantResolver;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${app.keycloak.base-url:http://localhost:8081}")
    private String keycloakBaseUrl;

    @Value("${app.keycloak.client-id:patho-nxgen-frontend}")
    private String keycloakClientId;

    public CustomAuthService(TenantResolver tenantResolver, ObjectMapper objectMapper) {
        this.tenantResolver = tenantResolver;
        this.restClient = RestClient.create();
        this.objectMapper = objectMapper;
    }

    public AuthResponse login(LoginRequest loginRequest, String hostHeader, String tenantSlugHeader) {
        log.info("[CustomAuthService] Processing login request for user='{}', tenantSlugHeader='{}', hostHeader='{}'",
                loginRequest.username(), tenantSlugHeader, hostHeader);

        // 1. Authoritative tenant resolution via host / header or TenantContext
        Optional<Tenant> tenantOpt = Optional.empty();

        if (TenantContext.getTenantId() != null) {
            // Already resolved by TenantFilter
            tenantOpt = tenantResolver.resolveById(TenantContext.getTenantId());
        }

        if (tenantOpt.isEmpty() && tenantSlugHeader != null && !tenantSlugHeader.isBlank()) {
            tenantOpt = tenantResolver.resolveBySlug(tenantSlugHeader);
        }

        if (tenantOpt.isEmpty() && hostHeader != null && !hostHeader.isBlank()) {
            tenantOpt = tenantResolver.resolveByDomain(hostHeader);
        }

        boolean isPlatformSlug = tenantSlugHeader != null && (
                tenantSlugHeader.equalsIgnoreCase("platform") ||
                tenantSlugHeader.equalsIgnoreCase("pathonxgen-platform") ||
                tenantSlugHeader.equalsIgnoreCase("master") ||
                tenantSlugHeader.equalsIgnoreCase("admin")
        );

        String realm;
        String tenantSlug;
        String tenantName;

        if (isPlatformSlug) {
            realm = "pathonxgen-platform";
            tenantSlug = "platform";
            tenantName = "Patho NXGen Platform Admin";
        } else if (tenantOpt.isPresent()) {
            Tenant tenant = tenantOpt.get();
            validateTenantStatus(tenant);
            realm = tenant.getKeycloakRealm() != null ? tenant.getKeycloakRealm() : "pathonxgen-platform";
            tenantSlug = tenant.getSlug() != null ? tenant.getSlug() : tenant.getTenantCode();
            tenantName = tenant.getLegalName();
        } else if (tenantSlugHeader == null || tenantSlugHeader.isBlank()) {
            // Fallback to 'demo' tenant if no slug is provided
            Optional<Tenant> demoOpt = tenantResolver.resolveBySlug("demo");
            if (demoOpt.isPresent()) {
                Tenant tenant = demoOpt.get();
                validateTenantStatus(tenant);
                realm = tenant.getKeycloakRealm() != null ? tenant.getKeycloakRealm() : "pathonxgen-platform";
                tenantSlug = tenant.getSlug() != null ? tenant.getSlug() : tenant.getTenantCode();
                tenantName = tenant.getLegalName();
            } else {
                realm = "pathonxgen-platform";
                tenantSlug = "platform";
                tenantName = "Patho NXGen Platform Admin";
            }
        } else {
            throw new InvalidCredentialsException("Unable to resolve target tenant for login request");
        }

        String tokenEndpoint = keycloakBaseUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", keycloakClientId);
        formData.add("username", loginRequest.username());
        formData.add("password", loginRequest.password());

        log.info("Authenticating user '{}' against Keycloak realm '{}'", loginRequest.username(), realm);

        try {
            ResponseEntity<String> response = restClient.post()
                    .uri(tokenEndpoint)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .toEntity(String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String accessToken = jsonNode.path("access_token").asText();
                String refreshToken = jsonNode.path("refresh_token").asText();
                long expiresIn = jsonNode.path("expires_in").asLong(3600);
                long refreshExpiresIn = jsonNode.path("refresh_expires_in").asLong(1800);
                String tokenType = jsonNode.path("token_type").asText("Bearer");

                List<String> roles = extractRolesFromJwt(accessToken);
                String username = extractUsernameFromJwt(accessToken, loginRequest.username());

                return new AuthResponse(
                        accessToken,
                        refreshToken,
                        expiresIn,
                        refreshExpiresIn,
                        tokenType,
                        username,
                        tenantSlug,
                        tenantName,
                        roles
                );
            } else {
                throw new InvalidCredentialsException("Invalid username or password");
            }
        } catch (HttpClientErrorException ex) {
            log.warn("Keycloak authentication failed for user '{}' in realm '{}': {}", loginRequest.username(), realm, ex.getResponseBodyAsString());
            String responseBody = ex.getResponseBodyAsString();
            if (responseBody.contains("Account disabled")) {
                throw new AccountDisabledException("User account is disabled. Please contact system administrator.");
            }
            throw new InvalidCredentialsException("Invalid username or password");
        } catch (HttpServerErrorException ex) {
            log.error("Keycloak server error during authentication: {}", ex.getMessage());
            throw new AuthServiceUnavailableException("Identity authentication service is temporarily unavailable");
        } catch (Exception ex) {
            if (ex instanceof InvalidCredentialsException || ex instanceof AccountDisabledException || ex instanceof TenantInactiveException) {
                throw (RuntimeException) ex;
            }
            log.error("Unexpected error during authentication call: {}", ex.getMessage(), ex);
            throw new AuthServiceUnavailableException("Identity authentication service is temporarily unavailable", ex);
        }
    }

    private List<String> extractRolesFromJwt(String jwtToken) {
        List<String> roles = new ArrayList<>();
        try {
            String[] parts = jwtToken.split("\\.");
            if (parts.length > 1) {
                String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
                JsonNode root = objectMapper.readTree(payload);

                // Realm roles
                JsonNode realmAccess = root.path("realm_access").path("roles");
                if (realmAccess.isArray()) {
                    for (JsonNode roleNode : realmAccess) {
                        roles.add(roleNode.asText());
                    }
                }

                // Client roles
                JsonNode resourceAccess = root.path("resource_access").path(keycloakClientId).path("roles");
                if (resourceAccess.isArray()) {
                    for (JsonNode roleNode : resourceAccess) {
                        roles.add(roleNode.asText());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not parse roles from JWT token: {}", e.getMessage());
        }
        return roles;
    }

    private void validateTenantStatus(Tenant tenant) {
        if (tenant != null && (tenant.getStatus() == Tenant.TenantStatus.SUSPENDED || tenant.getStatus() == Tenant.TenantStatus.DEACTIVATED)) {
            log.warn("Login attempt blocked for inactive tenant '{}' [Status: {}]", tenant.getSlug(), tenant.getStatus());
            throw new TenantInactiveException("Tenant account is " + tenant.getStatus().name() + ". Please contact support.");
        }
    }

    private String extractUsernameFromJwt(String jwtToken, String fallbackUsername) {
        try {
            String[] parts = jwtToken.split("\\.");
            if (parts.length > 1) {
                String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
                JsonNode root = objectMapper.readTree(payload);
                if (root.has("preferred_username")) {
                    return root.get("preferred_username").asText();
                }
            }
        } catch (Exception e) {
            log.warn("Could not parse username from JWT token: {}", e.getMessage());
        }
        return fallbackUsername;
    }
}
