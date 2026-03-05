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
        // This allows A1 to exist for Trip 1 AND Trip 2, but not twice for Trip 1
        @Index(name = "idx_trip_seat_unique", columnList = "trip_id, seat_number", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TripSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID tripSeatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @Column(name = "seat_number")
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    private SeatType seatType;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    @Version
    private Integer version;
}