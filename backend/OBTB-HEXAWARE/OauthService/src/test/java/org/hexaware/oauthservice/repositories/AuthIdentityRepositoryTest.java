package org.hexaware.oauthservice.repositories;

import org.hexaware.oauthservice.entites.AuthIdentity;
import org.hexaware.oauthservice.repositories.AuthIdentityRepository;
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
public class AuthIdentityRepositoryTest {

    @Autowired
    private AuthIdentityRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private AuthIdentity identity;
    private UUID userId;

    @BeforeEach
    public void setup() {
        userId = UUID.randomUUID();

        identity = new AuthIdentity();
        identity.setUserId(userId);
        identity.setUsername("john_doe");
        identity.setHashPassword("secure_hash");
        identity.setRoleMappingId(UUID.randomUUID());
        identity.set_Active(true);
        identity.set_Verified(false);

        // Use TestEntityManager to persist data for the test
        entityManager.persist(identity);
        entityManager.flush();
    }

    @Test
    @DisplayName("Find By UserId: Should return AuthIdentity when userId exists")
    void testFindByUserId_ReturnsIdentity() {
        // Act
        Optional<AuthIdentity> found = repository.findByUserId(userId);

        // Assert
        assertTrue(found.isPresent());
        assertEquals("john_doe", found.get().getUsername());
        assertEquals(userId, found.get().getUserId());
    }

    @Test
    @DisplayName("Find By Username: Should return AuthIdentity when username exists")
    void testFindByUsername_ReturnsIdentity() {
        // Act
        Optional<AuthIdentity> found = repository.findByUsername("john_doe");

        // Assert
        assertTrue(found.isPresent());
        assertEquals(userId, found.get().getUserId());
    }

    @Test
    @DisplayName("Find By UserId: Should return empty when userId does not exist")
    void testFindByUserId_NotFound() {
        // Act
        Optional<AuthIdentity> found = repository.findByUserId(UUID.randomUUID());

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Save Identity: Should generate authId automatically")
    void testSave_GeneratesId() {
        AuthIdentity newIdentity = new AuthIdentity();
        newIdentity.setUserId(UUID.randomUUID());
        newIdentity.setUsername("new_user");
        newIdentity.setHashPassword("pass");
        newIdentity.setRoleMappingId(UUID.randomUUID());

        AuthIdentity saved = repository.save(newIdentity);

        assertNotNull(saved.getAuthId());
        assertEquals("new_user", saved.getUsername());
    }
}