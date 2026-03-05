package org.hexaware.bookingservice.repositories;

import org.hexaware.bookingservice.entites.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {
}
