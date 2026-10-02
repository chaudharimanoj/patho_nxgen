package com.pathonxgen.modules.crm.controller;

import com.pathonxgen.common.ApiResponse;
import com.pathonxgen.modules.crm.entity.Referral;
import com.pathonxgen.modules.crm.service.ReferralService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/referrals")
public class ReferralController {

    private final ReferralService referralService;

    public ReferralController(ReferralService referralService) {
        this.referralService = referralService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'LAB_ADMINISTRATOR')")
    public ApiResponse<Referral> record(@RequestParam UUID doctorId, @RequestParam UUID patientId, @RequestParam String source) {
        return ApiResponse.ok(referralService.recordReferral(doctorId, patientId, source));
    }
}
