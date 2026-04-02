package org.hexaware.bookingservice.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.transaction.Transactional;
import org.hexaware.bookingservice.dtos.eventDtos.PaymentResultPayload;
import org.hexaware.bookingservice.entites.Booking;
import org.hexaware.bookingservice.enums.BookingStatus;
import org.hexaware.bookingservice.enums.PaymentStatus;
import org.hexaware.bookingservice.enums.SeatStatus;
import org.hexaware.bookingservice.repositories.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentResultConsumer {

    @Autowired
    private BookingRepository bookingRepository;

    // We use a clean, standard ObjectMapper for manual deserialization
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @KafkaListener(topics = "payment-events-topic", groupId = "booking-group")
    @Transactional
    public void consumePaymentResult(String rawJson) {
        try {
            System.out.println("Raw Kafka message received: " + rawJson);

            JsonNode rootNode = objectMapper.readTree(rawJson);
            String eventType = rootNode.get("eventType").asText();

            // Pull the data object and convert it to your concrete Record
            PaymentResultPayload payload = objectMapper.convertValue(
                    rootNode.get("data"),
                    PaymentResultPayload.class
            );

            Booking booking = bookingRepository.findById(payload.bookingId())
                    .orElseThrow(() -> new RuntimeException("CRITICAL: Booking not found for ID: " + payload.bookingId()));

            System.out.println("Received payment result for Booking ID " + payload.bookingId() + ": " + eventType);

            if ("PAYMENT_SUCCESSFUL".equals(eventType)) {
                booking.setPaymentStatus(PaymentStatus.COMPLETED);
                booking.setBookingStatus(BookingStatus.CONFIRMED);

                // Seat locking logic...
                System.out.println("Booking " + payload.bookingId() + " successfully CONFIRMED.");
            }
            else if ("PAYMENT_FAILED".equals(eventType)) {
                booking.setPaymentStatus(PaymentStatus.FAILED);
                booking.setBookingStatus(BookingStatus.FAILED);

                // Seat unlocking logic...
                System.out.println("Booking " + payload.bookingId() + " marked as FAILED. Reason: " + payload.failureReason());
            }

            bookingRepository.save(booking);

        } catch (Exception e) {
            System.err.println("CRITICAL ERROR: Failed to process payment event!");
            e.printStackTrace();
        }
    }
}