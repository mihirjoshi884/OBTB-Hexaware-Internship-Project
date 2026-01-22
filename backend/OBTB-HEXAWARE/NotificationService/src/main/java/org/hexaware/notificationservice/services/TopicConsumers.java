package org.hexaware.notificationservice.services;

import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.hexaware.notificationservice.dtos.NotificationEvent;
import org.hexaware.notificationservice.handlers.UserCreationTopicHandler;
import org.hexaware.notificationservice.queue.PendingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; // <-- Keep this import
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class TopicConsumers {
    private static final Logger log = LoggerFactory.getLogger(TopicConsumers.class);
    private final Map<String, TopicHandler> handlers;
    // 1. ADD THIS FIELD (This was missing, causing the "cannot find symbol" error)
    private final PendingQueue eventQueue;

    // Spring automatically injects all beans implementing TopicHandler into this map
    // The key is the bean name, so we convert it to use your getTopicKey() instead
    public TopicConsumers(java.util.List<TopicHandler> topicHandlers, PendingQueue eventQueue) {
        this.eventQueue = eventQueue;
        // Build the map for dynamic routing
        this.handlers = topicHandlers.stream()
                .collect(Collectors.toMap(TopicHandler::getTopicKey, java.util.function.Function.identity()));
    }

    @SneakyThrows
    @KafkaListener(topicPattern = "${kafka.topic.urgent-pattern}", groupId = "notification-urgent-immediate-group")
    public void consumeUrgentTopics(ConsumerRecord<String, NotificationEvent<?>> record) {
        log.info("IMMEDIATE CONSUMER: Received urgent message from topic: {}", record.topic());

        // 1. Extract the key using your existing logic from NotificationProcessor
        String topicKey = extractTopicKey(record.topic());

        // 2. Look up the correct handler (e.g., "user_created", "password_change", or "account_recovery")
        TopicHandler handler = handlers.get(topicKey);

        if (handler != null) {
            PendingQueue.PendingEventData eventData = new PendingQueue.PendingEventData(
                    record.topic(),
                    record.value()
            );
            // 3. Delegate to the specific handler
            handler.handle(eventData);
            log.info("IMMEDIATE CONSUMER: Successfully processed {} using {}", record.topic(), handler.getClass().getSimpleName());
        } else {
            log.warn("IMMEDIATE CONSUMER: No handler found for topic key: {}", topicKey);
        }
    }

    private String extractTopicKey(String topicName) {
        try {
            String[] parts = topicName.split("\\.");
            return (parts.length >= 3) ? parts[2] : parts[parts.length - 1];
        } catch (Exception e) {
            log.error("Failed to parse topic: {}", topicName);
            return null;
        }
    }
}