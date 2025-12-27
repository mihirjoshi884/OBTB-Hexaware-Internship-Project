package org.hexaware.oauthservice.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserRoleResolveServiceTest {

    @InjectMocks
    private UserRoleResolveService roleResolveService;

    @Mock
    private WebClient webClientMock;

    // Mocks for Step 1 (POST /oauth2/token)
    @Mock private WebClient.RequestBodyUriSpec postUriSpec;
    @Mock private WebClient.RequestBodySpec postBodySpec;
    @Mock private WebClient.RequestHeadersSpec postHeadersSpec;
    @Mock private WebClient.ResponseSpec postResponseSpec;

    // Mocks for Step 2 (GET /user-role/)
    @Mock private WebClient.RequestHeadersUriSpec getUriSpec;
    @Mock private WebClient.RequestHeadersSpec getHeadersSpec;
    @Mock private WebClient.ResponseSpec getResponseSpec;

    @BeforeEach
    void setUp() {
        // Force the mock into the private final field
        ReflectionTestUtils.setField(roleResolveService, "webClient", webClientMock);

        // Set the @Value fields so they aren't null
        ReflectionTestUtils.setField(roleResolveService, "clientId", "test-client");
        ReflectionTestUtils.setField(roleResolveService, "clientSecret", "test-secret");
        ReflectionTestUtils.setField(roleResolveService, "authBaseUrl", "http://auth");
        ReflectionTestUtils.setField(roleResolveService, "userServiceBaseUrl", "http://role");
    }

    @Test
    @DisplayName("Resolve Roles: Should parse JSON array roles correctly")
    void testResolveRoles_JsonArray() {
        UUID roleMappingId = UUID.randomUUID();

        // 1. Mock the Token POST call chain
        when(webClientMock.post()).thenReturn(postUriSpec);
        when(postUriSpec.uri(anyString())).thenReturn(postBodySpec);
        when(postBodySpec.header(anyString(), any())).thenReturn(postBodySpec);
        when(postBodySpec.bodyValue(any())).thenReturn(postHeadersSpec);
        when(postHeadersSpec.retrieve()).thenReturn(postResponseSpec);
        when(postResponseSpec.bodyToMono(String.class)).thenReturn(Mono.just("{\"access_token\":\"abc-123\"}"));

        // 2. Mock the Role GET call chain
        // Note: Your service casts this to RequestHeadersSpec
        when(webClientMock.get()).thenReturn(getUriSpec);
        when(getUriSpec.uri(anyString())).thenReturn(getHeadersSpec);
        when(getHeadersSpec.header(anyString(), any())).thenReturn(getHeadersSpec);
        when(getHeadersSpec.retrieve()).thenReturn(getResponseSpec);
        when(getResponseSpec.bodyToMono(String.class)).thenReturn(Mono.just("[\"ADMIN\", \"USER\"]"));

        // Act
        List<String> roles = roleResolveService.resolveRoles(roleMappingId);

        // Assert
        assertEquals(2, roles.size());
        assertTrue(roles.contains("ADMIN"));
        assertTrue(roles.contains("USER"));
    }

    @Test
    @DisplayName("Resolve Roles: Should handle plain string fallback")
    void testResolveRoles_PlainString() {
        // Mock Step 1
        when(webClientMock.post()).thenReturn(postUriSpec);
        when(postUriSpec.uri(anyString())).thenReturn(postBodySpec);
        when(postBodySpec.header(anyString(), any())).thenReturn(postBodySpec);
        when(postBodySpec.bodyValue(any())).thenReturn(postHeadersSpec);
        when(postHeadersSpec.retrieve()).thenReturn(postResponseSpec);
        when(postResponseSpec.bodyToMono(String.class)).thenReturn(Mono.just("{\"access_token\":\"abc\"}"));

        // Mock Step 2 (Return plain string instead of JSON)
        when(webClientMock.get()).thenReturn(getUriSpec);
        when(getUriSpec.uri(anyString())).thenReturn(getHeadersSpec);
        when(getHeadersSpec.header(anyString(), any())).thenReturn(getHeadersSpec);
        when(getHeadersSpec.retrieve()).thenReturn(getResponseSpec);
        when(getResponseSpec.bodyToMono(String.class)).thenReturn(Mono.just("CUSTOMER"));

        // Act
        List<String> roles = roleResolveService.resolveRoles(UUID.randomUUID());

        // Assert
        assertEquals(1, roles.size());
        assertEquals("CUSTOMER", roles.get(0));
    }
}