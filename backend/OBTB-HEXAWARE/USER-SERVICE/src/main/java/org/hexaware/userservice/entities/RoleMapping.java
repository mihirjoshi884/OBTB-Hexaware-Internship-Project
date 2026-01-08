package org.hexaware.userservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity
public class RoleMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "roleMappingId", nullable = false, updatable = false)
    private UUID roleMappingId;
    @Column(nullable = false, name = "roleId")
    private UUID roleId;
    @Column(unique = true, nullable = false, name = "userId")
    private UUID userId;
}
