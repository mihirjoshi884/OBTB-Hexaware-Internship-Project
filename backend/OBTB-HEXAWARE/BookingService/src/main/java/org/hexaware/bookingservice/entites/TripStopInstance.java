package org.hexaware.bookingservice.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "trip_stop_instances")
@Getter @Setter @NoArgsConstructor
public class TripStopInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "trip_instance_id")
    private TripInstance tripInstance;

    private String stopName;
    private Integer stopOrder;

    // These are the absolute times for this specific trip
    private LocalDateTime arrivalTime;
    private LocalDateTime departureTime;
}