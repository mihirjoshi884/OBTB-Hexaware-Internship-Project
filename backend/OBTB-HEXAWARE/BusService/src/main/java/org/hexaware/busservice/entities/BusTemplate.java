package org.hexaware.busservice.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hexaware.busservice.enums.BusType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "bus_templates", indexes = {
        @Index(name= "idx_bus_template_company_id",columnList = "company_id")
})
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    private BusType busType;

    private Integer totalSeats;
}