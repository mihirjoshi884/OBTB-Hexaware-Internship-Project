package org.hexaware.bookingservice.repositories;

import org.hexaware.bookingservice.entites.BookingArchive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BookingArchiveRepository extends JpaRepository<BookingArchive, UUID> {
    BookingArchive findByPnrNumber(String pnrNumber);
}
