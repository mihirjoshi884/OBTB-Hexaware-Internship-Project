package org.hexaware.notificationservice.services;

import org.hexaware.notificationservice.queue.PendingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NotificationProcessor {

    private final PendingQueue eventQueue;
    private final Map<String, TopicHandler> handlers;
    private static final Logger log = LoggerFactory.getLogger(NotificationProcessor.class);

    // Removed URGENT_BATCH_SIZE as it's no longer used for scheduled processing.
    private static final int LAZY_BATCH_SIZE = 500;

    // Injects the single PendingQueue bean and a list of ALL TopicHandler beans.
    public NotificationProcessor(PendingQueue eventQueue, List<TopicHandler> topicHandlers) {
        this.eventQueue = eventQueue;
        // Convert the list of handlers into a Map for O(1) lookup
        this.handlers = topicHandlers.stream()
                .collect(Collectors.toMap(TopicHandler::getTopicKey, Function.identity()));

        log.info("Initialized NotificationProcessor with {} Topic Handlers: {}",
                handlers.size(), handlers.keySet());
    }

    /**
     * Extracts the entity key (e.g., "user") from the full topic name
     * (e.g., "producer_oauth.urgent.user_created")
     */
    private String extractTopicKey(String topicName) {
        try {
            String[] parts = topicName.split("\\.");
            // Simply return the 3rd part to match "account_recovery" or "password_change"
            return parts[2];
        } catch (Exception e) {
            log.error("Failed to parse topic name: {}", topicName);
            return null;
        }
    }


    @Scheduled(fixedRateString = "${notification.lazy.interval.ms:300000}") // 5 minutes
    public void processLazyBatch() throws UnsupportedEncodingException {
        List<PendingQueue.PendingEventData> batch = eventQueue.pullLazyBatch(LAZY_BATCH_SIZE);
        if (batch.isEmpty()) return;

        log.info("LAZY PROCESSOR: Processing batch of {} events.", batch.size());

        for (PendingQueue.PendingEventData event : batch) {
            String topicKey = extractTopicKey(event.topicName());
            TopicHandler handler = handlers.get(topicKey);

            if (handler != null) {
                // Delegation
                handler.handle(event);
            } else {
                log.warn("No handler found for LAZY topic key: {}. Topic: {}", topicKey, event.topicName());
            }
        }
    }
}