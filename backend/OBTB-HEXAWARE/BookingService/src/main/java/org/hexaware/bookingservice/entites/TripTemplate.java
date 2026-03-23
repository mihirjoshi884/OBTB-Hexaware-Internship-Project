package org.hexaware.bookingservice.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hexaware.bookingservice.enums.DayOfWeek;
import org.hexaware.bookingservice.enums.TripType;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "trip_templates", indexes = {
        @Index(name = "idx_template_company_id", columnList = "companyId"),
        @Index(name = "idx_template_active_type", columnList = "isActive, tripType")
})
@Getter @Setter @NoArgsConstructor @ToString
public class TripTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID templateId;

    private UUID routeId;
    private UUID busId;
    private UUID companyId;
    private Double baseFare;

    @Enumerated(EnumType.STRING)
    private TripType tripType; // REGULAR or ONE_TIME

    // Scheduled relative times
    private DayOfWeek scheduledDay;
    private java.time.LocalTime regularTime;
    private java.time.LocalTime departureTime;
    private java.time.LocalTime arrivalTime;
    private java.time.LocalDate departureDate;
    private java.time.LocalDate arrivalDate;

    private boolean isActive = true;
}