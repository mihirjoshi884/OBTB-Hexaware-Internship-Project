package org.hexaware.bookingservice.entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hexaware.bookingservice.enums.SeatStatus;
import org.hexaware.bookingservice.enums.SeatType;

import java.util.UUID;

@Entity
@Table(name = "trip_seats", indexes = {
        // Updated index to use instance_id
        @Index(name = "idx_trip_seat_unique", columnList = "instance_id, seat_number", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TripSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID tripSeatId;

    // Change from Trip to TripInstance
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instance_id")
    private TripInstance tripInstance;

    @Column(name = "seat_number")
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    private SeatType seatType;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    @Version
    private Integer version;
}