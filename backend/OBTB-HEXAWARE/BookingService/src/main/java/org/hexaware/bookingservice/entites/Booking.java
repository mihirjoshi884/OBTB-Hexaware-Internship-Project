package org.hexaware.bookingservice.entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hexaware.bookingservice.enums.BookingStatus;
import org.hexaware.bookingservice.enums.PaymentStatus;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID bookingId;

    private String pnrNumber;

    // Changed to ManyToOne because a user could realistically make multiple bookings over time!
    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "userId")
    private PrimaryPassangerDetail primaryPassengerDetail;

    @ManyToOne
    @JoinColumn(
            name = "trip_instance_id",
            referencedColumnName = "instanceId"
    )
    private TripInstance trip;

    private String source;
    //if source is intermediate stop
    private LocalDateTime sourceArrival;
    private LocalDateTime sourceDeparture;
    //if destination is terminal stop or not
    private LocalDateTime destinationArrival;
    //if destination is terminal stop
    private LocalDateTime destinationDeparture;
    private String destination;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    private Double amountPaid;

    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime bookingTimestamp;
}