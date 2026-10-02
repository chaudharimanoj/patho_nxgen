package com.pathonxgen.modules.laboratory.controller;

import com.pathonxgen.common.ApiResponse;
import com.pathonxgen.modules.laboratory.dto.SampleTransitionRequest;
import com.pathonxgen.modules.laboratory.entity.Sample;
import com.pathonxgen.modules.laboratory.entity.Sample.SampleStatus;
import com.pathonxgen.modules.laboratory.service.SampleLifecycleService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/samples")
@Tag(name = "Laboratory", description = "Sample tracking and lifecycle transitions")
public class SampleController {

    private final SampleLifecycleService sampleLifecycleService;

    public SampleController(SampleLifecycleService sampleLifecycleService) {
        this.sampleLifecycleService = sampleLifecycleService;
    }

    @PostMapping("/{id}/transition/{status}")
    @PreAuthorize("hasAnyRole('PHLEBOTOMIST', 'TECHNICIAN', 'PATHOLOGIST', 'LAB_ADMINISTRATOR')")
    public ApiResponse<Sample> transition(@PathVariable UUID id,
                                           @PathVariable SampleStatus status,
                                           @Valid @RequestBody SampleTransitionRequest request) {
        return ApiResponse.ok(sampleLifecycleService.transition(id, status, request.actorRole()));
    }
}
