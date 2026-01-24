package org.hexaware.transactionservice.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hexaware.transactionservice.enums.TransactionType;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions", indexes = {
        // Index for fast history lookups
        @Index(name = "idx_owner_history", columnList = "ownerId, createdAt DESC"),
        // Index for finding payment details by booking ID
        @Index(name = "idx_reference_lookup", columnList = "referenceId")
}) // Using lowercase plural is a SQL best practice
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID transactionId;

    // The user to whom this specific ledger entry belongs
    @Column(nullable = false)
    private UUID ownerId;

    // Links the Debit and Credit rows (e.g., the Booking ID)
    @Column(nullable = false)
    private String referenceId;

    @Column(nullable = false)
    private Double amount;

    // CREDIT (Money in) or DEBIT (Money out)
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType type;

    private String description;

    // The "Snapshot" of the balance after this specific transaction occurred
    @Column(nullable = false)
    private Double postTransactionBalance;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
}