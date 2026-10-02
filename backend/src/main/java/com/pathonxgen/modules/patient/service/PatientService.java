package com.pathonxgen.modules.patient.service;

import com.pathonxgen.common.PageResponse;
import com.pathonxgen.core.exception.ResourceNotFoundException;
import com.pathonxgen.core.tenant.TenantContext;
import com.pathonxgen.modules.patient.dto.PatientRequest;
import com.pathonxgen.modules.patient.dto.PatientResponse;
import com.pathonxgen.modules.patient.entity.Patient;
import com.pathonxgen.modules.patient.repository.PatientRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;
    private final com.pathonxgen.modules.subscription.service.SubscriptionService subscriptionService;

    public PatientService(PatientRepository patientRepository,
                          com.pathonxgen.modules.subscription.service.SubscriptionService subscriptionService) {
        this.patientRepository = patientRepository;
        this.subscriptionService = subscriptionService;
    }

    public PatientResponse register(PatientRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            subscriptionService.checkPatientVolumeLimit(tenantId);
        }

        String mrn = generateMrn();
        Patient patient = new Patient(
                mrn,
                request.fullName(),
                request.dateOfBirth(),
                Patient.Gender.valueOf(request.gender().toUpperCase())
        );
        patient.setPhone(request.phone());
        patient.setEmail(request.email());
        patient.setAddress(request.address());
        patient.setInsuranceProvider(request.insuranceProvider());
        patient.setCorporateAccountCode(request.corporateAccountCode());

        Patient saved = patientRepository.save(patient);

        if (tenantId != null) {
            subscriptionService.incrementPatientUsage(tenantId);
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PatientResponse getById(UUID id) {
        UUID tenantId = TenantContext.getTenantId();
        return patientRepository.findByTenantIdAndId(tenantId, id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + id));
    }

    @Transactional(readOnly = true)
    public PageResponse<PatientResponse> search(String query, Pageable pageable) {
        UUID tenantId = TenantContext.getTenantId();
        if (query == null || query.isBlank()) {
            return PageResponse.from(patientRepository.findByTenantId(tenantId, pageable).map(this::toResponse));
        }
        var page = patientRepository
                .findByTenantIdAndFullNameContainingIgnoreCaseOrTenantIdAndPhoneContaining(tenantId, query, tenantId, query, pageable)
                .map(this::toResponse);
        return PageResponse.from(page);
    }

    public PatientResponse update(UUID id, PatientRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        Patient patient = patientRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + id));
        patient.setFullName(request.fullName());
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setGender(Patient.Gender.valueOf(request.gender().toUpperCase()));
        patient.setPhone(request.phone());
        patient.setEmail(request.email());
        patient.setAddress(request.address());
        patient.setInsuranceProvider(request.insuranceProvider());
        patient.setCorporateAccountCode(request.corporateAccountCode());
        return toResponse(patient);
    }

    private static final AtomicLong SEQ = new AtomicLong(1);

    private String generateMrn() {
        // Placeholder only - replace with a DB sequence per tenant schema
        // (see Flyway migration) for guaranteed uniqueness under concurrency.
        return "MRN-" + Year.now() + "-" + String.format("%06d", SEQ.getAndIncrement());
    }

    private PatientResponse toResponse(Patient p) {
        return new PatientResponse(
                p.getId(), p.getMrn(), p.getFullName(), p.getDateOfBirth(),
                p.getGender().name(), p.getPhone(), p.getEmail(),
                p.getInsuranceProvider(), p.getCreatedAt()
        );
    }
}
