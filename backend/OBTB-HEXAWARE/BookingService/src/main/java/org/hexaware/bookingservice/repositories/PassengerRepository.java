package org.hexaware.bookingservice.repositories;

import org.hexaware.bookingservice.entites.Passangers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PassengerRepository extends JpaRepository<Passangers, UUID> {
}
