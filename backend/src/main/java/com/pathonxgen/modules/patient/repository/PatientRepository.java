package com.pathonxgen.modules.patient.repository;

import com.pathonxgen.modules.patient.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {

    Optional<Patient> findByTenantIdAndId(UUID tenantId, UUID id);

    Optional<Patient> findByTenantIdAndMrn(UUID tenantId, String mrn);

    boolean existsByTenantIdAndMrn(UUID tenantId, String mrn);

    Page<Patient> findByTenantId(UUID tenantId, Pageable pageable);

    Page<Patient> findByTenantIdAndFullNameContainingIgnoreCaseOrTenantIdAndPhoneContaining(
            UUID tenantId1, String name, UUID tenantId2, String phone, Pageable pageable);
}

