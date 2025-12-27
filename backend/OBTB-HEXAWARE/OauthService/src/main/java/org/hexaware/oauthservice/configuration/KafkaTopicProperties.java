package org.hexaware.oauthservice.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "kafka")
@Component @Data
public class KafkaTopicProperties {

    private List<TopicConfig> topics =  new ArrayList<>();
    @Data
    public static class TopicConfig {
        private String topic;
        private Integer partitions;
        private Integer replication;
    }

}
