package org.hexaware.busservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "companies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID companyId;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String ownerName;

    @Column(unique = true,nullable = false)
    private UUID ownerId;
}
