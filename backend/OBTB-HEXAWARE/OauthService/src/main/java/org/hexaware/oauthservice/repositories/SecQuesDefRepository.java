package org.hexaware.oauthservice.repositories;

import org.hexaware.oauthservice.entites.SecurityQuestionDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SecQuesDefRepository extends JpaRepository<SecurityQuestionDefinition, UUID> {
    Optional<SecurityQuestionDefinition> findByIsActive(boolean active);
}
