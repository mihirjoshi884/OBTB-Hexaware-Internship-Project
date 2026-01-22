package org.hexaware.oauthservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.hexaware.oauthservice.dtos.*;
import org.hexaware.oauthservice.entites.AuthIdentity;
import org.hexaware.oauthservice.entites.SecurityQuestion;
import org.hexaware.oauthservice.entites.UserLockout;
import org.hexaware.oauthservice.mappers.AuthMapper;
import org.hexaware.oauthservice.repositories.AuthIdentityRepository;
import org.hexaware.oauthservice.repositories.SecurityQuestionRepository;
import org.hexaware.oauthservice.repositories.UserLockOutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class RegistrationService {

    @Autowired
    private  KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private  KafkaAdmin kafkaAdmin;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AuthIdentityRepository authIdentityRepository;
    @Autowired
    private WebClient webClient;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SecurityQuestionRepository secRepository;
    @Autowired
    private UserLockOutRepository userLockOutRepository;
    @Value("${userservice.base-uri}")
    private String userServiceBaseUrl;
    @Autowired
    private AuthMapper mapper;

    public Summary userRegistration(AuthUserCreationRequest authUserCreationRequest) throws Exception {

        // 1. External Call: Create User in User Service
        UserCreationRequest userCreationRequest = mapper.toUserCreationRequest(authUserCreationRequest);
        UserSummary userResponse = createUser(userCreationRequest);

        // Guard Clause: Validate User Service Response
        // Check for null response or inconsistent data (usernames should match).
        if (!Objects.equals(userResponse.getUsername(), authUserCreationRequest.getUsername())) {

            // Throw an exception instead of returning null to signal failure
            // and trigger the transaction rollback.
            throw new RuntimeException("User creation failed or returned an inconsistent response from User Service.");
        }



        // 2. Prepare and Save Security Questions
        SecurityQuestion securityQuestionEntity = storeHashQuestion(
                authUserCreationRequest.getSecQA(),
                userResponse.getUserId()
        );
        secRepository.save(securityQuestionEntity);

        // 3. Prepare and Save User Lockout Record
        UserLockout userLockout = new UserLockout();
        userLockout.setLoginCounter(0);
        userLockout.setUserId(userResponse.getUserId());
        userLockout.setLocked(false);
        // Attempt fields (LocalDateTime) will be NULL by default, indicating no failures.
        userLockOutRepository.save(userLockout);

        // 4. Prepare and Save AuthIdentity Record
        AuthIdentity authIdentity = mapper.toAuthIdentity(authUserCreationRequest);


        // Hashed Password
        authIdentity.setHashPassword(createHashPassword(authUserCreationRequest.getPassword()));

        // Initial Activation State
        authIdentity.set_Active(false);
        authIdentity.setUserId(userResponse.getUserId());
        authIdentity.setRoleMappingId(userResponse.getRoleMappingId());

        AuthIdentity savedAuthIdentity = authIdentityRepository.save(authIdentity);
        var result = mapper.toSummary(savedAuthIdentity);
        String topic = "producer_oauth.urgent.user_created";
        NotificationEvent notificationEvent = new NotificationEvent();
        notificationEvent.setData(result);
        notificationEvent.setRecipientIdentifier(result.getUsername());
        long epochMilli = java.time.Instant.now().toEpochMilli();
        notificationEvent.setTimestamp(epochMilli);
        String message = objectMapper.writeValueAsString(notificationEvent);
        kafkaTemplate.send(topic, message);
        return result;
    }
    public String createHash(String input) throws Exception {
        // 1. Get the algorithm instance
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        // 2. Compute the hash (returns a fixed-length byte array)
        byte[] hashbytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

        // 3. Convert the byte array to a hexadecimal string for output
        StringBuilder hexString = new StringBuilder(2 * hashbytes.length);
        for (byte b : hashbytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString(); // Output will be 64 characters long
    }
    private SecurityQuestion storeHashQuestion(List<SecurityAnswerDTO> SecQA, UUID currentUserId) {
        List<String> hashedQAPairs = SecQA.stream()
                .map(dto -> {
                    // Now you have clear methods instead of map.get("question")
                    String q = dto.getQuestion().trim().toLowerCase();
                    String a = dto.getAnswer().trim().toLowerCase();

                    try {
                        return createHash(q + a);
                    } catch (Exception e) {
                        throw new RuntimeException("Hashing failed", e);
                    }
                })
                .collect(Collectors.toList());
        if (hashedQAPairs.size() < 2) {
            throw new IllegalStateException("Expected at least two security question/answer pairs.");
        }
        SecurityQuestion entity = new SecurityQuestion();
        entity.setUserId(currentUserId);
        entity.setQuestionAndAnswer1(hashedQAPairs.get(0));
        entity.setQuestionAndAnswer2(hashedQAPairs.get(1));
        return entity;
    }


    private String createHashPassword(String password){
        return passwordEncoder.encode(password);
    }

    private UserSummary createUser(UserCreationRequest userCreationRequest) {
        try{
            UserSummary userSummary = webClient.post()
                    .uri(userServiceBaseUrl+"/user-api/v1/create-user")
                    .bodyValue(userCreationRequest)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->{
                        return clientResponse.createError().flatMap(error -> Mono.error((Throwable) error));
                    })
                    .bodyToMono(UserSummary.class).block();

            if(userSummary == null) {
                throw new RuntimeException("Received null response from User Service.");
            }
            return userSummary;
        }catch(WebClientResponseException e){
            System.err.println("User Service API Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw new RuntimeException("Failed to register user due to an error in the User Service.", e);
        }

    }
}
