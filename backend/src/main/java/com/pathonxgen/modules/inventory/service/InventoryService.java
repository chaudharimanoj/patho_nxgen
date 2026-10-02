package com.pathonxgen.modules.inventory.service;

import com.pathonxgen.modules.inventory.entity.InventoryItem;
import com.pathonxgen.modules.inventory.repository.InventoryItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {

    private final InventoryItemRepository repository;

    public InventoryService(InventoryItemRepository repository) {
        this.repository = repository;
    }

    public List<InventoryItem> lowStockItems() {
        return repository.findAll().stream().filter(InventoryItem::isBelowReorderThreshold).toList();
    }
}
