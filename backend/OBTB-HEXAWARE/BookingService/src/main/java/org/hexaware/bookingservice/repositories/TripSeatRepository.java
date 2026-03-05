package org.hexaware.bookingservice.repositories;

import org.hexaware.bookingservice.entites.TripSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TripSeatRepository extends JpaRepository<TripSeat, UUID> {
}
