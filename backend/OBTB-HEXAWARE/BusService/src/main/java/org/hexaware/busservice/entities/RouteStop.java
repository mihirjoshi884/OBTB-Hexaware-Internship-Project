package org.hexaware.busservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;

    private String stopName;
    private Integer stopOrder; // 0 for Origin, 1, 2...
    private Double distanceFromPreviousStop;
    private Integer timeOffsetFromOrigin; // minutes from start
}