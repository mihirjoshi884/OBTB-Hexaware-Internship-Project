package org.hexaware.busservice.services;


import org.hexaware.busservice.dtos.DocumentResponse;
import org.hexaware.busservice.dtos.DocumentUploadRequest;
import org.hexaware.busservice.dtos.DocumentUploadResponse;
import org.hexaware.busservice.dtos.ResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface BusService {

    public ResponseDto<DocumentUploadResponse> uploadBusOperatorDocument(MultipartFile aadharCard, MultipartFile panCard, DocumentUploadRequest request) throws IOException;
    public ResponseDto<DocumentResponse> getDocumentsByUserId(UUID userId);
}
