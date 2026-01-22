package org.hexaware.oauthservice.repositories;

import org.hexaware.oauthservice.entites.UserLockout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserLockOutRepository extends JpaRepository<UserLockout, UUID> {
    Optional<UserLockout> findByUserId(UUID userId);
}
