package org.hexaware.busservice.controller;

import org.hexaware.busservice.dtos.*;
import org.hexaware.busservice.dtos.busDtos.BusCreationRequest;
import org.hexaware.busservice.dtos.busDtos.BusFleetResponse;
import org.hexaware.busservice.dtos.busDtos.BusTemplateCreationRequest;
import org.hexaware.busservice.dtos.companyDtos.CompanyCreationRequest;
import org.hexaware.busservice.dtos.documentDtos.BusDocumentUploadRequest;
import org.hexaware.busservice.dtos.documentDtos.DocumentUploadRequest;
import org.hexaware.busservice.dtos.routeDtos.FetchRoute;
import org.hexaware.busservice.dtos.routeDtos.RouteRequest;
import org.hexaware.busservice.dtos.routeDtos.RouteResponse;
import org.hexaware.busservice.dtos.staffDtos.AddBusStaffRequest;
import org.hexaware.busservice.dtos.staffDtos.BusStaffCreationRequest;
import org.hexaware.busservice.enums.StaffType;
import org.hexaware.busservice.services.BusService;
import org.hexaware.busservice.services.LayoutTemplateService;
import org.hexaware.busservice.services.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bus-api/private/v1")
@PreAuthorize("hasRole('BUS_OPERATOR') or hasAuthority('SCOPE_internal')")
public class PrivateController {

    @Autowired
    private BusService busService;
    @Autowired
    private LayoutTemplateService layoutTemplateService;
    @Autowired
    private RouteService routeService;


    //http://localhost:8086/bus-api/private/v1/uploads-documents
    //http://localhost:9090/bus/bus-api/private/v1/uploads-documents
    @PostMapping(value = "/operator/upload-documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
    @GetMapping("/operator/documents/{userId}")
    public ResponseEntity<?> getBusOperatorDocuments(@PathVariable UUID userId) {
        // This call should fetch the DocumentUploadResponse (URLs and Status) from your DB
        ResponseDto result = busService.getDocumentsByUserId(userId);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @PatchMapping(value = "/operator/documents/update/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateOperatorDocuments(
            @PathVariable UUID userId,
            @RequestPart(value = "aadharCard", required = false) MultipartFile aadharCard,
            @RequestPart(value = "panCard", required = false) MultipartFile panCard) throws IOException {
        return ResponseEntity.ok(busService.updateOperatorDocuments(userId, aadharCard, panCard));
    }

    @DeleteMapping("/operator/documents/{userId}")
    public ResponseEntity<?> deleteOperatorDocuments(@PathVariable UUID userId) throws IOException {
        return ResponseEntity.ok(busService.deleteOperatorDocuments(userId));
    }

    @PostMapping(value = "/bus/upload-documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadBusDocuments(
            @RequestPart("data") BusDocumentUploadRequest request,
            @RequestPart("rcBook") MultipartFile rcBook,
            @RequestPart("insurance") MultipartFile insurance,
            @RequestPart("registrationNumberPlate") MultipartFile registrationNumberPlate
            ) throws IOException {

        var response = busService.uploadBusDocuments(request, rcBook, insurance, registrationNumberPlate);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    @GetMapping("/bus/documents/{busId}")
    public ResponseEntity<?> getBusDocuments(@PathVariable UUID busId) {
        return ResponseEntity.ok(busService.getBusDocuments(busId));
    }

    @PatchMapping(value = "/bus/documents/update/{busId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateBusDocuments(
            @PathVariable UUID busId,
            @RequestPart(value = "rcBook", required = false) MultipartFile rcBook,
            @RequestPart(value = "insurance", required = false) MultipartFile insurance,
            @RequestPart(value = "registrationNumberPlate", required = false) MultipartFile registrationNumberPlate) throws IOException {
        return ResponseEntity.ok(busService.updateBusDocuments(busId, rcBook, insurance, registrationNumberPlate));
    }

    @DeleteMapping("/bus/documents/{busId}")
    public ResponseEntity<?> deleteBusDocuments(@PathVariable UUID busId) throws IOException {
        return ResponseEntity.ok(busService.deleteBusDocuments(busId));
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

    @PatchMapping(value = "/bus/staff/{staffId}/update-license", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateStaffLicense(
            @PathVariable UUID staffId,
            @RequestPart("driverLicense") MultipartFile driverLicense) throws IOException {
        var response = busService.updateStaffLicense(staffId, driverLicense);
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
    public ResponseEntity<?> updateBusStaff(@RequestBody List<AddBusStaffRequest> requests){
        var response = busService.updateBusStaffList(requests);
        return ResponseEntity.status(response.getStatus()).body(response);
    }


    @PostMapping("/bus/routes/create")
    public ResponseEntity<?> createRoute(@RequestBody RouteRequest request) {
        ResponseDto<RouteResponse> response = routeService.createRoute(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/bus/routes/company/{companyId}")
    public ResponseEntity<?> getCompanyRoutes(@PathVariable UUID companyId) {
        ResponseDto<List<RouteResponse>> response = routeService.getCompanyRoutes(companyId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PatchMapping("/bus/routes/update/{routeId}")
    public ResponseEntity<?> updateRoute(
            @PathVariable UUID routeId,
            @RequestBody RouteRequest request) {
        ResponseDto<RouteResponse> response = routeService.updateRoute(routeId, request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    // /bus-api/private/v1/bus/get-buses-bulk
    @PostMapping("/bus/get-buses-bulk")
    public ResponseEntity<?> getBusesByBusIds(@RequestBody List<UUID> busIds){
        ResponseDto<List<BusFleetResponse>> response = busService.getAllBuses(busIds);
        System.out.println(response);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    // /bus-api/private/v1/bus/routes/get-routes-bulk
    @PostMapping("/bus/routes/get-routes-bulk")
    public ResponseEntity<?> getRoutesByRouteIds(@RequestBody List<UUID> routeIds){
        ResponseDto<List<RouteResponse>> response = busService.getAllRoutes(routeIds);
        System.out.println(response);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/bus/routes/{routeId}")
    public ResponseEntity<?> deleteRoute(@PathVariable UUID routeId) {
        ResponseDto<?> response = routeService.deleteRoute(routeId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/find/routes")
    public ResponseEntity<?> getAllRoutesBetweenSourceAndDestination(@RequestBody FetchRoute route){
        ResponseDto<?> response = routeService.fetchRouteBetweenSourceAndDestination(route);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/bus/{busId}")
    public ResponseEntity<?> getBusDetails(@PathVariable UUID busId){
        ResponseDto<?> response = busService.getBusDetails(busId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
