package org.hexaware.notificationservice.handlers;

import jakarta.mail.MessagingException;
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

import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

@Service
public class UserCreationTopicHandler implements TopicHandler {

    @Value("${angular.base-uri}") //http://localhost:4200
    private String angularBaseUrl;
    @Value("${userservice.base-uri}")
    private String userServiceBaseUrl;
    private final Logger log = LoggerFactory.getLogger(UserCreationTopicHandler.class);
    @Autowired
    private WebClient notificationWebClient;

    @Autowired
    private EmailSenderService emailSenderService;

    @Override
    public String getTopicKey() {
        return "user";
    }


    @SneakyThrows
    @Override
    public void handle(PendingQueue.PendingEventData data)  {
        NotificationEvent event = (NotificationEvent) data.payload();
        Summary userSummary = event.getData();
        UUID userId = userSummary.getUserId();
        log.info("Handling URGENT user event from topic: {} for User ID: {}", data.topicName(), userId);
        String userEmail = notificationWebClient.get()
                .uri(userServiceBaseUrl+"/user-api/v1/user-info/email/"+userSummary.getUserId())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("Retrieved email: {}. Sending notification...", userEmail);
        String verificationLink = angularBaseUrl+"/verify-account?userId=" + userId;

        // 3. Draft the Subject
        String subject = "Welcome to FastX! Verify your account to get started 🚀";

        // 4. Draft the HTML Body (Warm welcome + Regards)
        String body = "<html><body>"
                + "<h3>Hello there,</h3>"
                + "<p>A very warm welcome to the <b>FastX</b> family! We are thrilled to have you join our community.</p>"
                + "<p>To ensure your account is secure and ready for use, please verify your identity by clicking the link below:</p>"
                + "<p><a href='" + verificationLink + "' style='color: #007bff; font-weight: bold;'>Click here to verify your account</a></p>"
                + "<p>If the button above doesn't work, you can copy and paste this URL into your browser:<br>"
                + "<i>" + verificationLink + "</i></p>"
                + "<br>"
                + "<p>We're excited to see what you'll achieve with us!</p>"
                + "<p>Warm regards,<br><b>The FastX Team</b></p>"
                + "</body></html>";

        // 5. Send the email using your service
        emailSenderService.sendEmail(userEmail, subject, body);


    }
}
