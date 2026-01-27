package org.hexaware.busservice.services;

import org.hexaware.busservice.dtos.DocumentUploadResponse;
import org.hexaware.busservice.dtos.ResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;


public interface ImageUploadService {

    public Map uploadImage(MultipartFile aadharCard, MultipartFile panCard, UUID busOperatorId) throws RuntimeException,  IOException;

}
