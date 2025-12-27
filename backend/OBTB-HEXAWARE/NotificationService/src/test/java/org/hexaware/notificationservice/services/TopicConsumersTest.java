package org.hexaware.notificationservice.services;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.hexaware.notificationservice.dtos.NotificationEvent;
import org.hexaware.notificationservice.handlers.UserCreationTopicHandler;
import org.hexaware.notificationservice.queue.PendingQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
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
        // Initialize the class with constructor-injected fields
        topicConsumers = new TopicConsumers(eventQueue);

        // Manually inject the @Autowired field
        ReflectionTestUtils.setField(topicConsumers, "userCreationHandler", userCreationHandler);
    }

    @Test
    @DisplayName("Urgent Consumer: Should call handler immediately")
    void testConsumeUrgentTopics_ImmediateProcessing() throws Exception {
        // Arrange
        NotificationEvent event = new NotificationEvent();
        ConsumerRecord<String, NotificationEvent> record =
                new ConsumerRecord<>("producer_oauth.urgent.user_created", 0, 0L, "key", event);

        // Act
        topicConsumers.consumeUrgentTopics(record);

        // Assert
        verify(userCreationHandler, times(1)).handle(argThat(data ->
                data.topicName().equals("producer_oauth.urgent.user_created") &&
                        data.payload().equals(event)
        ));
        // Verify it was NOT pushed to any queue (it was processed immediately)
        verifyNoInteractions(eventQueue);
    }

    @Test
    @DisplayName("Lazy Consumer: Should push to lazy queue and NOT call handler")
    void testConsumeLazyTopics_Queueing() {
        // Arrange
        NotificationEvent event = new NotificationEvent();
        ConsumerRecord<String, NotificationEvent> record =
                new ConsumerRecord<>("producer_oauth.lazy.some_event", 0, 0L, "key", event);

        // Act
        topicConsumers.consumeLazyTopics(record);

        // Assert
        verify(eventQueue, times(1)).pushLazy(argThat(data ->
                data.topicName().equals("producer_oauth.lazy.some_event") &&
                        data.payload().equals(event)
        ));
        // Verify handler was NOT called
        verifyNoInteractions(userCreationHandler);
    }
}