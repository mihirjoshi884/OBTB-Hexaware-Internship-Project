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

import java.util.Map;

@Service
public class PasswordChangedTopicHandler implements TopicHandler {

    @Value("${userservice.base-uri}")
    private String userServiceBaseUrl;

    private final Logger log = LoggerFactory.getLogger(PasswordChangedTopicHandler.class);

    @Autowired
    private WebClient notificationWebClient;
    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    @Autowired
    private EmailSenderService emailSenderService;

    @Override
    public String getTopicKey() {
        return "password_change";
    }

    @SneakyThrows
    @Override
    public void handle(PendingQueue.PendingEventData data) {
        NotificationEvent<?> event = (NotificationEvent<?>) data.payload();
        // 1. Get the raw map from the event
        Map<String, Object> outerMap = (Map<String, Object>) event.getData();
        // 2. Extract the nested "user-information" map
        Object userInformation = outerMap.get("user-information");
        if (userInformation == null) {
            throw new IllegalArgumentException("Payload missing required 'user-information' key");
        }
        // 3. Now convert that nested object to your Summary class
        Summary summary = objectMapper.convertValue(userInformation, Summary.class);
        log.info("Sending password change confirmation to: {}", summary.getEmail());

        // 3. Draft a Security-Focused Email
        String subject = "Security Alert: Your FastX password was changed";

        String body = "<html><body>"
                + "<h3>Hello " + summary.getUsername() + ",</h3>"
                + "<p>This is a confirmation that the password for your <b>FastX</b> account has been successfully changed.</p>"
                + "<p><b>When:</b> " + new java.util.Date() + "</p>"
                + "<br>"
                + "<p style='color: #d9534f; font-weight: bold;'>If you did not make this change, please contact our support team immediately or reset your password using the 'Forgot Password' link on the login page.</p>"
                + "<br>"
                + "<p>Regards,<br><b>The FastX Security Team</b></p>"
                + "</body></html>";

        emailSenderService.sendEmail(summary.getEmail(), subject, body);
    }
}
