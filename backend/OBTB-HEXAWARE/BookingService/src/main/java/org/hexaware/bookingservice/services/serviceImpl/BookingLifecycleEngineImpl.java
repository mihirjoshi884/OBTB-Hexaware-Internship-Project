package org.hexaware.bookingservice.services.serviceImpl;

import jakarta.transaction.Transactional;
import org.hexaware.bookingservice.entites.Booking;
import org.hexaware.bookingservice.entites.BookingArchive;
import org.hexaware.bookingservice.enums.BookingStatus;
import org.hexaware.bookingservice.repositories.BookingArchiveRepository;
import org.hexaware.bookingservice.repositories.BookingRepository;
import org.hexaware.bookingservice.services.BookingLifecycleEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class BookingLifecycleEngineImpl implements BookingLifecycleEngine {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingArchiveRepository bookingArchiveRepository;

    @Override
    @Transactional
    public synchronized void processBookingLifecycle() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println("Booking Lifecycle Engine started at: " + now);

        // 1. Get the stale bookings (Trip must have arrived before 'now')
        List<BookingStatus> removableStatuses = Arrays.asList(
                BookingStatus.COMPLETED,
                BookingStatus.CANCELLED
        );

        List<Booking> staleBookings = bookingRepository.findStaleBookings(removableStatuses, now);

        for (Booking booking : staleBookings) {

            // CHECK: Does this archive already exist?
            if (!bookingArchiveRepository.existsById(booking.getBookingId())) {

                // 2. Create Archive Record mapped from active record
                BookingArchive archive = new BookingArchive();
                archive.setBookingId(booking.getBookingId());
                archive.setPnrNumber(booking.getPnrNumber());

                if (booking.getPrimaryPassengerDetail() != null) {
                    archive.setPrimaryPassengerName(booking.getPrimaryPassengerDetail().getName());
                    archive.setPrimaryPassengerEmail(booking.getPrimaryPassengerDetail().getEmail());
                }

                if (booking.getTrip() != null) {
                    archive.setTripId(booking.getTrip().getInstanceId());
                }

                archive.setSourceStop(booking.getSource());
                archive.setDestinationStop(booking.getDestination());
                archive.setAmountPaid(booking.getAmountPaid());
                archive.setStatus(booking.getBookingStatus());
                archive.setBookingTimestamp(booking.getBookingTimestamp());
                archive.setArchivedTimestamp(LocalDateTime.now());

                bookingArchiveRepository.save(archive);
            } else {
                System.out.println("Archive already exists for booking: " + booking.getBookingId() + ". Skipping insert.");
            }

            // 3. The "Nuclear" Clean Slate - ALWAYS delete from active tables
            bookingRepository.delete(booking);
        }

        // Final flush to ensure consistency
        bookingRepository.flush();
        System.out.println("Archived and purged " + staleBookings.size() + " stale bookings.");
    }
}
