package org.hexaware.oauthservice.repositories;

import org.hexaware.oauthservice.entites.SecurityQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SecurityQuestionRepository extends JpaRepository<SecurityQuestion, UUID> {
    Optional<SecurityQuestion> findByUserId(UUID userId);
}
