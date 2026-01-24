package org.hexaware.transactionservice.services.impl;

import jakarta.transaction.Transactional;
import org.hexaware.transactionservice.dtos.FundsSummaryDto;
import org.hexaware.transactionservice.dtos.LedgerEntryRequest;
import org.hexaware.transactionservice.dtos.ResponseDto;
import org.hexaware.transactionservice.entities.TransactionEntity;
import org.hexaware.transactionservice.enums.TransactionType;
import org.hexaware.transactionservice.exceptions.UnableToFetchFundsException;
import org.hexaware.transactionservice.repository.TransactionRepository;
import org.hexaware.transactionservice.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private WebClient transactionWebClient;
    @Value("${userservice.base-uri}")
    private String userServiceBaseUrl;

    @Override
    public Page<TransactionEntity> getOwnerHistory(UUID ownerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return transactionRepository.findByOwnerId(ownerId, pageable);
    }

    @Override
    @Transactional
    public void recordTransaction(LedgerEntryRequest ledgerEntryRequest) {
        // 1. Extract values
        UUID userid = ledgerEntryRequest.ownerId();
        Double postBalance = ledgerEntryRequest.postBalance();
        String reference = ledgerEntryRequest.referenceId();
        Double amount = ledgerEntryRequest.amount();
        TransactionType type = ledgerEntryRequest.type();
        String description = ledgerEntryRequest.description();

        // 2. Build the Entity
        TransactionEntity record = TransactionEntity.builder()
                .ownerId(userid)
                .amount(amount)
                .description(description)
                .referenceId(reference)
                .postTransactionBalance(postBalance)
                .type(type)
                .build();

        // 3. Save to the database (Ledger is now updated)
        transactionRepository.save(record);
    }

    @Override
    public Page<TransactionEntity> deleteTransaction(UUID ownerId, UUID transactionId, int page, int size) {
        return null;
    }


}
