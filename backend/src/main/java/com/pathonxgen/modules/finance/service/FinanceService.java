package com.pathonxgen.modules.finance.service;

import com.pathonxgen.modules.finance.entity.LedgerEntry;
import com.pathonxgen.modules.finance.repository.LedgerEntryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class FinanceService {

    private final LedgerEntryRepository ledgerEntryRepository;

    public FinanceService(LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    public BigDecimal netProfit() {
        return ledgerEntryRepository.findAll().stream()
                .map(e -> e.getType() == LedgerEntry.EntryType.INCOME ? e.getAmount() : e.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
