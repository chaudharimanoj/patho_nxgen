package com.pathonxgen.modules.inventory.repository;

import com.pathonxgen.modules.inventory.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID> {
    List<InventoryItem> findByQuantityOnHandLessThanEqual(Integer threshold);
}
