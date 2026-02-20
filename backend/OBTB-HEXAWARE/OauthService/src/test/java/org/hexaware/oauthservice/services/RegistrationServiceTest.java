package org.hexaware.oauthservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.hexaware.oauthservice.dtos.*;
import org.hexaware.oauthservice.entites.AuthIdentity;
import org.hexaware.oauthservice.entites.SecurityQuestion;
import org.hexaware.oauthservice.entites.UserLockout;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTest {

    @InjectMocks
    private RegistrationService registrationService;

    @Mock
    private AuthIdentityRepository authIdentityRepository;

    @Mock
    private SecurityQuestionRepository secRepository;

    @Mock
    private UserLockOutRepository userLockOutRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    // We need to mock the fluent API of WebClient
    @Mock
    private WebClient userServieWebClient;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    private AuthUserCreationRequest request;
    private UserSummary userSummary;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        request = new AuthUserCreationRequest();
        request.setUsername("alice");
        request.setPassword("password123");

        // Fix: Create actual DTO objects instead of HashMaps
        List<SecurityAnswerDTO> secQA = new ArrayList<>();

        SecurityAnswerDTO q1 = new SecurityAnswerDTO();
        q1.setQuestion("City"); // Adjust setter name based on your DTO fields
        q1.setAnswer("Pune");

        SecurityAnswerDTO q2 = new SecurityAnswerDTO();
        q2.setQuestion("Pet");
        q2.setAnswer("Bruno");

        secQA.add(q1);
        secQA.add(q2);

        request.setSecQA(secQA); // This will now compile!

        userSummary = new UserSummary();
        userSummary.setUserId(userId);
        userSummary.setUsername("alice");
    }
    @Test
    @DisplayName("Registration: Should successfully complete all 4 steps and send Kafka message")
    void userRegistration_Success() throws Exception {
        // 1. Mock WebClient (User Service Call)
        setupWebClientMock(userSummary);

        // 2. Mock Password Encoding
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_pass");

        // 3. Mock Repositories
        when(secRepository.save(any(SecurityQuestion.class))).thenReturn(new SecurityQuestion());
        when(userLockOutRepository.save(any(UserLockout.class))).thenReturn(new UserLockout());

        AuthIdentity savedAuth = new AuthIdentity();
        savedAuth.setUserId(userId);
        savedAuth.setUsername("alice");
        when(authIdentityRepository.save(any(AuthIdentity.class))).thenReturn(savedAuth);

        // 4. Mock Kafka/JSON
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // Act
        Summary result = registrationService.userRegistration(request);

        // Assert
        assertNotNull(result);
        assertEquals("alice", result.getUsername());

        // Verify cross-service and DB interactions
        verify(userLockOutRepository).save(argThat(l -> l.getLoginCounter() == 0));
        verify(authIdentityRepository).save(argThat(a -> !a.is_Active()));
        verify(kafkaTemplate).send(eq("producer_oauth.urgent.user_created"), anyString());
    }

    @Test
    @DisplayName("Registration: Should throw Exception if User Service returns mismatched username")
    void userRegistration_MismatchedUsername() {
        UserSummary mismatchedUser = new UserSummary();
        mismatchedUser.setUsername("wrong_name");
        setupWebClientMock(mismatchedUser);

        assertThrows(RuntimeException.class, () -> registrationService.userRegistration(request));
    }

    @Test
    @DisplayName("Registration: Should fail if less than two security questions are provided")
    void userRegistration_SecurityQuestionFailure() {
        setupWebClientMock(userSummary);
        request.getSecQA().remove(1); // Leave only one question

        assertThrows(RuntimeException.class, () -> registrationService.userRegistration(request));
    }

    /**
     * Helper to mock the complex fluent chain of WebClient
     */
    private void setupWebClientMock(UserSummary response) {
        when(userServieWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(UserSummary.class)).thenReturn(Mono.just(userSummary));
    }
}