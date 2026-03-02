package org.hexaware.busservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity @Table (name = "routes", indexes = {
        @Index(name = "idx_bus_routes_company_id",columnList = "company_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)

    private UUID routeId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    private String routeName; // e.g., "Express: Secunderabad to Bengaluru"
    private String origin;
    private String destination;
    private Double totalDistance; // km
    private Integer estimatedDuration; // in minutes

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stopOrder ASC")
    private List<RouteStop> stops = new ArrayList<>();
}
