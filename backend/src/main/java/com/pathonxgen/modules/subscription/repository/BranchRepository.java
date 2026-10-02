package com.pathonxgen.modules.subscription.repository;

import com.pathonxgen.modules.subscription.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BranchRepository extends JpaRepository<Branch, UUID> {
    List<Branch> findByTenantId(UUID tenantId);
    long countByTenantId(UUID tenantId);
}
