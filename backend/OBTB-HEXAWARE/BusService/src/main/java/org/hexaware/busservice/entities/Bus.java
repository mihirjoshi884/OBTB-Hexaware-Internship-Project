package org.hexaware.busservice.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hexaware.busservice.enums.VerificationStatus;

import java.util.List;
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
    private String busName;

    // The "Parent" relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private BusTemplate template;

    @OneToMany(mappedBy = "bus", cascade = CascadeType.ALL)
    private List<BusStaff> staffMembers;

    //required at the time of registering bus.
    @Column(unique = true, nullable = false)
    private String registrationNumber;
    @Column(unique = true, nullable = false)
    private String insurancePolicyNumber;
    @Column(unique = true, nullable = false)
    private String rcNumber;

    private VerificationStatus status = VerificationStatus.NOT_SUBMITTED;

    // Documentation fields
    // at the time of document upload
    private String registrationNumberPlateDocId;
    private String registrationNumberPlateDOCUrl;
    private String insurancePolicyDocUrl;
    private String insurancePolicyDocId;
    private String rcDocid;
    private String rcDocUrl; // Registration Certificate
}
