package com.pathonxgen.core.security;

import com.pathonxgen.core.tenant.TenantContext;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Validates that the Keycloak token issuer matches the resolved request tenant realm.
 * Enforces Rule 4: "Validate that the Keycloak identity belongs to the current tenant."
 */
@Component
public class TenantJwtIssuerValidator implements OAuth2TokenValidator<Jwt> {

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        String expectedRealm = TenantContext.getKeycloakRealm();
        if (expectedRealm == null || expectedRealm.isBlank()) {
            // Public or non-tenant endpoint
            return OAuth2TokenValidatorResult.success();
        }

        String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : "";
        if (!issuer.contains("/realms/" + expectedRealm) && !issuer.endsWith("/" + expectedRealm)) {
            OAuth2Error error = new OAuth2Error(
                    "invalid_token",
                    "Security Violation: Keycloak identity realm (" + issuer + ") does not match tenant realm (" + expectedRealm + ")",
                    null
            );
            return OAuth2TokenValidatorResult.failure(error);
        }

        return OAuth2TokenValidatorResult.success();
    }
}
