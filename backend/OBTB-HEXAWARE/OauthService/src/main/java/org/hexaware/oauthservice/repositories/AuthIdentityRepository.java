package org.hexaware.oauthservice.repositories;


import org.hexaware.oauthservice.dtos.Summary;
import org.hexaware.oauthservice.entites.AuthIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface AuthIdentityRepository extends JpaRepository<AuthIdentity, UUID> {
    Optional<AuthIdentity> findByUserId(UUID userId);
    Optional<AuthIdentity> findByUsername(String username);
}
