package org.hexaware.bookingservice.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "trip_archives")
@Getter @Setter @NoArgsConstructor
public class TripArchive {
    @Id
    private UUID instanceId; // Keep the same ID for traceability
    private UUID templateId;
    private LocalDateTime actualDeparture;
    private LocalDateTime actualArrival;
    private Double finalFare;
    private LocalDateTime archivedAt;
}