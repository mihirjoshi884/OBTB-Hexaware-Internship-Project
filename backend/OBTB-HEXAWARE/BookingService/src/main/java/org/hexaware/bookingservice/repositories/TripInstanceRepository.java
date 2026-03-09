package org.hexaware.bookingservice.repositories;

import org.hexaware.bookingservice.entites.TripInstance;
import org.hexaware.bookingservice.enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TripInstanceRepository extends JpaRepository<TripInstance, UUID> {

    // Used by the Engine to find trips that have finished but are still marked 'SCHEDULED'
    List<TripInstance> findByStatusAndActualArrivalBefore(TripStatus status, LocalDateTime arrivalTime);

    // Used to check if an instance for a specific date already exists to prevent duplicates
    boolean existsByTemplate_TemplateIdAndActualDepartureBetween(
            UUID templateId,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );

    void deleteByTemplate_TemplateIdAndStatus(UUID templateId, TripStatus status);

    // Used for the Public Search API (Customer finding buses for a specific route/date)
    List<TripInstance> findByTemplate_RouteIdAndStatusAndActualDepartureBetween(
            UUID routeId,
            TripStatus status,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );
}