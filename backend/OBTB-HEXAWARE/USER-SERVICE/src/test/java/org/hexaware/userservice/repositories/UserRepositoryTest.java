package org.hexaware.userservice.repositories;

import org.hexaware.userservice.entities.User;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private  TestEntityManager testEntityManager;

    private User savedUser;

    @BeforeEach
    public  void setUpUser(){
        User user = new User();
        // DO NOT set userId here. Let the DB do it.
        user.setFirstName("Firstname");
        user.setLastName("Lastname");
        user.setUsername("username");
        user.setEmail("email@test.com");
        user.setContact("123456789");
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        // persist() makes the object 'Managed' and assigns an ID
        this.savedUser = testEntityManager.persistFlushFind(user);
    }
    @Test
    @DisplayName("find user by its userId")
    public void testfindByUserId() {

        Optional<User> result = userRepository.findByUserId(savedUser.getUserId());

        // ASSERT
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("Firstname", result.get().getFirstName());
    }

    @Test
    @DisplayName("test Nullability Constraints if username is null")
    public void testNullabilityConstraints() {
        // 1. Arrange: Create a user without a username
        User invalidUser = new User();
        invalidUser.setFirstName("Firstname");
        invalidUser.setLastName("Lastname");
        invalidUser.setEmail("email2@test.com"); // Use a different email to avoid unique constraint issues
        invalidUser.setContact("987654321");
        invalidUser.setCreatedAt(Instant.now());
        invalidUser.setUpdatedAt(Instant.now());

        // We leave the username as NULL
        invalidUser.setUsername(null);

        // 2. Act & Assert:
        // We expect an exception when we flush to the database because of the NOT NULL constraint
        Assertions.assertThrows(Exception.class, () -> {
            testEntityManager.persistAndFlush(invalidUser);
        });
    }


}
