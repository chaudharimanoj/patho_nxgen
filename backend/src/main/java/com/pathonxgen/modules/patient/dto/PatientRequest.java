package com.pathonxgen.modules.patient.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record PatientRequest(
        @NotBlank(message = "Full name is required") String fullName,
        @NotNull @Past(message = "Date of birth must be in the past") LocalDate dateOfBirth,
        @NotBlank String gender,
        String phone,
        @Email String email,
        String address,
        String insuranceProvider,
        String corporateAccountCode
) {
}
