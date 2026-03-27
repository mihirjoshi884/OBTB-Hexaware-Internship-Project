package org.hexaware.bookingservice.repositories;

import org.hexaware.bookingservice.dtos.searchDtos.TripSearchResponseDto;
import org.hexaware.bookingservice.entites.TripInstance;
import org.hexaware.bookingservice.enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TripInstanceRepository extends JpaRepository<TripInstance, UUID> {

    // Used by the Engine to find trips that have finished but are still marked 'SCHEDULED'
    List<TripInstance> findByStatusAndActualArrivalBefore(TripStatus status, LocalDateTime arrivalTime);

    @Query("SELECT ti FROM TripInstance ti WHERE ti.status = :status " +
            "AND ti.actualArrival < :arrivalTime " +
            "AND ti.instanceId NOT IN (SELECT ta.instanceId FROM TripArchive ta)")
    List<TripInstance> findStaleUnarchivedTrips(
            @Param("status") TripStatus status,
            @Param("arrivalTime") LocalDateTime arrivalTime
    );

    // Used to check if an instance for a specific date already exists to prevent duplicates
    boolean existsByTemplate_TemplateIdAndActualDepartureBetween(
            UUID templateId,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );

    void deleteByTemplate_TemplateIdAndStatus(UUID templateId, TripStatus status);

    @Query("SELECT DISTINCT ti FROM TripInstance ti " +
            "LEFT JOIN FETCH ti.stops " +
            "JOIN ti.template t " +
            "WHERE ti.status = :status " +
            "AND t.templateId IN :templateIds")
    List<TripInstance> findAllByTemplate_TemplateIds(
            @Param("templateIds") List<UUID> templateIds,
            @Param("status") TripStatus status
    );


    @Query("""
        SELECT DISTINCT ti FROM TripInstance ti
        LEFT JOIN FETCH ti.template t
        LEFT JOIN FETCH ti.stops s
        WHERE t.templateId IN :templateIds
        AND (
            CAST(ti.actualDeparture AS LocalDate) = CAST(:departureDate AS LocalDate)
            OR EXISTS (
                SELECT 1 FROM TripStopInstance tsi 
                WHERE tsi.tripInstance = ti 
                AND CAST(tsi.departureTime AS LocalDate) = CAST(:departureDate AS LocalDate)
            )
        )
    """)
    List<TripInstance> findAvailableTrips(
            @Param("templateIds") List<UUID> templateIds,
            @Param("departureDate") LocalDateTime departureDate
    );

    @Query("SELECT ti FROM TripInstance ti WHERE ti.template.templateId IN :templateIds " +
            "AND ti.actualDeparture BETWEEN :start AND :end " +
            "AND ti.status = 'SCHEDULED'")
    List<TripInstance> findAvailableTripsInRange(
            @Param("templateIds") List<UUID> templateIds,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}