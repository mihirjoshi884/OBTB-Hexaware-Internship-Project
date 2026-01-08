package org.hexaware.notificationservice.configs;

import org.hexaware.notificationservice.queue.PendingQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationConfig {

    @Bean
    public PendingQueue getEventQueue() {
        return new PendingQueue();
    }
}
