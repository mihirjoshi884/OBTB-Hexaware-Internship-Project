package org.hexaware.busservice.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hexaware.busservice.enums.BusType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    @JdbcTypeCode(SqlTypes.JSON) // <--- ADD THIS LINE
    @Column(columnDefinition = "jsonb")
    private String layoutData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "layoutId")
    private LayoutTemplate layoutTemplate;

    @Enumerated(EnumType.STRING)
    private BusType busType;

    private Integer totalSeats;
}