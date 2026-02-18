package org.hexaware.busservice.controller;

import org.hexaware.busservice.dtos.*;
import org.hexaware.busservice.services.BusService;
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

    @PostMapping("/bus/create-template")
    public ResponseEntity<?> createTemplate(@RequestBody BusTemplateCreationRequest request) throws IOException {
        var response = busService.saveBusTemplate(request);
        return ResponseEntity.status(response.getStatus()).body(response.getBody());
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
}
