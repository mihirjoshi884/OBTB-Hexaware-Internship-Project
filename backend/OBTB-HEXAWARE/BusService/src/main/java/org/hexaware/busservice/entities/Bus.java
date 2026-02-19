package org.hexaware.busservice.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hexaware.busservice.enums.BusType;

import java.util.UUID;

@Entity
@Table(name = "buses", indexes = {
        @Index(name = "idx_bus_company_id", columnList = "company_id")
})
@Getter @Setter
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID busId;

    // e.g., "NY-99-B-1234"

    private String busName;

    @Enumerated(EnumType.STRING)
    private BusType busType; // SEATER, SLEEPER, HYBRID

    // The "Parent" relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private BusTemplate template;

    //required at the time of registering bus.
    @Column(unique = true, nullable = false)
    private String registrationNumber;
    @Column(unique = true, nullable = false)
    private String driverLicenseNumber;
    @Column(unique = true, nullable = false)
    private String insurancePolicyNumber;
    @Column(unique = true, nullable = false)
    private String rcNumber;

    // Documentation fields
    // at the time of document upload
    private String registrationNumberPlateId;
    private String registrationNumberPlateUrl;
    private String driverLicenseDocId;
    private String driverLicenseUrl;
    private String insurancePolicyNumberUrl;
    private String insurancePolicyDocId;
    private String rcDocid;
    private String rcDocUrl; // Registration Certificate
}
