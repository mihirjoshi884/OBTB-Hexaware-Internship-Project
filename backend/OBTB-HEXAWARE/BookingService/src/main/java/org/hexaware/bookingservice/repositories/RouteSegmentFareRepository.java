package org.hexaware.bookingservice.repositories;

import org.hexaware.bookingservice.entites.RouteSegmentFare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RouteSegmentFareRepository extends JpaRepository<RouteSegmentFare, UUID> {
}
