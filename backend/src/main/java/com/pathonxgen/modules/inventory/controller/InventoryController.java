package com.pathonxgen.modules.inventory.controller;

import com.pathonxgen.common.ApiResponse;
import com.pathonxgen.modules.inventory.entity.InventoryItem;
import com.pathonxgen.modules.inventory.service.InventoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('STORE_MANAGER', 'LAB_ADMINISTRATOR')")
    public ApiResponse<List<InventoryItem>> lowStock() {
        return ApiResponse.ok(inventoryService.lowStockItems());
    }
}
