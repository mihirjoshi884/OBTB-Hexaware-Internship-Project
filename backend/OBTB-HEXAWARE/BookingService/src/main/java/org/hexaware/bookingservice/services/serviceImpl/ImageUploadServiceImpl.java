package org.hexaware.bookingservice.services.serviceImpl;

import com.cloudinary.Cloudinary;
import org.hexaware.bookingservice.services.ImageUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageUploadServiceImpl implements ImageUploadService {

    @Autowired
    private Cloudinary cloudinary;

    @Override
    public Map uploadIdproof(MultipartFile idProof, UUID passangerId) throws RuntimeException{
        if( idProof == null || idProof.isEmpty()){
            throw new IllegalArgumentException("idProof file is required.");
        }
        Map<String, String> result = new HashMap<>();
        try {
            // Using a more structured folder path
            String folderPath = "passenger_id_docs/" + passangerId + "/id";

            Map uploadResult = uploadToCloudinary(idProof, folderPath);

            // These keys should match what your Service expects to save into BusStaff entity
            result.put("idProofUrl", (String) uploadResult.get("secure_url"));
            result.put("idProofPublicId", (String) uploadResult.get("public_id"));

            return result;
        } catch (IOException e) {
            // Log the error here if you have a logger
            throw new RuntimeException("Cloudinary upload failed for passanger: " + idProof, e);
        }
    }

    @Override
    public Map uploadBusTicket(String pnrNumber) {
        return Map.of();
    }

    private Map uploadToCloudinary(MultipartFile file, String publicId) throws IOException {
        String contentType = file.getContentType();

        // 1. Expanded Validation Logic
        boolean isAllowed = contentType != null && (
                contentType.equalsIgnoreCase("application/pdf") ||
                        contentType.equalsIgnoreCase("image/png") ||
                        contentType.equalsIgnoreCase("image/jpeg")
        );

        if (!isAllowed) {
            throw new IllegalArgumentException("Only PDF, PNG, or JPG files are allowed! Received: " + contentType);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("public_id", publicId);
        params.put("overwrite", true);
        params.put("flags", "attachment:false");

        // 2. CRITICAL: Change "raw" to "auto"
        // "auto" allows Cloudinary to detect if it's an image or a PDF automatically
        params.put("resource_type", "auto");

        return this.cloudinary.uploader().upload(file.getBytes(), params);
    }
}
