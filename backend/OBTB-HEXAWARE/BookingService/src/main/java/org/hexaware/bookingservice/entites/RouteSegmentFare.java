package org.hexaware.bookingservice.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
public class RouteSegmentFare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID routeId;
    private String sourceStop;
    private String destinationStop;

    private Double fare; // Specific price for this sub-route
}