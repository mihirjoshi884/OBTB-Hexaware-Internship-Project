package org.hexaware.notificationservice.services;

import org.hexaware.notificationservice.queue.PendingQueue;

import java.io.UnsupportedEncodingException;

public interface TopicHandler {

    String getTopicKey();
    void handle(PendingQueue.PendingEventData data) throws UnsupportedEncodingException;
}
