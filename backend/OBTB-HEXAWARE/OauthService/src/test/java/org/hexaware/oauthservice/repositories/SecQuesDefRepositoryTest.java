package org.hexaware.oauthservice.repositories;

import org.hexaware.oauthservice.entites.SecurityQuestionDefinition;
import org.hexaware.oauthservice.repositories.SecQuesDefRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class SecQuesDefRepositoryTest {

    @Autowired
    private SecQuesDefRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private SecurityQuestionDefinition activeQuestion;
    private SecurityQuestionDefinition inactiveQuestion;

    @BeforeEach
    void setUp() {
        activeQuestion = new SecurityQuestionDefinition("What is your pet's name?");
        activeQuestion.setActive(true);

        inactiveQuestion = new SecurityQuestionDefinition("What was your first car?");
        inactiveQuestion.setActive(false);

        entityManager.persist(activeQuestion);
        entityManager.persist(inactiveQuestion);
        entityManager.flush();
    }

    @Test
    @DisplayName("Find By IsActive: Should return an active question when active is true")
    void testFindByIsActive_True() {
        Optional<SecurityQuestionDefinition> result = repository.findByIsActive(true);

        assertTrue(result.isPresent());
        assertEquals("What is your pet's name?", result.get().getQuestionText());
        assertTrue(result.get().isActive());
    }

    @Test
    @DisplayName("Find By IsActive: Should return an inactive question when active is false")
    void testFindByIsActive_False() {
        Optional<SecurityQuestionDefinition> result = repository.findByIsActive(false);

        assertTrue(result.isPresent());
        assertEquals("What was your first car?", result.get().getQuestionText());
        assertFalse(result.get().isActive());
    }

    @Test
    @DisplayName("Save Question: Should automatically generate UUID for new question")
    void testSave_GeneratesUUID() {
        SecurityQuestionDefinition newQuestion = new SecurityQuestionDefinition("Where were you born?");
        SecurityQuestionDefinition saved = repository.save(newQuestion);

        assertNotNull(saved.getQuestionId());
        assertTrue(saved.isActive()); // Testing the default value logic
    }

    @Test
    @DisplayName("Unique Constraint: Should throw exception when saving duplicate question text")
    void testUniqueQuestionText() {
        SecurityQuestionDefinition duplicate = new SecurityQuestionDefinition("What is your pet's name?");

        assertThrows(DataIntegrityViolationException.class, () -> {
            repository.saveAndFlush(duplicate);
        });
    }
}