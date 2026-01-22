package org.hexaware.userservice.repositories;


import org.hexaware.userservice.dtos.UserSummary;
import org.hexaware.userservice.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUserId(UUID userId);

    Optional<User> findByUsername(String username);


}
