package org.hexaware.busservice.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hexaware.busservice.enums.BusType;

import java.util.UUID;

@Entity
@Table(name = "layout_templates")
@Getter @Setter
public class LayoutTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID layoutId;

    private String name; // e.g., "Standard 2+2 Seater"
    private String description;
    private Boolean isDoubleDecker;
    private Integer columns;
    private Integer aisleAfterColumn; // e.g., 2 (for a 2+2 layout)

    @Enumerated(EnumType.STRING)
    private BusType defaultType;
}
