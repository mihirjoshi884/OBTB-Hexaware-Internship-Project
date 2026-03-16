package org.hexaware.busservice.services.impl;

import jakarta.transaction.Transactional;
import org.hexaware.busservice.dtos.*;
import org.hexaware.busservice.dtos.busDtos.*;
import org.hexaware.busservice.dtos.companyDtos.CompanyCreationRequest;
import org.hexaware.busservice.dtos.companyDtos.CompanyCreationResponse;
import org.hexaware.busservice.dtos.companyDtos.CompanySummaryDTO;
import org.hexaware.busservice.dtos.documentDtos.*;
import org.hexaware.busservice.dtos.routeDtos.RouteResponse;
import org.hexaware.busservice.dtos.routeDtos.RouteStopDTO;
import org.hexaware.busservice.dtos.staffDtos.AddBusStaffRequest;
import org.hexaware.busservice.dtos.staffDtos.BusStaffCreationRequest;
import org.hexaware.busservice.dtos.staffDtos.BusStaffCreationResponse;
import org.hexaware.busservice.dtos.staffDtos.BusStaffResponse;
import org.hexaware.busservice.entities.*;
import org.hexaware.busservice.enums.DutyType;
import org.hexaware.busservice.enums.StaffType;
import org.hexaware.busservice.enums.VerificationStatus;
import org.hexaware.busservice.exceptions.BusNotFoundException;
import org.hexaware.busservice.exceptions.BusTypeMismatchException;
import org.hexaware.busservice.exceptions.CompanyNotFoundException;
import org.hexaware.busservice.exceptions.DocumentsNotFoundException;
import org.hexaware.busservice.repositories.*;
import org.hexaware.busservice.services.BusService;
import org.hexaware.busservice.services.BusTemplateService;
import org.hexaware.busservice.services.ImageUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BusServiceImpl implements BusService {

    @Autowired
    private BusOperatorRepository busOperatorRepository;
    @Autowired
    private ImageUploadService imageUploadService;
    @Autowired
    private BusTemplateService busTemplateService;
    @Autowired
    private BusRepository busRepository;
    @Autowired
    private BusTemplateRepository busTemplateRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private LayoutRepository layoutRepository;
    @Autowired
    private BusStaffRepository busStaffRepository;
    @Autowired
    private RouteRepository routeRepository;


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
    public ResponseDto<BusDocumentUploadResponse> uploadBusDocuments(BusDocumentUploadRequest request,
                                                                     MultipartFile rcBook,
                                                                     MultipartFile insurance,
                                                                     MultipartFile registrationNumberPlate)
            throws IOException {


        var bus = busRepository.findById(request.busId()).orElseThrow(() -> new BusNotFoundException("Bus not found with the id: " + request.busId()));
        var company = companyRepository.findById(request.companyId()).orElseThrow(()-> new CompanyNotFoundException("Company not found with the id: " + request.companyId()));
        var imageUploadResponse = imageUploadService.uploadBusDocuments(
                rcBook, insurance, registrationNumberPlate, request.ownerId(),company.getCompanyName(),request.busId()
        );
        bus.setRcDocUrl((String)  imageUploadResponse.get("rcBookUrl"));
        bus.setRcDocid((String) imageUploadResponse.get("rcBookPublicId"));
        bus.setInsurancePolicyDocUrl((String)  imageUploadResponse.get("insuranceUrl"));
        bus.setInsurancePolicyDocId((String) imageUploadResponse.get("insurancePolicyNumber"));
        bus.setRegistrationNumberPlateDOCUrl((String)  imageUploadResponse.get("registrationNumberPlateUrl"));
        bus.setRegistrationNumberPlateDocId((String)  imageUploadResponse.get("registrationNumberPlatePublicId"));
        bus.setStatus(VerificationStatus.PENDING);
        var updatedBus = busRepository.save(bus);
        var insurancePolicyRes = new BusDocumentResponse(updatedBus.getInsurancePolicyDocId(),"insurance policy",updatedBus.getInsurancePolicyDocUrl());
        var rcBookRes = new BusDocumentResponse(updatedBus.getRcDocid(),"registration book",updatedBus.getRegistrationNumberPlateDOCUrl());
        var regNumbPlate = new BusDocumentResponse(updatedBus.getRegistrationNumberPlateDocId(),"registration number",updatedBus.getRegistrationNumberPlateDOCUrl());
        var busDocumentUploadResponse = new BusDocumentUploadResponse(
                updatedBus.getBusId(),
                updatedBus.getCompany().getOwnerId(),
                updatedBus.getCompany().getCompanyId(),
                insurancePolicyRes,
                rcBookRes,
                regNumbPlate
        );
        return new ResponseDto<>(busDocumentUploadResponse,200,"Document uploaded");
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
                savedRecord.getOwnerId(),
                savedRecord.getStatus()
        );
        var response = new ResponseDto<>(companyResponse,200,"Company created");
        return response;
    }

    @Override
    public ResponseDto<BusTemplateCreationResponse> saveBusTemplate(BusTemplateCreationRequest busTemplateCreationRequest) {
        var template = new BusTemplate();
        template.setTemplateName(busTemplateCreationRequest.templateName());
        var company = companyRepository.findByOwnerId(busTemplateCreationRequest.ownerId());
        var bluePrint = layoutRepository.findById(busTemplateCreationRequest.layoutId())
                .orElseThrow(() -> new RuntimeException("Layout Template not found"));
        template.setLayoutData(busTemplateService.generateLayoutData(bluePrint,busTemplateCreationRequest.totalSeats()));
        template.setLayoutTemplate(bluePrint);
        template.setBusType(busTemplateCreationRequest.busType());
        template.setTotalSeats(busTemplateCreationRequest.totalSeats());
        template.setCompany(company);

        var savedTemplate = busTemplateRepository.save(template);
        var templateResponse = new BusTemplateCreationResponse(
                savedTemplate.getTemplateId(),
                savedTemplate.getTemplateName(),
                savedTemplate.getLayoutData(),
                savedTemplate.getLayoutTemplate().getLayoutId(),
                savedTemplate.getTotalSeats()

        );

        var response = new ResponseDto<>(templateResponse,200,"Template created");
        return response;
    }

    @Override
    public ResponseDto<BusFleetResponse> createBus(BusCreationRequest busCreationRequest) {
        // 1. Fetch references for Company and Template
        // Note: getReferenceById is better than findById here because it creates a
        // Proxy. We only need the IDs to save the foreign key, avoiding an extra DB SELECT.
        Company company = companyRepository.findById(busCreationRequest.companyId())
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + busCreationRequest.companyId()));

        BusTemplate template = busTemplateRepository.findById(busCreationRequest.templateId())
                .orElseThrow(() -> new RuntimeException("Template not found with ID: " + busCreationRequest.templateId()));

        if(busCreationRequest.busType() != template.getBusType()) {
            throw new BusTypeMismatchException("bus type mismatch");
        }
        // 2. Initialize and Map the Bus entity
        Bus bus = new Bus();
        bus.setBusName(busCreationRequest.busName());
        bus.setCompany(company);
        bus.setTemplate(template);
        bus.setRcNumber(busCreationRequest.rcNumber());
        bus.setInsurancePolicyNumber(busCreationRequest.insurancePolicyNumber());
        bus.setRegistrationNumber(busCreationRequest.registrationNumber());


        // 3. Save to database
        Bus savedBus = busRepository.save(bus);

        // 4. Map saved entity back to the Response DTO
        BusFleetResponse busResponse = new BusFleetResponse(
                savedBus.getBusId(),
                savedBus.getBusName(),
                savedBus.getStatus(),
                savedBus.getRegistrationNumber(),
                new CompanySummaryDTO(
                        savedBus.getCompany().getCompanyName(),
                        savedBus.getCompany().getCompanyId()
                ),
                new TemplateSummaryDTO(
                        savedBus.getTemplate().getTemplateName(),
                        savedBus.getTemplate().getBusType().toString(),
                        savedBus.getTemplate().getLayoutData()
                )
        );

        return new ResponseDto<>(busResponse, 201, "Bus skeleton created successfully. Please upload documents next.");
    }

    @Override
    public ResponseDto<CompanyCreationResponse> getCompanyDetail(UUID userId) {
        var fetchedCompany = companyRepository.findByOwnerId(userId);
        if(fetchedCompany == null){
            throw new CompanyNotFoundException("Company not found with ownerID: " + userId);
        }
        var companyResponse = new CompanyCreationResponse(
                fetchedCompany.getCompanyId(),
                fetchedCompany.getCompanyName(),
                fetchedCompany.getOwnerName(),
                fetchedCompany.getOwnerId(),
                fetchedCompany.getStatus()
        );
        return new ResponseDto<>(companyResponse,200,"company found by id-"+fetchedCompany.getCompanyId());
    }

    @Override
    public ResponseDto<List<BusTemplateResponse>> getBusTemplates(UUID userId) {
        var company = companyRepository.findByOwnerId(userId);
        if (company == null) {
            throw new CompanyNotFoundException("Company not found for user: " + userId);
        }
        var templates = busTemplateService.fetchBusTemplates(company.getCompanyId());
        if (templates.isEmpty()) {
            return new ResponseDto<>(List.of(), 200, "No bus templates found. Please create a template before adding a bus.");
        }
        List<BusTemplateResponse> templateResponses = templates.stream()
                .map(t -> new BusTemplateResponse(
                        t.getTemplateId(),
                        t.getTemplateName(),
                        t.getBusType(),
                        t.getTotalSeats(),
                        t.getLayoutData()
                ))
                .toList();

        return new ResponseDto<>(templateResponses, 200, "Bus templates fetched successfully");
    }

    @Override
    public ResponseDto<List<BusFleetResponse>> getAllExistingCompanyBuses(UUID companyId) {
        if (!companyRepository.existsById(companyId)) {
            return new ResponseDto<>(null, 404, "Company not found");
        }

        List<Bus> buses = busRepository.findAllByCompanyCompanyId(companyId);

        List<BusFleetResponse> response = buses.stream()
                .map(bus -> new BusFleetResponse(
                        bus.getBusId(),
                        bus.getBusName(),
                        bus.getStatus(),
                        bus.getRegistrationNumber(),
                        new CompanySummaryDTO(bus.getCompany().getCompanyName(), bus.getCompany().getCompanyId()),
                        new TemplateSummaryDTO(bus.getTemplate().getTemplateName(),
                                bus.getTemplate().getBusType().toString(),
                                bus.getTemplate().getLayoutData())
                )).toList();

        return new ResponseDto<>(response, 200, "Buses retrieved successfully");
    }

    @Override
    @Transactional
    public ResponseDto<BusStaffCreationResponse> createBusStaff(BusStaffCreationRequest request, MultipartFile driverLicense) {
        // 1. Verify Company exists
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + request.companyId()));

        // 2. Check for existing staff with same phone number
        if (busStaffRepository.findByPhoneNumber(request.phoneNumber()) != null) {
            return new ResponseDto<>(null, 400, "Phone number already registered.");
        }

        // 3. Conditional Driver Validation
        if (request.staffType() == StaffType.BUS_DRIVER) {
            // Validate license number presence and uniqueness only for Drivers
            if (request.driverLicenseNumber() == null || request.driverLicenseNumber().isEmpty()) {
                return new ResponseDto<>(null, 400, "Driver License number is required for drivers.");
            }
            if (busStaffRepository.existsByDriverLicenseNumber(request.driverLicenseNumber())) {
                return new ResponseDto<>(null, 400, "Driver License number already exists.");
            }
        }

        // 4. Map Request to Entity
        BusStaff staff = new BusStaff();
        staff.setName(request.name());
        staff.setAge(request.age());
        staff.setPhoneNumber(request.phoneNumber());
        staff.setStaffType(request.staffType());
        staff.setCompany(company);

        // Only set license number for Drivers
        if (request.staffType() == StaffType.BUS_DRIVER) {
            staff.setDriverLicenseNumber(request.driverLicenseNumber());
        }

        // 5. Initial Save
        staff = busStaffRepository.save(staff);

        // 6. Conditional Document Upload
        // Only upload if it is a Driver AND a file was provided
        if (request.staffType() == StaffType.BUS_DRIVER && driverLicense != null && !driverLicense.isEmpty()) {
            try {
                Map<String, String> uploadResult = imageUploadService.uploadDriverLicense(driverLicense, staff.getStaffId());
                staff.setDriverLicenseUrl(uploadResult.get("driverLicenseUrl"));
                staff.setDriverLicenseDocId(uploadResult.get("driverLicensePublicId"));

                staff = busStaffRepository.save(staff); // Update with Cloudinary details
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload driver license document", e);
            }
        }

        // 7. Prepare Response
        BusStaffCreationResponse response = new BusStaffCreationResponse(
                staff.getStaffId(),
                staff.getName(),
                staff.getPhoneNumber(),
                staff.getDriverLicenseNumber(), // Will be null for conductors
                staff.getStaffType()
        );

        return new ResponseDto<>(response, 201, "Bus staff created successfully");
    }
    @Override
    public ResponseDto<List<BusStaffResponse>> getAllExistingBusStaffs(UUID companyId) {
        var staffs = busStaffRepository.findAllByCompany_CompanyId(companyId);
        List<BusStaffResponse> staffResponses = staffs.stream()
                .map(
                        staff -> new BusStaffResponse(
                                staff.getStaffType(),
                                staff.getStaffId(),
                                staff.getDutyType(),
                                staff.getName(),
                                // SAFE CHECK: If bus is null, return null (or a default UUID)
                                staff.getBus() != null ? staff.getBus().getBusId() : null,
                                staff.getDriverLicenseUrl(),
                                staff.getDriverLicenseNumber()
                        )
                ).toList();

        return new ResponseDto<>(
                staffResponses,
                200,
                "Successfully retrieved " + staffResponses.size() + " staff members"
        );
    }

    @Override
    public ResponseDto<BusStaffResponse> getBusStaff(UUID id) {
        var staff = busStaffRepository.findById(id).orElseThrow(() -> new RuntimeException("Staff not found with ID: " + id));
        var response = new BusStaffResponse(
                staff.getStaffType(),
                staff.getStaffId(),
                staff.getDutyType(),
                staff.getName(),
                staff.getBus().getBusId(),
                staff.getDriverLicenseUrl(),
                staff.getDriverLicenseNumber()
        );
        return new ResponseDto<>(response, 200, "Bus staff retrieved successfully");
    }

    // BusServiceImpl.java

    @Override
    @Transactional
    public ResponseDto<BusStaffResponse> updateBusStaffList(List<AddBusStaffRequest> requests) {
        // 1. Guard clause: If the list is empty, return success immediately
        if (requests == null || requests.isEmpty()) {
            return new ResponseDto<>(null, 200, "No staff updates required");
        }

        BusStaff lastProcessedStaff = null;

        for (AddBusStaffRequest req : requests) {
            if (req.staffId() == null) continue;

            var staff = busStaffRepository.findById(req.staffId())
                    .orElseThrow(() -> new RuntimeException("Staff not found: " + req.staffId()));

            if (req.busId() == null) {
                // Unassigning staff from bus
                staff.setBus(null);
                staff.setDutyType(null);
            } else {
                // Assigning staff to bus
                var bus = busRepository.findById(req.busId())
                        .orElseThrow(() -> new RuntimeException("Bus not found: " + req.busId()));
                staff.setBus(bus);
                staff.setDutyType(req.dutyType());
            }
            lastProcessedStaff = busStaffRepository.save(staff);
        }

        // 2. Handle the case where the list had items but none were valid
        if (lastProcessedStaff == null) {
            return new ResponseDto<>(null, 200, "Processed but no records were changed");
        }

        // 3. Return the last updated staff record as per your existing structure
        var responseBody = new BusStaffResponse(
                lastProcessedStaff.getStaffType(),
                lastProcessedStaff.getStaffId(),
                lastProcessedStaff.getDutyType(),
                lastProcessedStaff.getName(),
                lastProcessedStaff.getBus() != null ? lastProcessedStaff.getBus().getBusId() : null,
                lastProcessedStaff.getDriverLicenseUrl(),
                lastProcessedStaff.getDriverLicenseNumber()
        );

        return new ResponseDto<>(responseBody, 200, "Staff assignments updated successfully");
    }

    @Override
    public ResponseDto<BusDocumentUploadResponse> getBusDocuments(UUID busId) {
        var fetchedBus = busRepository.findById(busId)
                .orElseThrow(() -> new BusNotFoundException("Bus not found"));
        var insurancePolicyRes = new BusDocumentResponse(fetchedBus.getInsurancePolicyDocId(),"insurance policy",fetchedBus.getInsurancePolicyDocUrl());
        var rcBookRes = new BusDocumentResponse(fetchedBus.getRcDocid(),"registration book",fetchedBus.getRegistrationNumberPlateDOCUrl());
        var regNumbPlate = new BusDocumentResponse(fetchedBus.getRegistrationNumberPlateDocId(),"registration number",fetchedBus.getRegistrationNumberPlateDOCUrl());
        var busDocumentUploadResponse = new BusDocumentUploadResponse(
                fetchedBus.getBusId(),
                fetchedBus.getCompany().getOwnerId(),
                fetchedBus.getCompany().getCompanyId(),
                insurancePolicyRes,
                rcBookRes,
                regNumbPlate
        );
        return new ResponseDto<>(busDocumentUploadResponse,200,"Document uploaded");
    }

    @Override
    @Transactional
    public ResponseDto<BusDocumentUploadResponse> updateBusDocuments(UUID busId, MultipartFile rc, MultipartFile ins, MultipartFile plate) throws IOException {
        Bus bus = busRepository.findById(busId).orElseThrow(() -> new BusNotFoundException("Bus not found"));

        var uploadRes = imageUploadService.uploadBusDocuments(
                rc, ins, plate, bus.getCompany().getOwnerId(), bus.getCompany().getCompanyName(), busId
        );

        if (rc != null) {
            bus.setRcDocUrl((String) uploadRes.get("rcBookUrl"));
            bus.setRcDocid((String) uploadRes.get("rcBookPublicId"));
        }
        if (ins != null) {
            bus.setInsurancePolicyDocUrl((String) uploadRes.get("insuranceUrl"));
            bus.setInsurancePolicyDocId((String) uploadRes.get("insurancePublicId"));
        }
        if (plate != null) {
            bus.setRegistrationNumberPlateDOCUrl((String) uploadRes.get("registrationNumberPlateUrl"));
            bus.setRegistrationNumberPlateDocId((String) uploadRes.get("registrationNumberPlatePublicId"));
        }

        busRepository.save(bus);
        return getBusDocuments(busId);
    }

    @Override
    @Transactional
    public ResponseDto<String> deleteBusDocuments(UUID busId) throws IOException {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new BusNotFoundException("Bus not found"));

        // 1. Delete files from Cloudinary/Service
        if (bus.getRcDocid() != null) imageUploadService.deleteFile(bus.getRcDocid());
        if (bus.getInsurancePolicyDocId() != null) imageUploadService.deleteFile(bus.getInsurancePolicyDocId());
        if (bus.getRegistrationNumberPlateDocId() != null) imageUploadService.deleteFile(bus.getRegistrationNumberPlateDocId());

        // 2. Clear document references
        bus.setRcDocUrl(null);
        bus.setRcDocid(null);
        bus.setInsurancePolicyDocUrl(null);
        bus.setInsurancePolicyDocId(null);
        bus.setRegistrationNumberPlateDOCUrl(null);
        bus.setRegistrationNumberPlateDocId(null);

        // 3. FIX: Reset the status so the UI shows "Upload" instead of "Edit"
        bus.setStatus(VerificationStatus.NOT_SUBMITTED);

        busRepository.save(bus);
        return new ResponseDto<>("Bus documents deleted", 200, "Deleted");
    }


    @Override
    @Transactional
    public ResponseDto<BusStaffResponse> updateStaffLicense(UUID staffId, MultipartFile driverLicense) throws IOException {
        // 1. Find the staff member
        BusStaff staff = busStaffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        // 2. Upload new license (ImageUploadService handles replacing if FileId exists)
        var uploadResults = imageUploadService.uploadDriverLicense(driverLicense, staffId);

        // 3. Update staff details
        staff.setDriverLicenseUrl(uploadResults.get("driverLicenseUrl"));
        staff.setDriverLicenseDocId(uploadResults.get("driverLicensePublicId"));

        var updatedStaffDoc  = busStaffRepository.save(staff);
        UUID busId = (updatedStaffDoc.getBus() != null) ? updatedStaffDoc.getBus().getBusId() : null;
        var response = new BusStaffResponse(
                updatedStaffDoc.getStaffType(),
                updatedStaffDoc.getStaffId(),
                updatedStaffDoc.getDutyType(),
                updatedStaffDoc.getName(),
                busId,
                updatedStaffDoc.getDriverLicenseUrl(),
                updatedStaffDoc.getDriverLicenseNumber()
        );
        // 4. Return updated response
        return new ResponseDto<>(response, 200, "License updated successfully");
    }

    @Override
    @Transactional
    public ResponseDto<DocumentUploadResponse> updateOperatorDocuments(UUID userId, MultipartFile aadhar, MultipartFile pan) throws IOException {
        BusOperator operator = busOperatorRepository.findByUserId(userId)
                .orElseThrow(() -> new DocumentsNotFoundException("Operator not found"));

        // Use existing method to upload.
        // Note: Your ImageUploadServiceImpl uses 'overwrite: true', so it replaces the file at the same PublicID
        var results = imageUploadService.uploadImage(aadhar, pan, operator.getBusOperatorId());

        if (aadhar != null && !aadhar.isEmpty()) {
            operator.setAadharUrl((String) results.get("aadharUrl"));
            operator.setAadharFileId((String) results.get("aadharPublicId"));
        }
        if (pan != null && !pan.isEmpty()) {
            operator.setPanUrl((String) results.get("panUrl"));
            operator.setPanFileId((String) results.get("panPublicId"));
        }

        operator.setStatus(VerificationStatus.PENDING);
        busOperatorRepository.save(operator);

        return new ResponseDto<>(null, 200, "Operator documents updated");
    }

    @Override
    @Transactional
    public ResponseDto<String> deleteOperatorDocuments(UUID userId) throws IOException {
        BusOperator operator = busOperatorRepository.findByUserId(userId).get();

        if (operator.getAadharFileId() != null) imageUploadService.deleteFile(operator.getAadharFileId());
        if (operator.getPanFileId() != null) imageUploadService.deleteFile(operator.getPanFileId());

        operator.setAadharUrl(null);
        operator.setAadharFileId(null);
        operator.setPanUrl(null);
        operator.setPanFileId(null);
        operator.setStatus(VerificationStatus.NOT_SUBMITTED);

        busOperatorRepository.save(operator);
        return new ResponseDto<>("Operator documents cleared", 200, "Deleted");
    }

    @Override
    public ResponseDto<List<BusFleetResponse>> getAllBuses(List<UUID> busIds) {
        List<Bus> busResponse = busRepository.findAllById(busIds);
        List<BusFleetResponse> response = busResponse.stream().map(this::mapToBusFleetResponse).toList();
        return new  ResponseDto<>(response, 200, "All bus fleets found");
    }

    @Override
    public ResponseDto<List<RouteResponse>> getAllRoutes(List<UUID> routeIds) {
        List<Route> routeResponse = routeRepository.findAllById(routeIds);
        List<RouteResponse> response = routeResponse.stream().map(this::mapToRouteResponse).toList();
        return new   ResponseDto<>(response, 200, "All routes found");
    }

    private RouteResponse mapToRouteResponse(Route route) {
        return new RouteResponse(
                route.getRouteId(),
                route.getRouteName(),
                route.getOrigin(),
                route.getDestination(),
                route.getTotalDistance(),
                route.getEstimatedDuration(),
                route.getStops().stream().map(this::mapToRouteStop).toList()
        );
    }
    private RouteStopDTO mapToRouteStop(RouteStop routeStop) {
        return new RouteStopDTO(
                routeStop.getStopName(),
                routeStop.getStopOrder(),
                routeStop.getDistanceFromOrigin(),
                routeStop.getTimeOffsetFromOrigin()
        );
    }
    private BusFleetResponse mapToBusFleetResponse(Bus bus) {
        return new BusFleetResponse(
                bus.getBusId(),
                bus.getBusName(),
                bus.getStatus(),
                bus.getRegistrationNumber(),
                new CompanySummaryDTO(
                        bus.getCompany().getCompanyName(),
                        bus.getCompany().getCompanyId()
                ),
                new TemplateSummaryDTO(
                        bus.getTemplate().getTemplateName(),
                        bus.getTemplate().getBusType().toString(),
                        bus.getTemplate().getLayoutData()
                )
        );
    }
}
