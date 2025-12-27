package org.hexaware.oauthservice.repositories;

import org.hexaware.oauthservice.entites.UserLockout;
import org.hexaware.oauthservice.repositories.UserLockOutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserLockOutRepositoryTest {

    @Autowired
    private UserLockOutRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private UUID testUserId;
    private UserLockout lockoutRecord;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        lockoutRecord = new UserLockout();
        lockoutRecord.setUserId(testUserId);
        lockoutRecord.setLoginCounter(2);
        lockoutRecord.setLocked(false);
        lockoutRecord.setAttempt1(LocalDateTime.now().minusMinutes(10));
        lockoutRecord.setAttempt2(LocalDateTime.now().minusMinutes(5));
        // attempt3 remains null for now

        entityManager.persistAndFlush(lockoutRecord);
    }

    @Test
    @DisplayName("Find By UserId: Should return lockout record when valid UUID is provided")
    void testFindByUserId_Success() {
        // Act
        Optional<UserLockout> result = repository.findByUserId(testUserId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(2, result.get().getLoginCounter());
        assertFalse(result.get().isLocked());
        assertNotNull(result.get().getAttempt1());
        assertNull(result.get().getAttempt3());
    }

    @Test
    @DisplayName("Find By UserId: Should return empty Optional when user has no lockout record")
    void testFindByUserId_NotFound() {
        Optional<UserLockout> result = repository.findByUserId(UUID.randomUUID());
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Update Lockout: Should correctly update counter and lock status")
    void testUpdateLockoutStatus() {
        // Arrange
        UserLockout existing = repository.findByUserId(testUserId).get();
        existing.setLoginCounter(3);
        existing.setLocked(true);
        existing.setAttempt3(LocalDateTime.now());

        // Act
        repository.saveAndFlush(existing);
        entityManager.clear(); // Clear cache to force load from DB

        // Assert
        UserLockout updated = repository.findByUserId(testUserId).get();
        assertEquals(3, updated.getLoginCounter());
        assertTrue(updated.isLocked());
        assertNotNull(updated.getAttempt3());
    }

    @Test
    @DisplayName("Unique Constraint: Should fail when creating a duplicate lockout record for the same UserID")
    void testUniqueUserIdConstraint() {
        UserLockout duplicate = new UserLockout();
        duplicate.setUserId(testUserId); // Same ID as setup
        duplicate.setLoginCounter(0);
        duplicate.setLocked(false);

        assertThrows(DataIntegrityViolationException.class, () -> {
            repository.saveAndFlush(duplicate);
        });
    }
}