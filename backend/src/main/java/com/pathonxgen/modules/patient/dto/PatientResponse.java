package com.pathonxgen.modules.patient.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PatientResponse(
        UUID id,
        String mrn,
        String fullName,
        LocalDate dateOfBirth,
        String gender,
        String phone,
        String email,
        String insuranceProvider,
        Instant createdAt
) {
}
