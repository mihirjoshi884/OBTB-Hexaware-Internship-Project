package org.hexaware.userservice.services;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.hexaware.userservice.dtos.eventDtos.BookingInitiatedPayload;
import org.hexaware.userservice.dtos.eventDtos.PaymentResultPayload;
import org.hexaware.userservice.dtos.eventDtos.SagaEvent;
import org.hexaware.userservice.enums.EventType;
import org.hexaware.userservice.enums.PaymentStatus; // Imported assuming you have this
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Component
public class BookingInitiatedConsumer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private WalletService walletService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @KafkaListener(topics = "booking-initiated-topic", groupId = "wallet-group")
    public void consumeBookingInitiated(SagaEvent<BookingInitiatedPayload> event) {

        // 🛡️ Fix 1: Record accessors don't use the "get" prefix!
        BookingInitiatedPayload data = objectMapper.convertValue(
                event.data(),
                BookingInitiatedPayload.class
        );

        // 🛡️ Fix 2: Records are immutable! We must use the All-Args constructor
        // instead of empty instantiation and set() methods.
        SagaEvent<PaymentResultPayload> responseEvent;

        try {
            // 💰 Hit the DB to process the actual movement of money
            walletService.processWalletTransfer(data.payerUserId(), data.payeeUserId(), data.amount());

            System.out.println("Successfully processed payment for booking: " + data.bookingId());

            // 🛡️ Fix 3: Match your record constructor and use PaymentStatus enum
            PaymentResultPayload successPayload = new PaymentResultPayload(
                    data.bookingId(),
                    PaymentStatus.COMPLETED,
                    null
            );

            responseEvent = new SagaEvent<>(
                    EventType.PAYMENT_SUCCESSFUL,
                    successPayload,
                    LocalDateTime.now()
            );

        } catch (Exception e) {
            System.err.println("Wallet transaction failed for booking " + data.bookingId() + ": " + e.getMessage());

            // 🛡️ Fix 4: Match your record constructor and handle failure
            PaymentResultPayload failurePayload = new PaymentResultPayload(
                    data.bookingId(),
                    PaymentStatus.FAILED,
                    e.getMessage()
            );

            responseEvent = new SagaEvent<>(
                    EventType.PAYMENT_FAILED,
                    failurePayload,
                    LocalDateTime.now()
            );
        }

        // Fire response back to the Booking Service!
        kafkaTemplate.send("payment-events-topic", responseEvent);
    }
}