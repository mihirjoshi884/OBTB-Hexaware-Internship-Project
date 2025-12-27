package org.hexaware.notificationservice.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.UnsupportedEncodingException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailSenderServiceTest {

    @InjectMocks
    private EmailSenderService emailSenderService;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    private final String testEmail = "test@example.com";
    private final String testSubject = "Test Subject";
    private final String testBody = "<h1>Hello</h1>";
    private final String senderEmail = "no-reply@fastx.com";

    @BeforeEach
    void setUp() {
        // Set the @Value field manually
        ReflectionTestUtils.setField(emailSenderService, "emailUsername", senderEmail);

        // Ensure createMimeMessage returns our mock
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    @DisplayName("Send Email: Should successfully trigger mailSender with configured message")
    void testSendEmail_Success() throws MessagingException, UnsupportedEncodingException {
        // Act
        emailSenderService.sendEmail(testEmail, testSubject, testBody);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("Send Email: Should throw MessagingException if mailSender fails")
    void testSendEmail_Failure() {
        // Arrange
        doThrow(new RuntimeException("Connection failed")).when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            emailSenderService.sendEmail(testEmail, testSubject, testBody);
        });
    }
}
