package org.hexaware.transactionservice.repository;

import org.hexaware.transactionservice.entities.TransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {

    /**
     * Finds all transactions for a specific owner.
     * The Pageable object will handle the LIMIT, OFFSET, and SORT.
     * Because of our index (ownerId, createdAt DESC), this query is O(log n).
     */
    Page<TransactionEntity> findByOwnerId(UUID ownerId, Pageable pageable);

    /**
     * Admin/System lookup to find both sides of a specific booking.
     * Uses idx_reference_lookup.
     */
    List<TransactionEntity> findAllByReferenceId(String referenceId);

    @Query("SELECT SUM(t.amount) FROM TransactionEntity t WHERE t.ownerId = :ownerId AND t.type = 'CREDIT'")
    Double getTotalRevenueByOwner(@Param("ownerId") UUID ownerId);
}