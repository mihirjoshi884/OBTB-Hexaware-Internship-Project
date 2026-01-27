package org.hexaware.busservice.services.impl;

import org.hexaware.busservice.dtos.DocumentResponse;
import org.hexaware.busservice.dtos.DocumentUploadRequest;
import org.hexaware.busservice.dtos.DocumentUploadResponse;
import org.hexaware.busservice.dtos.ResponseDto;
import org.hexaware.busservice.entities.BusOperator;
import org.hexaware.busservice.enums.VerificationStatus;
import org.hexaware.busservice.exceptions.DocumentsNotFoundException;
import org.hexaware.busservice.repositories.BusOperatorRepository;
import org.hexaware.busservice.services.BusService;
import org.hexaware.busservice.services.ImageUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class BusServiceImpl implements BusService {

    @Autowired
    private BusOperatorRepository busOperatorRepository;
    @Autowired
    private ImageUploadService imageUploadService;


    @Override
    public ResponseDto<DocumentUploadResponse> uploadBusOperatorDocument(MultipartFile aadharCard
            , MultipartFile panCard
            , DocumentUploadRequest request)
            throws IOException {

        BusOperator busOperator = busOperatorRepository.findByUserId(request.userId())
                .orElse(new BusOperator());

        busOperator.setUserId(request.userId());
        busOperator.setAadharNumber(request.aadharNumber());
        busOperator.setPanNumber(request.panNumber());

        // Save first to ensure we have a BusOperatorId for Cloudinary paths
        var createdBusOperator = busOperatorRepository.save(busOperator);
        var imageUploadResult = imageUploadService.uploadImage(aadharCard,panCard,createdBusOperator.getBusOperatorId());
        createdBusOperator.setAadharFileId((String) imageUploadResult.get("aadharPublicId"));
        createdBusOperator.setAadharUrl((String) imageUploadResult.get("aadharUrl"));
        createdBusOperator.setPanFileId((String) imageUploadResult.get("panPublicId"));
        createdBusOperator.setPanUrl((String) imageUploadResult.get("panUrl"));
        createdBusOperator.setStatus(VerificationStatus.PENDING);
        var savedBusOperator = busOperatorRepository.save(createdBusOperator);

        var documentResponse = new DocumentUploadResponse(
                savedBusOperator.getBusOperatorId(),
                savedBusOperator.getAadharNumber(),
                savedBusOperator.getAadharUrl(),
                savedBusOperator.getPanNumber(),
                savedBusOperator.getPanUrl(),
                savedBusOperator.getStatus(),
                savedBusOperator.getSubmittedAt(),
                savedBusOperator.getVerifiedAt()
        );
        return new ResponseDto<DocumentUploadResponse>(documentResponse,200,"Document uploaded");
    }

    @Override
    public ResponseDto<DocumentResponse> getDocumentsByUserId(UUID userId) {
        var result = busOperatorRepository.findByUserId(userId).orElseThrow(()-> new DocumentsNotFoundException("document not found"));
        var documentResponse = new DocumentResponse(
                result.getBusOperatorId(),
                result.getAadharNumber(),
                result.getAadharUrl(),
                result.getPanNumber(),
                result.getPanUrl(),
                result.getStatus(),
                result.getSubmittedAt(),
                result.getVerifiedAt()
        );
        return new ResponseDto<>(documentResponse,200,"Document uploaded");
    }
}
