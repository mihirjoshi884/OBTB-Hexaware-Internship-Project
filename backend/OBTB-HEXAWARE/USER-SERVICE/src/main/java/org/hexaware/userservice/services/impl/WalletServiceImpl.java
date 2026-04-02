package org.hexaware.userservice.services.impl;

import jakarta.transaction.Transactional;
import org.hexaware.userservice.dtos.FundsSummaryDto;
import org.hexaware.userservice.dtos.LedgerEntryRequest;
import org.hexaware.userservice.entities.User;
import org.hexaware.userservice.enums.TransactionType;
import org.hexaware.userservice.repositories.UserRepository;
import org.hexaware.userservice.services.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebClient userservicedWebClient;

    @Value("${transactionservice.base-uri}")
    private String transactionServiceBaseUri;

    @Override
    @Transactional // 🛡️ Fix 1: Ensure both operations succeed or both fail together
    public void processWalletTransfer(UUID payerUserId, UUID payeeUserId, Double amount) {

        // 1. Fetch payer and payee
        var payer = userRepository.findByUserId(payerUserId)
                .orElseThrow(() -> new RuntimeException("Payer account not found!"));
        var payerWallet = payer.getWallet();

        var payee = userRepository.findByUserId(payeeUserId)
                .orElseThrow(() -> new RuntimeException("Payee (Bus Operator) account not found!"));
        var payeeWallet = payee.getWallet();

        // 🛡️ Fix 2: Check the PAYER'S balance, not the payee's!
        if (payerWallet.getBalance() < amount) {
            throw new RuntimeException("Insufficient wallet balance. Needed: " + amount);
        }

        // 2. Adjust balances
        payerWallet.setBalance(payerWallet.getBalance() - amount);
        payeeWallet.setBalance(payeeWallet.getBalance() + amount);

        // 3. Persist state
        userRepository.save(payer);
        userRepository.save(payee);

        // 🛡️ Fix 3: Generate reference IDs and record ledger entries for both parties!
        String sharedReference = "BK-" + UUID.randomUUID().toString().substring(0, 8);

        // Record Debit for Passenger
        recordTransaction(
                payer,
                amount,
                TransactionType.DEBIT,
                payerWallet.getBalance(),
                sharedReference,
                "Ticket Booking Payment"
        );

        // Record Credit for Bus Operator
        recordTransaction(
                payee,
                amount,
                TransactionType.CREDIT,
                payeeWallet.getBalance(),
                sharedReference,
                "Ticket Sale Payout"
        );
    }

    // 🛡️ Fix 4: Made it more flexible to handle both DEBIT and CREDIT!
    private void recordTransaction(User user, Double amount, TransactionType type, Double postBalance, String reference, String description) {

        var ledgerEntryRequest = new LedgerEntryRequest(
                user.getUserId(),
                amount,
                type,
                postBalance,
                reference,
                description
        );

        userservicedWebClient.post()
                .uri(transactionServiceBaseUri + "/txn-api/v1/record")
                .bodyValue(ledgerEntryRequest)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        Mono.error(new RuntimeException("Transaction Service failed to record the ledger!"))
                )
                .toBodilessEntity()
                .block(); // Blocks until the transaction service responds
    }
}