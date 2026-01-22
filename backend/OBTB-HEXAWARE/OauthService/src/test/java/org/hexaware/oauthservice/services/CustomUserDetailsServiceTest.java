package org.hexaware.oauthservice.services;

import org.hexaware.oauthservice.entites.AuthIdentity;
import org.hexaware.oauthservice.entites.UserLockout;
import org.hexaware.oauthservice.repositories.AuthIdentityRepository;
import org.hexaware.oauthservice.repositories.UserLockOutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Mock
    private AuthIdentityRepository authIdentityRepository;

    @Mock
    private UserLockOutRepository userLockOutRepository;

    @Mock
    private UserRoleResolveService roleResolveService;

    private AuthIdentity mockIdentity;
    private UUID userId;
    private UUID roleMappingId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        roleMappingId = UUID.randomUUID();

        mockIdentity = new AuthIdentity();
        mockIdentity.setUserId(userId);
        mockIdentity.setUsername("johndoe");
        mockIdentity.setHashPassword("encrypted_pass");
        mockIdentity.setRoleMappingId(roleMappingId);
    }

    @Test
    @DisplayName("LoadUser: Should return valid UserDetails for an active, unlocked user")
    void loadUserByUsername_Success() {
        // Arrange
        when(authIdentityRepository.findByUsername("johndoe")).thenReturn(Optional.of(mockIdentity));
        when(userLockOutRepository.findByUserId(userId)).thenReturn(Optional.empty()); // No lockout record = unlocked
        when(roleResolveService.resolveRoles(roleMappingId)).thenReturn(List.of("ADMIN", "USER"));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("johndoe");

        // Assert
        assertNotNull(userDetails);
        assertEquals("johndoe", userDetails.getUsername());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("LoadUser: Should throw UsernameNotFoundException when user does not exist")
    void loadUserByUsername_UserNotFound() {
        when(authIdentityRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername("missing")
        );
    }

    @Test
    @DisplayName("LoadUser: Should set accountNonLocked to false when login counter >= 3 and isLocked is true")
    void loadUserByUsername_AccountLocked() {
        // Arrange
        when(authIdentityRepository.findByUsername("johndoe")).thenReturn(Optional.of(mockIdentity));

        UserLockout lockedInfo = new UserLockout();
        lockedInfo.setLoginCounter(3);
        lockedInfo.setLocked(true);
        when(userLockOutRepository.findByUserId(userId)).thenReturn(Optional.of(lockedInfo));

        when(roleResolveService.resolveRoles(roleMappingId)).thenReturn(List.of("USER"));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("johndoe");

        // Assert
        assertFalse(userDetails.isAccountNonLocked(), "Account should be locked");
    }

    @Test
    @DisplayName("LoadUser: Should return unlocked even with failed attempts if isLocked is false")
    void loadUserByUsername_ManyAttemptsButNotLocked() {
        // Arrange
        when(authIdentityRepository.findByUsername("johndoe")).thenReturn(Optional.of(mockIdentity));

        UserLockout lockoutInfo = new UserLockout();
        lockoutInfo.setLoginCounter(5);
        lockoutInfo.setLocked(false); // Counter is high, but admin hasn't set locked=true or logic hasn't triggered
        when(userLockOutRepository.findByUserId(userId)).thenReturn(Optional.of(lockoutInfo));

        when(roleResolveService.resolveRoles(roleMappingId)).thenReturn(List.of("USER"));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("johndoe");

        // Assert
        assertTrue(userDetails.isAccountNonLocked());
    }
}