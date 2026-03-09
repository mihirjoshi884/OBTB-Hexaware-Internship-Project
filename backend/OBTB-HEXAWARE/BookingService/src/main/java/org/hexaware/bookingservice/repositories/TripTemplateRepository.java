package org.hexaware.bookingservice.repositories;

import org.hexaware.bookingservice.entites.TripTemplate;
import org.hexaware.bookingservice.enums.TripType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TripTemplateRepository extends JpaRepository<TripTemplate, UUID> {

    // Finds all active REGULAR schedules to generate future instances
    List<TripTemplate> findByIsActiveTrueAndTripType(TripType tripType);

    // Find templates by company (useful for the operator dashboard)
    List<TripTemplate> findByCompanyId(UUID companyId);
}