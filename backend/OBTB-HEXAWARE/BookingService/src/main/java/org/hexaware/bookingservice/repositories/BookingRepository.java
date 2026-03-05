package org.hexaware.bookingservice.repositories;

import org.hexaware.bookingservice.entites.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
}
