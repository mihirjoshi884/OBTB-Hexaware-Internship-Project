package org.hexaware.busservice.services.impl;

import org.hexaware.busservice.dtos.*;
import org.hexaware.busservice.entities.Bus;
import org.hexaware.busservice.entities.BusOperator;
import org.hexaware.busservice.entities.BusTemplate;
import org.hexaware.busservice.entities.Company;
import org.hexaware.busservice.enums.VerificationStatus;
import org.hexaware.busservice.exceptions.DocumentsNotFoundException;
import org.hexaware.busservice.repositories.BusOperatorRepository;
import org.hexaware.busservice.repositories.BusRepository;
import org.hexaware.busservice.repositories.BusTemplateRepository;
import org.hexaware.busservice.repositories.CompanyRepository;
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
    @Autowired
    private BusRepository busRepository;
    @Autowired
    private BusTemplateRepository busTemplateRepository;
    @Autowired
    private CompanyRepository companyRepository;


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

    @Override
    public ResponseDto<CompanyCreationResponse> createCompany(CompanyCreationRequest companyCreationRequest) {
        var company = new Company();
        company.setCompanyName(companyCreationRequest.companyName());
        company.setOwnerId(companyCreationRequest.ownerId());
        company.setOwnerName(companyCreationRequest.ownerName());
        var savedRecord = companyRepository.save(company);

        var companyResponse = new CompanyCreationResponse(
                savedRecord.getCompanyId(),
                savedRecord.getCompanyName(),
                savedRecord.getOwnerName(),
                savedRecord.getOwnerId()
        );
        var response = new ResponseDto<>(companyResponse,200,"Company created");
        return response;
    }

    @Override
    public ResponseDto<BusTemplateCreationResponse> saveBusTemplate(BusTemplateCreationRequest busTemplateCreationRequest) {
        var template = new BusTemplate();
        template.setTemplateName(busTemplateCreationRequest.templateName());
        template.setLayoutData(busTemplateCreationRequest.layoutData());
        template.setTotalSeats(busTemplateCreationRequest.totalSeats());

        var savedTemplate = busTemplateRepository.save(template);
        var templateResponse = new BusTemplateCreationResponse(
                savedTemplate.getTemplateId(),
                savedTemplate.getTemplateName(),
                savedTemplate.getLayoutData(),
                savedTemplate.getTotalSeats()

        );

        var response = new ResponseDto<>(templateResponse,200,"Template created");
        return response;
    }

    @Override
    public ResponseDto<BusCreationResponse> createBus(BusCreationRequest busCreationRequest) {
        // 1. Fetch references for Company and Template
        // Note: getReferenceById is better than findById here because it creates a
        // Proxy. We only need the IDs to save the foreign key, avoiding an extra DB SELECT.
        Company company = companyRepository.findById(busCreationRequest.companyId())
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + busCreationRequest.companyId()));

        BusTemplate template = busTemplateRepository.findById(busCreationRequest.templateId())
                .orElseThrow(() -> new RuntimeException("Template not found with ID: " + busCreationRequest.templateId()));

        // 2. Initialize and Map the Bus entity
        Bus bus = new Bus();
        bus.setBusName(busCreationRequest.busName());
        bus.setBusType(busCreationRequest.busType());
        bus.setCompany(company);
        bus.setTemplate(template);

        // 3. Save to database
        Bus savedBus = busRepository.save(bus);

        // 4. Map saved entity back to the Response DTO
        BusCreationResponse busResponse = new BusCreationResponse(
                savedBus.getBusId(),
                savedBus.getBusName(),
                savedBus.getBusType(),
                savedBus.getCompany().getCompanyName(),
                savedBus.getTemplate().getTemplateName()
        );

        return new ResponseDto<>(busResponse, 201, "Bus skeleton created successfully. Please upload documents next.");
    }
}
