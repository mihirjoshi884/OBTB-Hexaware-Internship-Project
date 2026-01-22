package org.hexaware.notificationservice.services;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.hexaware.notificationservice.dtos.NotificationEvent;
import org.hexaware.notificationservice.dtos.Summary;
import org.hexaware.notificationservice.handlers.UserCreationTopicHandler;
import org.hexaware.notificationservice.queue.PendingQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TopicConsumersTest {

    private TopicConsumers topicConsumers;

    @Mock
    private PendingQueue eventQueue;

    @Mock
    private UserCreationTopicHandler userCreationHandler;

    @BeforeEach
    void setUp() {
        // 1. Tell the mock what its key is.
        // This is vital because the TopicConsumers constructor iterates through
        // the list to build the internal Map.
        when(userCreationHandler.getTopicKey()).thenReturn("user_created");

        // 2. Pass the list and the queue to the new constructor
        // OLD: topicConsumers = new TopicConsumers(eventQueue);
        // NEW:
        topicConsumers = new TopicConsumers(java.util.List.of(userCreationHandler), eventQueue);

        // 3. Remove ReflectionTestUtils.setField(...)
        // It is no longer needed because the handler is now injected via constructor.
    }

    @Test
    @DisplayName("Urgent Consumer: Should call specific handler based on topic key")
    void testConsumeUrgentTopics_ImmediateProcessing() throws Exception {
        // Arrange
        NotificationEvent<Summary> event = new NotificationEvent<>();
        // The topic name segment at index 2 matches our "user_created" key
        ConsumerRecord<String, NotificationEvent<?>> record =
                new ConsumerRecord<>("producer_oauth.urgent.user_created", 0, 0L, "key", event);

        // Act
        topicConsumers.consumeUrgentTopics(record);

        // Assert
        ArgumentCaptor<PendingQueue.PendingEventData> captor = ArgumentCaptor.forClass(PendingQueue.PendingEventData.class);
        verify(userCreationHandler, times(1)).handle(captor.capture());

        assertEquals("producer_oauth.urgent.user_created", captor.getValue().topicName());
        assertEquals(event, captor.getValue().payload());
        verifyNoInteractions(eventQueue);
    }
}