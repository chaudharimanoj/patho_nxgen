package com.pathonxgen.modules.tenantadmin.repository;

import com.pathonxgen.modules.tenantadmin.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findBySlug(String slug);
    Optional<Tenant> findByDomain(String domain);
    Optional<Tenant> findByTenantCode(String tenantCode);
    boolean existsBySlug(String slug);
    boolean existsByDomain(String domain);
    boolean existsByTenantCode(String tenantCode);
}

