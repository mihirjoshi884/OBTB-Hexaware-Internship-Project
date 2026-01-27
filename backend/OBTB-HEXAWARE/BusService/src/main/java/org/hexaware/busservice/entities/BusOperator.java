package org.hexaware.busservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hexaware.busservice.enums.VerificationStatus;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "bus_operator_profiles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class BusOperator {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID busOperatorId;

    @Column(nullable = false, unique = true)
    private UUID userId; // Links to your User.java in UserService

    @Enumerated(EnumType.STRING)
    private VerificationStatus status = VerificationStatus.NOT_SUBMITTED;

    // Personal Docs (Phase 1)
    private String aadharFileId;
    private String aadharUrl;
    private String aadharNumber;

    private String panFileId;
    private String panUrl;
    private String panNumber;

    // Commercial Status (Phase 2)
    private boolean canAddBuses = false;

    @CreationTimestamp
    private LocalDateTime submittedAt;
    private LocalDateTime verifiedAt;
}
