package org.hexaware.notificationservice.handlers;

import org.hexaware.notificationservice.dtos.NotificationEvent;
import org.hexaware.notificationservice.dtos.Summary;
import org.hexaware.notificationservice.queue.PendingQueue;
import org.hexaware.notificationservice.services.EmailSenderService;
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

import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCreationTopicHandlerTest {

    @InjectMocks
    private UserCreationTopicHandler topicHandler;

    @Mock
    private EmailSenderService emailSenderService;

    @Mock
    private WebClient notificationWebClient;

    // WebClient fluent API mocks
    @Mock private WebClient.RequestHeadersUriSpec getUriSpec;
    @Mock private WebClient.RequestHeadersSpec getHeadersSpec;
    @Mock private WebClient.ResponseSpec responseSpec;

    private UUID testUserId;
    private PendingQueue.PendingEventData mockEventData;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        // Prepare Mock Data
        Summary summary = new Summary();
        summary.setUserId(testUserId);

        // FIX: Specify <Summary> instead of <?>
        NotificationEvent<Summary> event = new NotificationEvent<>();
        event.setData(summary);

        mockEventData = new PendingQueue.PendingEventData("user-topic", event);

        // Inject the mock WebClient manually
        ReflectionTestUtils.setField(topicHandler, "notificationWebClient", notificationWebClient);
    }

    @Test
    @DisplayName("Handle Topic: Should fetch email and send welcome notification")
    void handle_Success() throws Exception {
        // Arrange: Mock WebClient chain
        when(notificationWebClient.get()).thenReturn(getUriSpec);
        when(getUriSpec.uri(any(Function.class))).thenReturn(getHeadersSpec);
        when(getHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("user@example.com"));

        // Act
        topicHandler.handle(mockEventData);

        // Assert: Verify the email was sent with correct details
        verify(emailSenderService).sendEmail(
                eq("user@example.com"),
                contains("Welcome to FastX!"),
                argThat(body -> body.contains(testUserId.toString()) && body.contains("verify-account"))
        );
    }

    @Test
    @DisplayName("Topic Key: Should return 'user'")
    void getTopicKey_ReturnsUser() {
        assertEquals("user", topicHandler.getTopicKey());
    }
}