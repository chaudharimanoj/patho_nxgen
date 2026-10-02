package com.pathonxgen.modules.patient.controller;

import com.pathonxgen.common.ApiResponse;
import com.pathonxgen.common.PageResponse;
import com.pathonxgen.modules.patient.dto.PatientRequest;
import com.pathonxgen.modules.patient.dto.PatientResponse;
import com.pathonxgen.modules.patient.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/patients")
@Tag(name = "Patient Management", description = "Patient registration, search, and medical records")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'LAB_ADMINISTRATOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PatientResponse> register(@Valid @RequestBody PatientRequest request) {
        return ApiResponse.ok(patientService.register(request), "Patient registered");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'LAB_ADMINISTRATOR', 'PATHOLOGIST', 'TECHNICIAN')")
    public ApiResponse<PatientResponse> getById(@PathVariable UUID id) {
        return ApiResponse.ok(patientService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'LAB_ADMINISTRATOR', 'PATHOLOGIST')")
    public ApiResponse<PageResponse<PatientResponse>> search(
            @RequestParam(defaultValue = "") String query,
            Pageable pageable) {
        return ApiResponse.ok(patientService.search(query, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'LAB_ADMINISTRATOR')")
    public ApiResponse<PatientResponse> update(@PathVariable UUID id, @Valid @RequestBody PatientRequest request) {
        return ApiResponse.ok(patientService.update(id, request), "Patient updated");
    }
}
