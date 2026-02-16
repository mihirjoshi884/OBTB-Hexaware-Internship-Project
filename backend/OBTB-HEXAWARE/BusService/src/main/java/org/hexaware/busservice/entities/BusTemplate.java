package org.hexaware.busservice.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "bus_templates")
@Getter @Setter
public class BusTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID templateId;

    private String templateName; // e.g., "Volvo 2+2 Sleeper"

    /**
     * layoutData will store the JSON grid.
     * Example: [{"id": "1A", "x": 0, "y": 0, "type": "SLEEPER"}, ...]
     */
    @Column(columnDefinition = "jsonb")
    private String layoutData;

    private Integer totalSeats;
}