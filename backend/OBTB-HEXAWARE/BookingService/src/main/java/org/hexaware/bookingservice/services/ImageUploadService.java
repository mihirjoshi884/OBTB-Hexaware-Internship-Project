package org.hexaware.bookingservice.services;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

public interface ImageUploadService {

    public Map uploadIdproof(MultipartFile idProof, UUID passangerId);
    public Map uploadBusTicket(String pnrNumber);
}
