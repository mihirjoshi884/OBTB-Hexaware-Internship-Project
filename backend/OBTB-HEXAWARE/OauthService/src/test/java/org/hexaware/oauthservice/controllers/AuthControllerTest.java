package org.hexaware.oauthservice.controllers;



import com.fasterxml.jackson.databind.ObjectMapper;
import org.hexaware.oauthservice.controller.AuthController;
import org.hexaware.oauthservice.dtos.AuthUserCreationRequest;
import org.hexaware.oauthservice.dtos.UserStatusResponse;
import org.hexaware.oauthservice.entites.PrincipleUser;
import org.hexaware.oauthservice.services.AuthService;
import org.hexaware.oauthservice.services.RegistrationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrationService registrationService;

    @MockitoBean
    private AuthService authService;


    private final ObjectMapper objectMapper = new ObjectMapper();



    @Test
    @WithMockUser
    @DisplayName("POST /register: Success")
    void testRegisterUser() throws Exception {
        AuthUserCreationRequest request = new AuthUserCreationRequest();
        request.setUsername("testuser");

        when(registrationService.userRegistration(any())).thenReturn(null);

        mockMvc.perform(post("/auth-api/v1/register")
                        .with(csrf()) // ✅ Add this to satisfy Spring Security
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }



    @Test
    @WithMockUser
    @DisplayName("GET /user/is-verified/{userId}: Success")
    void testIsVerified() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authService.isVerified(userId)).thenReturn(true);

        mockMvc.perform(get("/auth-api/v1/user/is-verified/" + userId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("GET /user/get-current-user: Success with custom Principal")
    void testGetCurrentUser() throws Exception {
        // We create a mock of your custom PrincipleUser
        PrincipleUser mockPrincipal = mock(PrincipleUser.class);
        when(mockPrincipal.getUsername()).thenReturn("john_doe");
        when(mockPrincipal.isEnabled()).thenReturn(true);
        when(mockPrincipal.isAccountNonLocked()).thenReturn(true);
        when(mockPrincipal.getAuthorities()).thenReturn((List) List.of(new SimpleGrantedAuthority("ROLE_USER")));

        mockMvc.perform(get("/auth-api/v1/user/get-current-user")
                        .with(authentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(mockPrincipal, null, mockPrincipal.getAuthorities()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john_doe"))
                .andExpect(jsonPath("$.enabled").value(true));
    }
}