package com.pathonxgen.core.tenant;

import com.pathonxgen.modules.tenantadmin.entity.Tenant;
import com.pathonxgen.modules.tenantadmin.repository.TenantRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Resolves a request's tenant domain or slug to the registered {@link Tenant} entity.
 */
@Component
public class TenantResolver {

    private final TenantRepository tenantRepository;

    public TenantResolver(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public Optional<Tenant> resolveById(java.util.UUID tenantId) {
        if (tenantId == null) {
            return Optional.empty();
        }
        return tenantRepository.findById(tenantId);
    }

    public Optional<Tenant> resolveByDomain(String hostOrDomain) {
        if (hostOrDomain == null || hostOrDomain.isBlank()) {
            return Optional.empty();
        }
        String cleanDomain = hostOrDomain.split(":")[0].toLowerCase().trim();
        Optional<Tenant> byDomain = tenantRepository.findByDomain(cleanDomain);
        if (byDomain.isPresent()) {
            return byDomain;
        }
        // Extract subdomain (e.g. udc from udc.pathonxgen.com or udc.localhost)
        String[] parts = cleanDomain.split("\\.");
        if (parts.length > 1) {
            String subdomain = parts[0].trim();
            return tenantRepository.findBySlug(subdomain.toLowerCase())
                    .or(() -> tenantRepository.findByTenantCode(subdomain))
                    .or(() -> tenantRepository.findByTenantCode(subdomain.toUpperCase()));
        }
        return Optional.empty();
    }

    public Optional<Tenant> resolveBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return Optional.empty();
        }
        String clean = slug.trim();
        return tenantRepository.findBySlug(clean.toLowerCase())
                .or(() -> tenantRepository.findByTenantCode(clean))
                .or(() -> tenantRepository.findByTenantCode(clean.toUpperCase()));
    }
}

