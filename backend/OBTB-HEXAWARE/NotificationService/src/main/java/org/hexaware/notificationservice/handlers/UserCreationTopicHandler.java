package org.hexaware.notificationservice.handlers;

import lombok.SneakyThrows;
import org.hexaware.notificationservice.dtos.NotificationEvent;
import org.hexaware.notificationservice.dtos.Summary;
import org.hexaware.notificationservice.queue.PendingQueue;
import org.hexaware.notificationservice.services.EmailSenderService;
import org.hexaware.notificationservice.services.TopicHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Service
public class UserCreationTopicHandler implements TopicHandler {

    @Value("${angular.base-uri}")
    private String angularBaseUrl;

    @Value("${userservice.base-uri}")
    private String userServiceBaseUrl;

    private final Logger log = LoggerFactory.getLogger(UserCreationTopicHandler.class);

    @Autowired
    private WebClient notificationWebClient;
    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Autowired
    private EmailSenderService emailSenderService;

    @Override
    public String getTopicKey() {
        return "user_created"; // Ensure this matches the topic name
    }

    @SneakyThrows
    @Override
    public void handle(PendingQueue.PendingEventData data) {
        // 1. Explicitly cast to NotificationEvent<Summary>
        // Use the Summary type because that's what was sent during registration
        NotificationEvent<?> event = (NotificationEvent<?>) data.payload();

        // 2. Extract the Summary object
        Summary userSummary = objectMapper.convertValue(event.getData(), Summary.class);
        UUID userId = userSummary.getUserId();

        log.info("Handling URGENT user creation for User: {}", userSummary.getUsername());

        // External Call to get latest email from User Service
        String userEmail = notificationWebClient.get()
                .uri(userServiceBaseUrl + "/user-api/v1/user-info/email/" + userId)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("Retrieved email: {}. Sending verification link...", userEmail);

        // Construct the Verification Link
        String verificationLink = angularBaseUrl + "/verify-account?userId=" + userId;

        String subject = "Welcome to FastX! Verify your account to get started 🚀";

        String body = "<html><body>"
                + "<h3>Hello " + userSummary.getUsername() + ",</h3>"
                + "<p>A very warm welcome to the <b>FastX</b> family!</p>"
                + "<p>Please verify your identity by clicking the link below:</p>"
                + "<p><a href='" + verificationLink + "' style='background-color: #28a745; color: white; padding: 10px; text-decoration: none; border-radius: 5px;'>Verify Account</a></p>"
                + "<p>Or copy this link: <br><i>" + verificationLink + "</i></p>"
                + "<br><p>Regards,<br><b>The FastX Team</b></p>"
                + "</body></html>";

        emailSenderService.sendEmail(userEmail, subject, body);
    }
}