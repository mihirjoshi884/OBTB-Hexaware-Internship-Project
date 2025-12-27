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


@Service
public class TopicConsumers {

    private static final Logger log = LoggerFactory.getLogger(TopicConsumers.class);

    private final PendingQueue eventQueue;

    // CORRECTION: Use @Autowired for field injection, and remove 'final' if not using constructor injection.
    @Autowired
    private UserCreationTopicHandler userCreationHandler; // <-- No 'final' keyword, Spring will inject this field.

    // CONSTRUCTOR: Only inject the remaining 'final' fields (in this case, eventQueue).
    public TopicConsumers(PendingQueue eventQueue) {
        this.eventQueue = eventQueue;
        // userCreationHandler is injected directly by Spring after the constructor runs.
    }

    // IMMEDIATE PROCESSING FOR URGENT TOPICS
    @SneakyThrows
    @KafkaListener(topicPattern = "${kafka.topic.urgent-pattern}", groupId = "notification-urgent-immediate-group")
    public void consumeUrgentTopics(ConsumerRecord<String, NotificationEvent> record) {
        log.info("IMMEDIATE CONSUMER: Received urgent message from topic: {}", record.topic());

        PendingQueue.PendingEventData eventData = new PendingQueue.PendingEventData(
                record.topic(),
                record.value()
        );

        // Immediate processing here.
        userCreationHandler.handle(eventData);

        log.info("IMMEDIATE CONSUMER: Finished processing urgent message.");
    }

    // DELAYED/BATCH PROCESSING FOR LAZY TOPICS (Remains unchanged)
    @KafkaListener(topicPattern = "${kafka.topic.lazy-pattern}")
    public void consumeLazyTopics(ConsumerRecord<String, NotificationEvent> record) {

        PendingQueue.PendingEventData eventData = new PendingQueue.PendingEventData(
                record.topic(),
                record.value()
        );
        eventQueue.pushLazy(eventData);
    }
}