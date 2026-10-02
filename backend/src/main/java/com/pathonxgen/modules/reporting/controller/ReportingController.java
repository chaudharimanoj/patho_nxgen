package com.pathonxgen.modules.reporting.controller;

import com.pathonxgen.common.ApiResponse;
import com.pathonxgen.modules.reporting.dto.OperationalKpiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportingController {

    @GetMapping("/kpis")
    @PreAuthorize("hasAnyRole('LAB_ADMINISTRATOR', 'ACCOUNTANT')")
    public ApiResponse<OperationalKpiResponse> kpis() {
        // TODO: aggregate from patient/laboratory/billing modules via
        // dedicated read-side queries once reporting module owns its
        // own projection tables (avoid ad-hoc joins across module boundaries).
        return ApiResponse.ok(new OperationalKpiResponse(0, 0, 0, 0));
    }
}
