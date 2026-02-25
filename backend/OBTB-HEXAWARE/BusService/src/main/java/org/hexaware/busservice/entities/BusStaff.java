package org.hexaware.busservice.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hexaware.busservice.enums.DutyType;
import org.hexaware.busservice.enums.StaffType;

import java.util.UUID;

@Entity
@Table(name = "bus_staff", indexes = {
        @Index(name = "idx_staff_bus_id", columnList = "bus_id"),
        @Index(name = "idx_staff_company_id", columnList = "company_id")
})
@Getter @Setter
public class BusStaff {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID staffId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StaffType staffType;

    @Column(nullable = false)
    private String name;

    private int age;

    @Enumerated(EnumType.STRING)
    @Column(name = "duty_type", nullable = true)
    private DutyType dutyType;

    @Column(unique = true)
    private String phoneNumber;

    // Driver specific fields (logic handles necessity)
    private String driverLicenseNumber;
    private String driverLicenseDocId;
    private String driverLicenseUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id")
    private Bus bus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}