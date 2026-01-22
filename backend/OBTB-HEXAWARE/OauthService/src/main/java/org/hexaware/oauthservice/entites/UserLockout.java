package org.hexaware.oauthservice.entites;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
@Entity @Table(name = "UserLockOut")
public class UserLockout {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userLockOutId;

    @Column(name = "UserId", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "Login-Counter", nullable = false, unique = false)
    private Integer loginCounter;

    @Column(name = "is_Locked", nullable = false, unique = false)
    private boolean isLocked;

    @Column(name = "Attempt-1")
    private LocalDateTime attempt1;

    @Column(name = "Attempt-2")
    private LocalDateTime attempt2;

    @Column(name = "Attempt-3")
    private LocalDateTime attempt3;
}
