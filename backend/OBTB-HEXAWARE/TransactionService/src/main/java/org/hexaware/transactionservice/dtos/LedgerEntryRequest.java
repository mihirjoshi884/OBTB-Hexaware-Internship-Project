package org.hexaware.transactionservice.dtos;



import org.hexaware.transactionservice.enums.TransactionType;

import java.util.UUID;

public record LedgerEntryRequest(
        UUID ownerId,
        Double amount,
        TransactionType type,        // "CREDIT" or "DEBIT"
        Double postBalance, // The NEW balance calculated by User Service
        String referenceId, // Booking ID or "TOPUP"
        String description
) {
}
