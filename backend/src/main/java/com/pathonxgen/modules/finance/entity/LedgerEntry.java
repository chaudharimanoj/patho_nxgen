package com.pathonxgen.modules.finance.entity;

import com.pathonxgen.common.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private EntryType type;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 255)
    private String description;

    public enum EntryType { INCOME, EXPENSE }

    protected LedgerEntry() {}

    public LedgerEntry(EntryType type, BigDecimal amount, String description) {
        this.type = type;
        this.amount = amount;
        this.description = description;
    }

    public EntryType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
}
