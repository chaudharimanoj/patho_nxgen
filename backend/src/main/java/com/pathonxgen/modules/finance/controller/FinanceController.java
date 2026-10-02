package com.pathonxgen.modules.finance.controller;

import com.pathonxgen.common.ApiResponse;
import com.pathonxgen.modules.finance.service.FinanceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/finance")
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping("/net-profit")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'LAB_ADMINISTRATOR')")
    public ApiResponse<BigDecimal> netProfit() {
        return ApiResponse.ok(financeService.netProfit());
    }
}
