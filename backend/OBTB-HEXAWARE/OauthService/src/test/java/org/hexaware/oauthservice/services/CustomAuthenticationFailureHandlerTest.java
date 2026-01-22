package org.hexaware.oauthservice.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomAuthenticationFailureHandlerTest {

    @InjectMocks
    private CustomAuthenticationFailureHandler failureHandler;

    @Mock
    private UserLockOutRepository userLockOutRepository;

    @Mock
    private AuthIdentityRepository authIdentityRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException authException;

    private StringWriter responseWriter;
    private AuthIdentity mockIdentity;
    private UUID userId;

    @BeforeEach
    void setUp() throws IOException {
        userId = UUID.randomUUID();
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        mockIdentity = new AuthIdentity();
        mockIdentity.setUserId(userId);
        mockIdentity.setUsername("testuser");
        mockIdentity.set_Verified(true);
        mockIdentity.set_Active(true);
    }

    @Test
    @DisplayName("Failure: Missing username should return 400 Bad Request")
    void onAuthenticationFailure_MissingUsername() throws IOException {
        when(request.getParameter("username")).thenReturn(null);

        failureHandler.onAuthenticationFailure(request, response, authException);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(responseWriter.toString().contains("Username or password missing"));
    }

    @Test
    @DisplayName("Failure: User not found should return 401 Unauthorized")
    void onAuthenticationFailure_UserNotFound() throws IOException {
        when(request.getParameter("username")).thenReturn("unknown");
        when(authIdentityRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        failureHandler.onAuthenticationFailure(request, response, authException);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(responseWriter.toString().contains("Invalid username or password"));
    }

    @Test
    @DisplayName("Failure: Unverified account should return 403 Forbidden")
    void onAuthenticationFailure_Unverified() throws IOException {
        mockIdentity.set_Verified(false);
        when(request.getParameter("username")).thenReturn("testuser");
        when(authIdentityRepository.findByUsername("testuser")).thenReturn(Optional.of(mockIdentity));

        failureHandler.onAuthenticationFailure(request, response, authException);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        assertTrue(responseWriter.toString().contains("verify your account"));
    }

    @Test
    @DisplayName("Lockout Logic: Second failed attempt should increment counter and set Attempt-2")
    void onAuthenticationFailure_IncrementLockout() throws IOException {
        when(request.getParameter("username")).thenReturn("testuser");
        when(authIdentityRepository.findByUsername("testuser")).thenReturn(Optional.of(mockIdentity));

        UserLockout lockout = new UserLockout();
        lockout.setUserId(userId);
        lockout.setLoginCounter(1); // Already failed once
        lockout.setLocked(false);

        when(userLockOutRepository.findByUserId(userId)).thenReturn(Optional.of(lockout));

        failureHandler.onAuthenticationFailure(request, response, authException);

        assertEquals(2, lockout.getLoginCounter());
        assertNotNull(lockout.getAttempt2());
        assertFalse(lockout.isLocked());
        verify(userLockOutRepository).save(lockout);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("Lockout Logic: Third failed attempt should lock the account")
    void onAuthenticationFailure_FinalLockout() throws IOException {
        when(request.getParameter("username")).thenReturn("testuser");
        when(authIdentityRepository.findByUsername("testuser")).thenReturn(Optional.of(mockIdentity));

        UserLockout lockout = new UserLockout();
        lockout.setUserId(userId);
        lockout.setLoginCounter(2); // Has 2 attempts, this is the 3rd
        lockout.setLocked(false);

        when(userLockOutRepository.findByUserId(userId)).thenReturn(Optional.of(lockout));

        failureHandler.onAuthenticationFailure(request, response, authException);

        assertEquals(3, lockout.getLoginCounter());
        assertTrue(lockout.isLocked());
        assertNotNull(lockout.getAttempt3());
        verify(userLockOutRepository).save(lockout);
    }
}