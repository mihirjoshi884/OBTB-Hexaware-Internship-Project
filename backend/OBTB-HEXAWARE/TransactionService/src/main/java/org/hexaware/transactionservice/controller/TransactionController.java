package org.hexaware.transactionservice.controller;

import org.hexaware.transactionservice.dtos.FundsSummaryDto;
import org.hexaware.transactionservice.dtos.LedgerEntryRequest;
import org.hexaware.transactionservice.dtos.ResponseDto;
import org.hexaware.transactionservice.entities.TransactionEntity;
import org.hexaware.transactionservice.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/txn-api/v1")
@CrossOrigin(origins = "http://localhost:4200")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/record")
    public ResponseEntity<?> recordTransaction(@RequestBody LedgerEntryRequest request) {
        // We call the service and return 201 Created
        transactionService.recordTransaction(request);
        return ResponseEntity.status(201).body("Transaction recorded in ledger");
    }

    @GetMapping("/history/{ownerId}")
    public ResponseEntity<Page<TransactionEntity>> getHistory(
            @PathVariable UUID ownerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(transactionService.getOwnerHistory(ownerId, page, size));
    }
}