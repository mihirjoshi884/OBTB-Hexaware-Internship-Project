package org.hexaware.notificationservice.queue;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class PendingQueue {

    public record PendingEventData(String topicName, Object payload){}

    private final BlockingQueue<PendingEventData> urgentQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<PendingEventData> lazyQueue = new LinkedBlockingQueue<>();

    public boolean pushUrgent(PendingEventData data) {
        return urgentQueue.offer(data);
    }
    public boolean pushLazy(PendingEventData data) {
        return lazyQueue.offer(data);
    }
    public List<PendingEventData> pullUrgentBatch(int batchSize) {
        return pullBatchFromQueue(urgentQueue, batchSize);
    }

    public List<PendingEventData> pullLazyBatch(int batchSize) {
        return pullBatchFromQueue(lazyQueue, batchSize);
    }

    private List<PendingEventData> pullBatchFromQueue(BlockingQueue<PendingEventData> queue, int batchSize) {
        List<PendingEventData> batch = new ArrayList<>(batchSize);
        queue.drainTo(batch, batchSize);
        return batch;
    }
}
