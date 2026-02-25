package org.hexaware.busservice.controller;

import org.hexaware.busservice.dtos.*;
import org.hexaware.busservice.dtos.busDtos.BusCreationRequest;
import org.hexaware.busservice.dtos.busDtos.BusTemplateCreationRequest;
import org.hexaware.busservice.dtos.companyDtos.CompanyCreationRequest;
import org.hexaware.busservice.dtos.documentDtos.DocumentUploadRequest;
import org.hexaware.busservice.dtos.staffDtos.AddBusStaffRequest;
import org.hexaware.busservice.dtos.staffDtos.BusStaffCreationRequest;
import org.hexaware.busservice.enums.StaffType;
import org.hexaware.busservice.services.BusService;
import org.hexaware.busservice.services.LayoutTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/bus-api/private/v1")
@PreAuthorize("hasRole('BUS_OPERATOR')")
public class PrivateController {

    @Autowired
    private BusService busService;
    @Autowired
    private LayoutTemplateService layoutTemplateService;


    //http://localhost:8086/bus-api/private/v1/uploads-documents
    //http://localhost:9090/bus/bus-api/private/v1/uploads-documents
    @PostMapping(value = "/upload-documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadBusOperatorDocument(
            @RequestPart("data") DocumentUploadRequest request,
            @RequestPart("aadharCard") MultipartFile aadharCard,
            @RequestPart("panCard") MultipartFile panCard
    ) throws IOException {

        // Early Validation
        if (!isPdf(aadharCard) || !isPdf(panCard)) {
            return ResponseEntity.badRequest().body("Both Aadhar and PAN cards must be in PDF format.");
        }

        ResponseDto result = busService.uploadBusOperatorDocument(aadharCard, panCard, request);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @GetMapping("/documents/{userId}")
    public ResponseEntity<?> getBusOperatorDocuments(@PathVariable UUID userId) {
        // This call should fetch the DocumentUploadResponse (URLs and Status) from your DB
        ResponseDto result = busService.getDocumentsByUserId(userId);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    private boolean isPdf(MultipartFile file) {
        return file != null && "application/pdf".equals(file.getContentType());
    }

    @PostMapping("/bus/create-bus")
    public ResponseEntity<?> createBus(@RequestBody BusCreationRequest request) throws IOException {
        var response = busService.createBus(request);
        return ResponseEntity.status(response.getStatus()).body(response.getBody());
    }

    @GetMapping("/bus/get-buses/{companyId}")
    public ResponseEntity<?> getAllExistingCompanyBuses(@PathVariable UUID companyId) throws IOException {
        var response = busService.getAllExistingCompanyBuses(companyId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/bus/create-template")
    public ResponseEntity<?> createTemplate(@RequestBody BusTemplateCreationRequest request) throws IOException {
        var response = busService.saveBusTemplate(request);
        // FIX: Return the whole response object, not just the body
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/company/create-company")
    public ResponseEntity<?> createCompany(@RequestBody CompanyCreationRequest request) throws IOException {
        var response = busService.createCompany(request);
        return ResponseEntity.status(response.getStatus()).body(response.getBody());
    }

    @GetMapping("/company/get-company/{userId}")
    public ResponseEntity<?> getCompanyDetails(@PathVariable UUID userId){
        var response = busService.getCompanyDetail(userId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/layout-template/templates")
    public ResponseEntity<?> getLayoutTemplates(){
        var response = layoutTemplateService.getLayoutTemplates();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/bus/get-bus-templates/{userId}")
    public ResponseEntity<?> getBusTemplates(@PathVariable UUID userId){
        var response = busService.getBusTemplates(userId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping(value = "/bus/staff/create-staff", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createBusStaff(
            @RequestPart("data") BusStaffCreationRequest request,
            // Mark as required=false to allow Conductors to skip the file
            @RequestPart(value = "driverLicense", required = false) MultipartFile driverLicense
    ) throws IOException {

        // Only validate if a file is actually sent
        if (driverLicense != null && !driverLicense.isEmpty()) {
            if (!isPdf(driverLicense)) {
                return ResponseEntity.badRequest().body("Driver License must be in PDF format.");
            }
        } else if (request.staffType() == StaffType.BUS_DRIVER) {
            // Enforce file presence specifically for Drivers
            return ResponseEntity.badRequest().body("Driver License document is required for Drivers.");
        }

        var response = busService.createBusStaff(request, driverLicense);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/bus/staff/get-all-staffs/{companyId}")
    public ResponseEntity<?> getAllStaffs(@PathVariable UUID companyId){
        var response = busService.getAllExistingBusStaffs(companyId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/bus/staff/{staffId}")
    public ResponseEntity<?> getBusStaff(@PathVariable UUID staffId){
        var response = busService.getBusStaff(staffId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PatchMapping("/bus/staff/update-staff")
    public ResponseEntity<?> updateBusStaff(@RequestBody AddBusStaffRequest request){
        var response = busService.updateBusStaff(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

}
