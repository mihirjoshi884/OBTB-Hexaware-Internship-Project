package org.hexaware.notificationservice.queue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PendingQueueTest {

    private PendingQueue pendingQueue;

    @BeforeEach
    void setUp() {
        pendingQueue = new PendingQueue();
    }

    @Test
    @DisplayName("Push Urgent: Should successfully add data to the urgent queue")
    void testPushUrgent() {
        PendingQueue.PendingEventData data = new PendingQueue.PendingEventData("user-topic", "test-payload");

        boolean result = pendingQueue.pushUrgent(data);

        assertTrue(result, "Data should be successfully offered to the queue");
    }

    @Test
    @DisplayName("Push Lazy: Should successfully add data to the lazy queue")
    void testPushLazy() {
        PendingQueue.PendingEventData data = new PendingQueue.PendingEventData("log-topic", "lazy-payload");

        boolean result = pendingQueue.pushLazy(data);

        assertTrue(result, "Data should be successfully offered to the queue");
    }

    @Test
    @DisplayName("Pull Urgent Batch: Should retrieve specified number of items")
    void testPullUrgentBatch() {
        // Arrange: Push 5 items
        for (int i = 0; i < 5; i++) {
            pendingQueue.pushUrgent(new PendingQueue.PendingEventData("topic", "data-" + i));
        }

        // Act: Pull a batch of 3
        List<PendingQueue.PendingEventData> batch = pendingQueue.pullUrgentBatch(3);

        // Assert
        assertEquals(3, batch.size(), "Batch size should match requested size");
        assertEquals("data-0", batch.get(0).payload());

        // Check remaining items
        List<PendingQueue.PendingEventData> remaining = pendingQueue.pullUrgentBatch(10);
        assertEquals(2, remaining.size(), "Remaining items should be 2");
    }

    @Test
    @DisplayName("Pull Lazy Batch: Should handle requests larger than current queue size")
    void testPullLazyBatch_Partial() {
        // Arrange: Push only 2 items
        pendingQueue.pushLazy(new PendingQueue.PendingEventData("topic", "payload-1"));
        pendingQueue.pushLazy(new PendingQueue.PendingEventData("topic", "payload-2"));

        // Act: Try to pull a batch of 10
        List<PendingQueue.PendingEventData> batch = pendingQueue.pullLazyBatch(10);

        // Assert
        assertEquals(2, batch.size(), "Should return only available items if queue is smaller than batch size");
        assertTrue(pendingQueue.pullLazyBatch(1).isEmpty(), "Queue should be empty after drain");
    }

    @Test
    @DisplayName("Pull Batch: Should return empty list when queue is empty")
    void testPullEmptyQueue() {
        List<PendingQueue.PendingEventData> result = pendingQueue.pullUrgentBatch(5);

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Pulling from an empty queue should return an empty list, not null");
    }
}