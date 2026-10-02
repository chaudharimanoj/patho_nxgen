package com.pathonxgen.core.tenant;

import com.pathonxgen.modules.tenantadmin.entity.Tenant;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Resolves the tenant for every request and stores tenant context in {@link TenantContext}.
 * Resolution order:
 * 1. Host domain or X-Tenant-Domain header
 * 2. JWT "tenant" claim
 * 3. Fallback X-Tenant-Slug header (for dev/API testing)
 */
@Component
@Order(20)
public class TenantFilter extends OncePerRequestFilter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TenantFilter.class);
    private final TenantResolver tenantResolver;

    public TenantFilter(TenantResolver tenantResolver) {
        this.tenantResolver = tenantResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Optional<Tenant> resolvedTenant = Optional.empty();

            // 1. Resolve from Host domain or X-Tenant-Domain header
            String domainHeader = request.getHeader("X-Tenant-Domain");
            if (domainHeader != null && !domainHeader.isBlank()) {
                resolvedTenant = tenantResolver.resolveByDomain(domainHeader);
            } else if (request.getServerName() != null) {
                resolvedTenant = tenantResolver.resolveByDomain(request.getServerName());
            }

            // 2. Resolve from authenticated JWT tenant claim if present
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
                String tenantSlug = jwt.getClaimAsString("tenant");
                if (tenantSlug != null && resolvedTenant.isEmpty()) {
                    resolvedTenant = tenantResolver.resolveBySlug(tenantSlug);
                }
            }

            // 3. Fallback header for testing
            if (resolvedTenant.isEmpty()) {
                String slugHeader = request.getHeader("X-Tenant-Slug");
                if (slugHeader != null && !slugHeader.isBlank()) {
                    resolvedTenant = tenantResolver.resolveBySlug(slugHeader);
                }
            }

            // Populate TenantContext and check tenant lifecycle status
            if (resolvedTenant.isPresent()) {
                Tenant tenant = resolvedTenant.get();
                log.info("[TenantFilter] Request '{} {}' resolved tenant '{}' (code: '{}', status: '{}')",
                        request.getMethod(), request.getRequestURI(), tenant.getSlug(), tenant.getTenantCode(), tenant.getStatus());

                boolean isPublicOrAuthEndpoint = request.getRequestURI().startsWith("/api/v1/public")
                        || request.getRequestURI().equals("/api/v1/tenant/public-config")
                        || request.getRequestURI().startsWith("/api/v1/auth")
                        || request.getRequestURI().startsWith("/api/v1/platform/tenants");

                if (!isPublicOrAuthEndpoint && (tenant.getStatus() == Tenant.TenantStatus.SUSPENDED || tenant.getStatus() == Tenant.TenantStatus.DEACTIVATED)) {
                    log.warn("[TenantFilter] BLOCKED request '{} {}' for INACTIVE tenant '{}' [Status: {}]",
                            request.getMethod(), request.getRequestURI(), tenant.getSlug(), tenant.getStatus());
                    response.setStatus(423); // 423 Locked
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Tenant account is " + tenant.getStatus().name() + ". Please contact platform support.\"}");
                    return;
                }

                TenantContext.setTenantId(tenant.getId());
                TenantContext.setTenantCode(tenant.getTenantCode());
                TenantContext.setKeycloakRealm(tenant.getKeycloakRealm());
                TenantContext.setDomain(tenant.getDomain());
            } else {
                log.debug("[TenantFilter] Request '{} {}' proceeded with NO resolved tenant context", request.getMethod(), request.getRequestURI());
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}


