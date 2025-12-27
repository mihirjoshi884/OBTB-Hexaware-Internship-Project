package org.hexaware.oauthservice.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.List;

@Configuration
@EnableConfigurationProperties(KafkaTopicProperties.class)
public class KafkaTopicConfig {

    @Bean
    public List<NewTopic> createTopics(KafkaTopicProperties kafkaTopicProperties) {
        return kafkaTopicProperties.getTopics().stream()
                .map(topic -> TopicBuilder.name(topic.getTopic())
                        .partitions(topic.getPartitions())
                        .replicas(topic.getReplication()).build())
                .toList();
    }
}
