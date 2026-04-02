package org.hexaware.bookingservice.repositories;

import org.hexaware.bookingservice.entites.Booking;
import org.hexaware.bookingservice.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    // Find all bookings with a specific status (e.g., COMPLETED) for archival
    List<Booking> findByBookingStatus(BookingStatus status);

    // Optional: Find by PNR if you need to look up a booking quickly
    Booking findByPnrNumber(String pnrNumber);

    @Query("SELECT b FROM Booking b JOIN b.trip t WHERE " +
            "b.bookingStatus IN :statuses AND t.actualArrival < :currentTime")
    List<Booking> findStaleBookings(
            @Param("statuses") List<BookingStatus> statuses,
            @Param("currentTime") LocalDateTime currentTime
    );

    @jakarta.transaction.Transactional
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE Booking b SET b.trip = null WHERE b.trip.instanceId = :instanceId")
    void nullifyTripReference(@Param("instanceId") UUID instanceId);
}
