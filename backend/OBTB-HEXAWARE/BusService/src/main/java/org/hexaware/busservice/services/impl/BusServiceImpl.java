package org.hexaware.busservice.services.impl;

import jakarta.transaction.Transactional;
import org.hexaware.busservice.dtos.*;
import org.hexaware.busservice.dtos.busDtos.*;
import org.hexaware.busservice.dtos.companyDtos.CompanyCreationRequest;
import org.hexaware.busservice.dtos.companyDtos.CompanyCreationResponse;
import org.hexaware.busservice.dtos.companyDtos.CompanySummaryDTO;
import org.hexaware.busservice.dtos.documentDtos.DocumentResponse;
import org.hexaware.busservice.dtos.documentDtos.DocumentUploadRequest;
import org.hexaware.busservice.dtos.documentDtos.DocumentUploadResponse;
import org.hexaware.busservice.dtos.staffDtos.AddBusStaffRequest;
import org.hexaware.busservice.dtos.staffDtos.BusStaffCreationRequest;
import org.hexaware.busservice.dtos.staffDtos.BusStaffCreationResponse;
import org.hexaware.busservice.dtos.staffDtos.BusStaffResponse;
import org.hexaware.busservice.entities.*;
import org.hexaware.busservice.enums.DutyType;
import org.hexaware.busservice.enums.StaffType;
import org.hexaware.busservice.enums.VerificationStatus;
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
    public ResponseDto<BusCreationResponse> createBus(BusCreationRequest busCreationRequest) {
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
        BusCreationResponse busResponse = new BusCreationResponse(
                savedBus.getBusId(),
                savedBus.getBusName(),
                savedBus.getCompany().getCompanyName(),
                savedBus.getTemplate().getTemplateName()
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
                                staff.getBus() != null ? staff.getBus().getBusId() : null
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
          staff.getBus().getBusId()
        );
        return new ResponseDto<>(response, 200, "Bus staff retrieved successfully");
    }

    @Override
    public ResponseDto<BusStaffResponse> updateBusStaff(AddBusStaffRequest request) {
        var staff = busStaffRepository.findById(request.staffId()).orElseThrow(()-> new RuntimeException("Staff not found with ID: " + request.staffId()) );
        var bus = busRepository.findById(request.busId()).orElseThrow(()-> new RuntimeException("Bus not found with ID: " + request.busId()));
        staff.setBus(bus);
        staff.setDutyType(request.dutyType());
        var savedStaff = busStaffRepository.save(staff);
        var response = new BusStaffResponse(
                savedStaff.getStaffType(),
                savedStaff.getStaffId(),
                savedStaff.getDutyType(),
                savedStaff.getName(),
                savedStaff.getBus().getBusId()
        );
        return new  ResponseDto<>(response, 200, "Bus staff updated successfully");
    }
}
