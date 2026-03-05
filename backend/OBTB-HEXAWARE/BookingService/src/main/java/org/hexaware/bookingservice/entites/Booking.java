package org.hexaware.bookingservice.entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hexaware.bookingservice.enums.BookingStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID bookingId;

    private String pnrNumber; // Unique 8-digit string for the customer
    private UUID customerId;

    @ManyToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;

    private String seatNumber;
    private String sourceStop;
    private String destinationStop;

    private Double amountPaid;

    @Enumerated(EnumType.STRING)
    private BookingStatus status; // CONFIRMED, CANCELLED, COMPLETED

    private LocalDateTime bookingTimestamp;
}