package org.hexaware.busservice.services.impl;

import com.cloudinary.Cloudinary;
import org.hexaware.busservice.services.ImageUploadService;
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
    public Map<String, String> uploadImage(MultipartFile aadharCard, MultipartFile panCard, UUID busOperatorId) throws RuntimeException, IOException {
        Map<String, String> results = new HashMap<>();

        try {
            // Upload Aadhar and extract both ID and URL
            Map aadharUpload = uploadToCloudinary(aadharCard, "bus_operator_docs/" + busOperatorId + "/aadhar");
            results.put("aadharUrl", (String) aadharUpload.get("secure_url"));
            results.put("aadharPublicId", (String) aadharUpload.get("public_id"));

            // Upload PAN and extract both ID and URL
            Map panUpload = uploadToCloudinary(panCard, "bus_operator_docs/" + busOperatorId + "/pan");
            results.put("panUrl", (String) panUpload.get("secure_url"));
            results.put("panPublicId", (String) panUpload.get("public_id"));

            return results;
        } catch (IOException e) {
            throw new RuntimeException("Document upload failed", e);
        }
    }
    private Map uploadToCloudinary(MultipartFile file, String publicId) throws IOException {
        // Check if the file is a PDF
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
            throw new IllegalArgumentException("Only PDF files are allowed! Received: " + contentType);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("public_id", publicId);
        params.put("overwrite", true);
        params.put("flags","attachment:false");
        params.put("resource_type", "raw");

        return this.cloudinary.uploader().upload(file.getBytes(), params);
    }
}
