package com.pathonxgen.core.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pathonxgen.core.tenant.TenantFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stateless resource-server security: every request carrying a Keycloak
 * issued JWT is validated against the corresponding realm's JWK set.
 * Authorization is RBAC based on the "roles" claim, mapped to
 * Spring authorities by {@link KeycloakJwtAuthenticationConverter}.
 */
@Configuration
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final KeycloakJwtAuthenticationConverter jwtAuthConverter;
    private final TenantFilter tenantFilter;
    private final Map<String, JwtDecoder> jwtDecoders = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.keycloak.base-url:http://localhost:8081}")
    private String keycloakBaseUrl;

    public SecurityConfig(KeycloakJwtAuthenticationConverter jwtAuthConverter, TenantFilter tenantFilter) {
        this.jwtAuthConverter = jwtAuthConverter;
        this.tenantFilter = tenantFilter;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return token -> {
            try {
                String[] parts = token.split("\\.");
                if (parts.length > 1) {
                    String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
                    JsonNode root = objectMapper.readTree(payload);
                    String issuer = root.path("iss").asText();
                    if (issuer != null && !issuer.isBlank() && issuer.startsWith(keycloakBaseUrl)) {
                        JwtDecoder decoder = jwtDecoders.computeIfAbsent(issuer, JwtDecoders::fromIssuerLocation);
                        return decoder.decode(token);
                    }
                }
            } catch (Exception e) {
                log.warn("[SecurityConfig] Multi-tenant JwtDecoder failed to decode token: {}", e.getMessage());
            }
            String fallbackIssuer = keycloakBaseUrl + "/realms/pathonxgen-platform";
            return jwtDecoders.computeIfAbsent(fallbackIssuer, JwtDecoders::fromIssuerLocation).decode(token);
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/swagger-resources/**", "/webjars/**").permitAll()
                .requestMatchers("/api/v1/public/**", "/api/v1/tenant/public-config", "/api/v1/auth/**", "/api/v1/platform/tenants", "/api/v1/platform/tenants/**", "/api/v1/platform/subscriptions/**", "/api/v1/tenant/subscription").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer((OAuth2ResourceServerConfigurer<HttpSecurity> oauth2) -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder()).jwtAuthenticationConverter(jwtAuthConverter))
            )
            .addFilterAfter(tenantFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("https://*.pathonxgen.com", "http://localhost:*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "X-Tenant-Slug", "X-Tenant-Domain", "Link", "X-Total-Count"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
