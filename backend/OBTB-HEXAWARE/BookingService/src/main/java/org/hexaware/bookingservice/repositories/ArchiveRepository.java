package org.hexaware.bookingservice.repositories;

import org.hexaware.bookingservice.entites.TripArchive;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ArchiveRepository extends JpaRepository<TripArchive, UUID> {
}
