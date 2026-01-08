package org.hexaware.userservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hexaware.userservice.enums.Roles;


import java.util.UUID;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Entity
@Table(name = "Role_Table")
public class Role {

    // keep a constructor that accepts Roles (assign enum directly)
    public Role(Roles role) {
        this.role = role;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "RoleId", nullable = false, updatable = false)
    private UUID Roleid;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, unique = true)
    private Roles role;
}
