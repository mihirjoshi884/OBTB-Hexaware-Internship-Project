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
@Table(name = "booking_archives")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BookingArchive {
    @Id
    private UUID bookingId;

    private String pnrNumber;

    private String primaryPassengerName;
    private String primaryPassengerEmail;

    private UUID tripId;
    private String sourceStop;
    private String destinationStop;
    private Double amountPaid;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private LocalDateTime bookingTimestamp;
    private LocalDateTime archivedTimestamp;
}