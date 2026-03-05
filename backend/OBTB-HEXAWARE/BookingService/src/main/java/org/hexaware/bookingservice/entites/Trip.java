package org.hexaware.bookingservice.entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hexaware.bookingservice.enums.TripType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "trips", indexes = {
        @Index(name = "idx_trip_bus_id", columnList = "busId"),
        @Index(name = "idx_trip_company_id", columnList = "companyId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID tripId;

    private UUID routeId;   // From Bus Service
    private UUID busId;     // From Bus Service
    private UUID companyId; // From Bus Service

    @Enumerated(EnumType.STRING)
    private TripType tripType; // REGULAR, ONE_TIME

    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL)
    private List<TripSeat> seatMap = new ArrayList<>();

    private Double baseFare; // Default fare for the full route
}
