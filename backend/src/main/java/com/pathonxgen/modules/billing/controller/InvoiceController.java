package com.pathonxgen.modules.billing.controller;

import com.pathonxgen.common.ApiResponse;
import com.pathonxgen.modules.billing.entity.Invoice;
import com.pathonxgen.modules.billing.service.InvoiceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BILLING_EXECUTIVE', 'LAB_ADMINISTRATOR')")
    public ApiResponse<Invoice> create(@RequestParam UUID patientId,
                                        @RequestParam BigDecimal amount,
                                        @RequestParam Invoice.PaymentMode mode) {
        return ApiResponse.ok(invoiceService.createInvoice(patientId, amount, mode), "Invoice created");
    }
}
