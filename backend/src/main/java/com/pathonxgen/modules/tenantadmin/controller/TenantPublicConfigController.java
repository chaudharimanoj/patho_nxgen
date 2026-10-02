package com.pathonxgen.modules.tenantadmin.controller;

import com.pathonxgen.core.tenant.TenantResolver;
import com.pathonxgen.modules.tenantadmin.dto.TenantPublicConfigDto;
import com.pathonxgen.modules.tenantadmin.entity.Tenant;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Controller providing public, unauthenticated tenant branding configuration.
 * Section 6: GET /api/v1/tenant/public-config
 */
@RestController
@RequestMapping("/api/v1/tenant")
public class TenantPublicConfigController {

    private final TenantResolver tenantResolver;

    public TenantPublicConfigController(TenantResolver tenantResolver) {
        this.tenantResolver = tenantResolver;
    }

    @GetMapping("/public-config")
    public ResponseEntity<TenantPublicConfigDto> getPublicConfig(HttpServletRequest request) {
        Optional<Tenant> resolvedTenant = Optional.empty();

        // 1. Resolve from X-Tenant-Domain header
        String domainHeader = request.getHeader("X-Tenant-Domain");
        if (domainHeader != null && !domainHeader.isBlank()) {
            resolvedTenant = tenantResolver.resolveByDomain(domainHeader);
        }

        // 2. Resolve from Host header / ServerName
        if (resolvedTenant.isEmpty() && request.getServerName() != null) {
            resolvedTenant = tenantResolver.resolveByDomain(request.getServerName());
        }

        // 3. Fallback to X-Tenant-Slug header (for dev/API testing)
        String slugHeader = request.getHeader("X-Tenant-Slug");
        if (slugHeader != null && !slugHeader.isBlank()) {
            if (slugHeader.equalsIgnoreCase("platform") ||
                slugHeader.equalsIgnoreCase("pathonxgen-platform") ||
                slugHeader.equalsIgnoreCase("master") ||
                slugHeader.equalsIgnoreCase("admin")) {
                return ResponseEntity.ok(TenantPublicConfigDto.of(
                        null, "PLATFORM", "Patho NXGen Platform Admin", "platform", "ACTIVE"
                ));
            }
            resolvedTenant = tenantResolver.resolveBySlug(slugHeader);
        }

        // 4. Default fallback to 'demo' tenant if on plain localhost
        if (resolvedTenant.isEmpty()) {
            resolvedTenant = tenantResolver.resolveBySlug("demo");
        }

        if (resolvedTenant.isPresent()) {
            Tenant t = resolvedTenant.get();
            return ResponseEntity.ok(TenantPublicConfigDto.of(
                    t.getId(),
                    t.getTenantCode(),
                    t.getLegalName(),
                    t.getSlug(),
                    t.getStatus() != null ? t.getStatus().name() : "ACTIVE"
            ));
        }

        return ResponseEntity.notFound().build();
    }
}
