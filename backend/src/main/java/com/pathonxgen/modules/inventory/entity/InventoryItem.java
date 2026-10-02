package com.pathonxgen.modules.inventory.entity;

import com.pathonxgen.common.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "inventory_items")
public class InventoryItem extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemType type;

    @Column(nullable = false)
    private Integer quantityOnHand;

    @Column(nullable = false)
    private Integer reorderThreshold;

    private LocalDate expiryDate;

    @Column(length = 120)
    private String vendor;

    public enum ItemType { REAGENT, CONSUMABLE }

    protected InventoryItem() {}

    public InventoryItem(String name, ItemType type, Integer quantityOnHand, Integer reorderThreshold) {
        this.name = name;
        this.type = type;
        this.quantityOnHand = quantityOnHand;
        this.reorderThreshold = reorderThreshold;
    }

    public String getName() { return name; }
    public ItemType getType() { return type; }
    public Integer getQuantityOnHand() { return quantityOnHand; }
    public void setQuantityOnHand(Integer quantityOnHand) { this.quantityOnHand = quantityOnHand; }
    public Integer getReorderThreshold() { return reorderThreshold; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }
    public boolean isBelowReorderThreshold() { return quantityOnHand <= reorderThreshold; }
}
