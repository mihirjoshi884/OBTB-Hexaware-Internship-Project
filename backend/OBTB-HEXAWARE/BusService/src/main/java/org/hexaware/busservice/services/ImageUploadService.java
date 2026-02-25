package org.hexaware.busservice.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;


public interface ImageUploadService {

    public Map uploadImage(MultipartFile aadharCard, MultipartFile panCard, UUID busOperatorId) throws RuntimeException,  IOException;
    public Map<String, String> uploadDriverLicense(MultipartFile driverLicense, UUID staffId) throws RuntimeException,  IOException;

}
