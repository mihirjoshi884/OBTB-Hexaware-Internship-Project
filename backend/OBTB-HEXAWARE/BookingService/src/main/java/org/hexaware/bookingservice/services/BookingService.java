package org.hexaware.bookingservice.services;

import org.hexaware.bookingservice.dtos.ResponseDto;
import org.hexaware.bookingservice.dtos.bookingDtos.BookingInitiatedResponse;
import org.hexaware.bookingservice.dtos.bookingDtos.BookingRequestDto;
import org.hexaware.bookingservice.dtos.bookingDtos.PrimaryPassangerDetailCreationRequest;
import org.hexaware.bookingservice.dtos.bookingDtos.PrimaryPassangerDetailDto;
import org.hexaware.bookingservice.entites.PrimaryPassangerDetail;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface BookingService {

    public ResponseDto<Boolean> isPrimaryPassangerInfoAvailable(UUID userId);
    public ResponseDto<PrimaryPassangerDetailDto> createPrimaryPassanger(PrimaryPassangerDetailCreationRequest request);
    public ResponseDto<BookingInitiatedResponse> bookTicket(BookingRequestDto request, List<MultipartFile> idFiles);
    public ResponseDto<Boolean> initiatePayment(UUID bookingId);
    public ResponseDto<String> getBookingStatus(UUID bookingId);
}
