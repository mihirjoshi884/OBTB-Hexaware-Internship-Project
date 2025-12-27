package org.hexaware.notificationservice.services;


import org.hexaware.notificationservice.queue.PendingQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationProcessorTest {

    private NotificationProcessor notificationProcessor;

    @Mock
    private PendingQueue eventQueue;

    @Mock
    private TopicHandler mockUserHandler;

    @BeforeEach
    void setUp() {
        // Prepare the handler mock
        when(mockUserHandler.getTopicKey()).thenReturn("user");

        // Initialize processor with the mock handler list
        notificationProcessor = new NotificationProcessor(eventQueue, List.of(mockUserHandler));
    }

    @Test
    @DisplayName("Process Lazy Batch: Should successfully extract key and delegate to handler")
    void testProcessLazyBatch_Success() throws UnsupportedEncodingException {
        // Arrange
        String topicName = "producer_oauth.urgent.user_created";
        PendingQueue.PendingEventData event = new PendingQueue.PendingEventData(topicName, "some-payload");

        when(eventQueue.pullLazyBatch(anyInt())).thenReturn(List.of(event));

        // Act
        notificationProcessor.processLazyBatch();

        // Assert
        // Verify key extraction: "producer_oauth.urgent.user_created" -> "user"
        verify(mockUserHandler, times(1)).handle(event);
    }

    @Test
    @DisplayName("Process Lazy Batch: Should skip processing if batch is empty")
    void testProcessLazyBatch_EmptyBatch() throws UnsupportedEncodingException {
        // Arrange
        when(eventQueue.pullLazyBatch(anyInt())).thenReturn(List.of());

        // Act
        notificationProcessor.processLazyBatch();

        // Assert
        // This verifies that 'handle' was never called.
        // We don't use verifyNoInteractions because getTopicKey() was called in constructor.
        verify(mockUserHandler, never()).handle(any());
    }

    @Test
    @DisplayName("Topic Key Extraction: Should handle malformed topic names gracefully")
    void testProcessLazyBatch_MalformedTopic() throws UnsupportedEncodingException {
        // Arrange
        String badTopic = "invalidTopicFormat"; // Will cause ArrayIndexOutOfBoundsException
        PendingQueue.PendingEventData event = new PendingQueue.PendingEventData(badTopic, "data");

        when(eventQueue.pullLazyBatch(anyInt())).thenReturn(List.of(event));

        // Act
        notificationProcessor.processLazyBatch();

        // Assert
        // Logic should log error and continue without calling any handler
        verify(mockUserHandler, never()).handle(any());
    }

    @Test
    @DisplayName("Handler Mapping: Should warn and skip if no handler matches topic key")
    void testProcessLazyBatch_NoHandlerFound() throws UnsupportedEncodingException {
        // Arrange
        String topicName = "producer_oauth.urgent.order_created"; // Key is "order", no handler for this
        PendingQueue.PendingEventData event = new PendingQueue.PendingEventData(topicName, "data");

        when(eventQueue.pullLazyBatch(anyInt())).thenReturn(List.of(event));

        // Act
        notificationProcessor.processLazyBatch();

        // Assert
        verify(mockUserHandler, never()).handle(any());
    }
}