package org.hexaware.bookingservice.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hexaware.bookingservice.enums.TripStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "trip_instances", indexes = {
        @Index(name = "idx_instance_departure", columnList = "actualDeparture"),
        @Index(name = "idx_instance_status", columnList = "status")
})
@Getter @Setter @NoArgsConstructor
public class TripInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID instanceId;

    @ManyToOne
    @JoinColumn(name = "template_id")
    private TripTemplate template;

    private LocalDateTime actualDeparture;
    private LocalDateTime actualArrival;

    @Enumerated(EnumType.STRING)
    private TripStatus status; // SCHEDULED, COMPLETED

    @OneToMany(mappedBy = "tripInstance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripSeat> seatMap = new ArrayList<>();
}