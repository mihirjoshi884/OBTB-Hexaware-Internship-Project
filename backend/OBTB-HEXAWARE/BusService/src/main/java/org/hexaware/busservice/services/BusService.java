package org.hexaware.busservice.services;


import org.hexaware.busservice.dtos.*;
import org.hexaware.busservice.entities.Bus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface BusService {

    public ResponseDto<DocumentUploadResponse> uploadBusOperatorDocument(MultipartFile aadharCard, MultipartFile panCard, DocumentUploadRequest request) throws IOException;
    public ResponseDto<DocumentResponse> getDocumentsByUserId(UUID userId);
    public ResponseDto<CompanyCreationResponse> createCompany(CompanyCreationRequest companyCreationRequest);
    public ResponseDto<BusTemplateCreationResponse> saveBusTemplate(BusTemplateCreationRequest busTemplateCreationRequest);
    public ResponseDto<BusCreationResponse> createBus(BusCreationRequest busCreationRequest);
    public ResponseDto<CompanyCreationResponse> getCompanyDetail(UUID userId);
    public ResponseDto<List<BusTemplateResponse>> getBusTemplates(UUID userId);
    public ResponseDto<List<BusFleetResponse>> getAllExistingCompanyBuses(UUID companyId);
}
