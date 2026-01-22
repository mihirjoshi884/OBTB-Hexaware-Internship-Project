package org.hexaware.userservice.controllers;


import org.hexaware.userservice.controller.UserController;
import org.hexaware.userservice.dtos.UserCreationRequest;
import org.hexaware.userservice.dtos.UserSummary;
import org.hexaware.userservice.services.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class
})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean // Required for Spring Boot 3.4+ and 4.0
    private UserServiceImpl userServiceImpl;

    @Autowired
    private ObjectMapper objectMapper;

    private UserSummary mockSummary;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockSummary = new UserSummary();
        mockSummary.setUserId(userId);
        mockSummary.setUsername("johndoe");
        mockSummary.setEmail("john@email.com");
    }

    @Test
    @WithMockUser
    @DisplayName("POST /create-user: Should return 200 when all mandatory fields are provided")
    void testCreateUser() throws Exception {
        // 1. Arrange - Fill ALL fields that have @NotBlank or @NotNull in your DTO
        UserCreationRequest request = new UserCreationRequest();
        request.setUsername("johndoe");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setContact("1234567890");
        request.setRoleName("CUSTOMER");

        // Stub the service
        when(userServiceImpl.addUser(any(UserCreationRequest.class))).thenReturn(mockSummary);

        // 2. Act & Assert
        mockMvc.perform(post("/user-api/v1/create-user")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request))) // Now the JSON body is complete
                .andExpect(status().isOk()) // This will now pass
                .andExpect(jsonPath("$.username").value("johndoe"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /user/{userId}: Should return 200 and user details")
    void testGetUser() throws Exception {
        when(userServiceImpl.getUser(any(UUID.class))).thenReturn(mockSummary);

        mockMvc.perform(get("/user-api/v1/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /hello-world: Should return plain text")
    void testHelloWorld() throws Exception {
        mockMvc.perform(get("/user-api/v1/hello-world"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello World"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /user-info/email/{userId}: Should return email as string")
    void testGetUserEmail() throws Exception {
        when(userServiceImpl.getEmail(userId)).thenReturn("john@email.com");

        mockMvc.perform(get("/user-api/v1/user-info/email/" + userId))
                .andExpect(status().isOk())
                .andExpect(content().string("john@email.com"));
    }
}
