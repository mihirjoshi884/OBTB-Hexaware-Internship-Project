package org.hexaware.transactionservice.services;

import org.hexaware.transactionservice.dtos.FundsSummaryDto;
import org.hexaware.transactionservice.dtos.LedgerEntryRequest;
import org.hexaware.transactionservice.entities.TransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface TransactionService {

    public Page<TransactionEntity> getOwnerHistory(UUID ownerId, int page, int size);

    public void recordTransaction(LedgerEntryRequest ledgerEntryRequest);
    public Page<TransactionEntity> deleteTransaction(UUID ownerId, UUID transactionId, int page, int size);
}
