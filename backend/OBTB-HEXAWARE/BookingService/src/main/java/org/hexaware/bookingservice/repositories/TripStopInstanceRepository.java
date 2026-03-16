package org.hexaware.bookingservice.repositories;

import org.hexaware.bookingservice.entites.TripStopInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TripStopInstanceRepository extends JpaRepository<TripStopInstance, UUID> {


}
