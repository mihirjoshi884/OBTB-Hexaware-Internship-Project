package org.hexaware.notificationservice.handlers;

import jakarta.mail.MessagingException;
import org.hexaware.notificationservice.dtos.NotificationEvent;
import org.hexaware.notificationservice.queue.PendingQueue;
import org.hexaware.notificationservice.services.EmailSenderService;
import org.hexaware.notificationservice.services.TopicHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@Service
public class UserAccountRecoveryTopicHandler implements TopicHandler {

    @Value("${angular.base-uri}") //http://localhost:4200
    private String angularBaseUrl;
    private final Logger log = LoggerFactory.getLogger(UserAccountRecoveryTopicHandler.class);
    @Autowired
    private EmailSenderService emailSenderService;


    @Override
    public String getTopicKey() {
        return "account_recovery";
    }

    @Override
    public void handle(PendingQueue.PendingEventData data) throws UnsupportedEncodingException {
        var event = (NotificationEvent<Map<String, Object>>) data.payload();
        Map<String, Object> payload = event.getData();

        Map<String, Object> userMap = (Map<String, Object>) payload.get("user");
        String token = (String) payload.get("token");

        String email = (String) userMap.get("email");
        String username = (String) userMap.get("username");

        // Construct the URL
        String redirectUrl = String.format("%s/change-password?token=%s&email=%s",
                angularBaseUrl, token, email);

        // Create an HTML Body with a clickable link
        String htmlBody = "<h3>Hello " + username + ",</h3>" +
                "<p>You requested to reset your password. Please click the button below to proceed:</p>" +
                "<a href=\"" + redirectUrl + "\" style=\"" +
                "background-color: #007bff; color: white; padding: 10px 20px; " +
                "text-decoration: none; border-radius: 5px; display: inline-block;" +
                "\">Reset Password</a>" +
                "<p>If the button doesn't work, copy and paste this link into your browser:</p>" +
                "<p>" + redirectUrl + "</p>" +
                "<p><strong>Note:</strong> This link will expire in 15 minutes.</p>";

        try {
            emailSenderService.sendEmail(email, "Reset Your Password", htmlBody);
            log.info("Email successfully dispatched to {}", email); // Log SUCCESS here
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("CRITICAL: Email delivery failed for {}. Error: {}", email, e.getMessage());
            throw new RuntimeException("Email delivery failed", e);
        }
    }
}
