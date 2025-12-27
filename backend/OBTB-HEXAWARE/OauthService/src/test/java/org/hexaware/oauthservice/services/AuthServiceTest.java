package org.hexaware.oauthservice.services;

import org.hexaware.oauthservice.dtos.UserStatusResponse;
import org.hexaware.oauthservice.entites.AuthIdentity;
import org.hexaware.oauthservice.repositories.AuthIdentityRepository;
import org.hexaware.oauthservice.repositories.SecurityQuestionRepository;
import org.hexaware.oauthservice.repositories.UserLockOutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private AuthIdentityRepository authIdentityRepository;

    @Mock
    private SecurityQuestionRepository securityQuestionRepository;

    @Mock
    private UserLockOutRepository userLockOutRepository;

    private UUID userId;
    private AuthIdentity mockAuth;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockAuth = new AuthIdentity();
        mockAuth.setUserId(userId);
        mockAuth.setUsername("testuser");
        mockAuth.set_Active(false);
        mockAuth.set_Verified(false);
    }

    @Test
    @DisplayName("Activate User: Should return status response when user exists")
    void testActivateUser_Success() {
        // Arrange
        when(authIdentityRepository.findByUserId(userId)).thenReturn(Optional.of(mockAuth));
        when(authIdentityRepository.save(any(AuthIdentity.class))).thenReturn(mockAuth);

        // Act
        UserStatusResponse response = authService.activateUser(userId);

        // Assert
        assertNotNull(response);
        assertTrue(response.isActive());
        assertEquals("testuser", response.username());
        verify(authIdentityRepository).save(mockAuth);
    }

    @Test
    @DisplayName("Activate User: Should throw RuntimeException when user not found")
    void testActivateUser_NotFound() {
        when(authIdentityRepository.findByUserId(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.activateUser(userId);
        });

        assertEquals("something went wrong", exception.getMessage());
    }

    @Test
    @DisplayName("Verify User: Should return status response and set verified to true")
    void testVerifyUser_Success() {
        // Arrange
        when(authIdentityRepository.findByUserId(userId)).thenReturn(Optional.of(mockAuth));
        when(authIdentityRepository.save(any(AuthIdentity.class))).thenReturn(mockAuth);

        // Act
        UserStatusResponse response = authService.verifyUser(userId);

        // Assert
        assertNotNull(response);
        assertTrue(response.isVerified());
        verify(authIdentityRepository).save(mockAuth);
    }

    @Test
    @DisplayName("IsVerified: Should return true if user is verified")
    void testIsVerified_True() {
        mockAuth.set_Verified(true);
        when(authIdentityRepository.findByUserId(userId)).thenReturn(Optional.of(mockAuth));

        boolean result = authService.isVerified(userId);

        assertTrue(result);
    }

    @Test
    @DisplayName("IsActive: Should return false if user is not active")
    void testIsActive_False() {
        mockAuth.set_Active(false);
        when(authIdentityRepository.findByUserId(userId)).thenReturn(Optional.of(mockAuth));

        boolean result = authService.isActive(userId);

        assertFalse(result);
    }

    @Test
    @DisplayName("IsActive: Should throw exception with userId in message when not found")
    void testIsActive_UserNotFound() {
        when(authIdentityRepository.findByUserId(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.isActive(userId);
        });

        assertTrue(exception.getMessage().contains(userId.toString()));
    }
}