package com.pathonxgen.modules.laboratory.repository;

import com.pathonxgen.modules.laboratory.entity.Sample;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SampleRepository extends JpaRepository<Sample, UUID> {
    Optional<Sample> findByTenantIdAndId(UUID tenantId, UUID id);
    Optional<Sample> findByTenantIdAndBarcode(UUID tenantId, String barcode);
    List<Sample> findByTenantIdAndStatus(UUID tenantId, Sample.SampleStatus status);
    List<Sample> findByTenantIdAndPatientId(UUID tenantId, UUID patientId);
}

