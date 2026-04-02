package org.hexaware.bookingservice.entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "primary_passenger_details")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class PrimaryPassangerDetail {

    @Id
    private UUID userId;

    private String name;
    private String email;
    private String phone;
    private String emergencyContactName;
    private String emergencyContact;


    @OneToMany(mappedBy = "primaryPassengerDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Passangers> coPassengers = new ArrayList<>();
}
