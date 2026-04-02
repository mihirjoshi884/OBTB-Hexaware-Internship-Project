package org.hexaware.bookingservice.entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hexaware.bookingservice.enums.IdProofType;

import java.util.UUID;


@Entity
@Table(name = "passengers")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Passangers {

    @Id
    private UUID passengerId;

    private String passengerName;
    private Integer age;
    private String gender;

    @Enumerated(EnumType.STRING)
    private IdProofType idProofType;

    private String idNumber;
    private String idUrl;

    // Added to resolve the mappedBy error
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_passenger_id")
    private PrimaryPassangerDetail primaryPassengerDetail;

    // Associate the seat directly to the person sitting in it!
    private String seatNumber;
}