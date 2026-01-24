package org.hexaware.oauthservice.entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity @Table(name= "AuthIdentity")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AuthIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID authId;


    @Column(name = "UserId", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false, unique = true)
    private String hashPassword;


    @Column(name = "role_mapping_id", nullable = false)
    private UUID roleMappingId;

    @Column(name = "is_Active", nullable = false)
    private boolean is_Active;




    @Column(name = "is_verified", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean is_Verified;


    @Column(name = "password_updated_at")
    private Instant passwordUpdatedAt;

    @Column(name = "last_login_success")
    private Instant lastLoginSuccess;
}
