package org.hexaware.oauthservice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hexaware.oauthservice.dtos.NotificationEvent;
import org.hexaware.oauthservice.dtos.ResponseDto;
import org.hexaware.oauthservice.dtos.SecurityInfoDto;
import org.hexaware.oauthservice.dtos.Summary;
import org.hexaware.oauthservice.entites.AuthIdentity;
import org.hexaware.oauthservice.entites.RecoveryBundle;
import org.hexaware.oauthservice.entites.SecurityQuestion;
import org.hexaware.oauthservice.exceptions.UserDoesNotExistException;
import org.hexaware.oauthservice.repositories.AuthIdentityRepository;
import org.hexaware.oauthservice.repositories.SecurityQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccountService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private AuthIdentityRepository authIdentityRepository;
    @Autowired
    private SecurityQuestionRepository securityQuestionRepository;
    @Autowired
    private KafkaAdmin kafkaAdmin;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @jakarta.annotation.PostConstruct
    public void setUp() {
        // This is the magic line that fixes your error
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
    }
    @Autowired
    private AuthService authService;

    public ResponseDto getSecurityInfo(String username) {
        return authIdentityRepository.findByUsername(username)
                .map(identity -> new ResponseDto(
                        new SecurityInfoDto(identity.getPasswordUpdatedAt(), identity.getLastLoginSuccess()),
                        200, "Security info retrieved successfully"
                ))
                .orElseThrow(() -> new UserDoesNotExistException("User not found with username: " + username));
    }


    public Map<String,String> publishAccountRecovery(String username, String email){

        System.out.println("Fetching from DATABASE for: " + username);
        var authIdentity = authIdentityRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var secQues = securityQuestionRepository.findByUserId(authIdentity.getUserId());
        List<String> secQuestions = new ArrayList<>();
        secQuestions.add(secQues.get().getQuestionAndAnswer1());
        secQuestions.add(secQues.get().getQuestionAndAnswer2());

        return publishTopic("producer_oauth.urgent.account_recovery",
                authIdentity,email);
    }

    private Map<String,String> publishTopic(String topic, AuthIdentity identity, String email) {
        try {
            Summary summary = new Summary();
            summary.setUsername(identity.getUsername());
            summary.setEmail(email);
            summary.setUserId(identity.getUserId());

            // 1. Create the Map that the Consumer expects (with "user" and "token")
            Map<String, Object> payload = new HashMap<>();
            payload.put("user", summary);
            payload.put("token", authService.generateRecoveryToken(summary.getUsername()));

            // 2. Wrap it in your NotificationEvent DTO
            NotificationEvent<Map<String, Object>> notificationEvent = new NotificationEvent<>();
            notificationEvent.setData(payload);
            notificationEvent.setRecipientIdentifier(summary.getUsername());
            notificationEvent.setTimestamp(java.time.Instant.now().toEpochMilli());

            // 3. Serialize and Send
            String message = objectMapper.writeValueAsString(notificationEvent);
            kafkaTemplate.send(topic, message);

            return Map.of("message", "Success");
        } catch (Exception e) {
            return Map.of("Failed","Error:"+e.getMessage());
        }
    }
}
