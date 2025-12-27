package org.hexaware.oauthservice.repositories;

import org.hexaware.oauthservice.entites.SecurityQuestion;
import org.hexaware.oauthservice.repositories.SecurityQuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;


import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class SecurityQuestionRepositoryTest {

    @Autowired
    private SecurityQuestionRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private UUID testUserId;
    private SecurityQuestion savedQuestion;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        SecurityQuestion question = new SecurityQuestion();
        question.setUserId(testUserId);
        question.setQuestionAndAnswer1("What is your pet's name?|Buddy");
        question.setQuestionAndAnswer2("What was your first car?|Toyota");

        // Persist to the in-memory H2 database
        savedQuestion = entityManager.persistAndFlush(question);
    }

    @Test
    @DisplayName("Find By UserId: Should return SecurityQuestion when valid UUID is provided")
    void testFindByUserId_Success() {
        // Act - Pass the actual UUID object, not the String
        Optional<SecurityQuestion> result = repository.findByUserId(testUserId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUserId, result.get().getUserId());
    }

    @Test
    @DisplayName("Find By UserId: Should return empty Optional when userId does not exist")
    void testFindByUserId_NotFound() {
        // Act - Pass a random UUID object
        Optional<SecurityQuestion> result = repository.findByUserId(UUID.randomUUID());

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Save Question: Should verify unique constraint on UserId")
    void testUniqueUserIdConstraint() {
        // Attempt to create a second security question entry for the SAME user
        SecurityQuestion duplicateEntry = new SecurityQuestion();
        duplicateEntry.setUserId(testUserId);
        duplicateEntry.setQuestionAndAnswer1("Q1|A1");
        duplicateEntry.setQuestionAndAnswer2("Q2|A2");

        // Assert that saving a duplicate userId throws a PersistenceException/DataIntegrityViolation
        assertThrows(Exception.class, () -> {
            repository.saveAndFlush(duplicateEntry);
        }, "Should fail because userId is marked as unique in the entity");
    }
}