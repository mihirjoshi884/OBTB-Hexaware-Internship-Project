package org.hexaware.bookingservice.repositories;

import org.hexaware.bookingservice.entites.TripArchive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ArchiveRepository extends JpaRepository<TripArchive, UUID> {
}
