package org.hexaware.busservice.services;


import org.hexaware.busservice.dtos.*;
import org.hexaware.busservice.dtos.busDtos.*;
import org.hexaware.busservice.dtos.companyDtos.CompanyCreationRequest;
import org.hexaware.busservice.dtos.companyDtos.CompanyCreationResponse;
import org.hexaware.busservice.dtos.documentDtos.*;
import org.hexaware.busservice.dtos.staffDtos.AddBusStaffRequest;
import org.hexaware.busservice.dtos.staffDtos.BusStaffCreationRequest;
import org.hexaware.busservice.dtos.staffDtos.BusStaffCreationResponse;
import org.hexaware.busservice.dtos.staffDtos.BusStaffResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface BusService {

    public ResponseDto<DocumentUploadResponse> uploadBusOperatorDocument(MultipartFile aadharCard, MultipartFile panCard, DocumentUploadRequest request) throws IOException;
    public ResponseDto<BusDocumentUploadResponse> uploadBusDocuments(BusDocumentUploadRequest request, MultipartFile rcBook,MultipartFile insurance,MultipartFile registrationNumberPlate)throws IOException;
    public ResponseDto<DocumentResponse> getDocumentsByUserId(UUID userId);
    public ResponseDto<CompanyCreationResponse> createCompany(CompanyCreationRequest companyCreationRequest);
    public ResponseDto<BusTemplateCreationResponse> saveBusTemplate(BusTemplateCreationRequest busTemplateCreationRequest);
    public ResponseDto<BusCreationResponse> createBus(BusCreationRequest busCreationRequest);
    public ResponseDto<CompanyCreationResponse> getCompanyDetail(UUID userId);
    public ResponseDto<List<BusTemplateResponse>> getBusTemplates(UUID userId);
    public ResponseDto<List<BusFleetResponse>> getAllExistingCompanyBuses(UUID companyId);
    public ResponseDto<BusStaffCreationResponse> createBusStaff(BusStaffCreationRequest request,MultipartFile driverLicense);
    public ResponseDto<List<BusStaffResponse>> getAllExistingBusStaffs(UUID companyId);
    public ResponseDto<BusStaffResponse> getBusStaff(UUID id);
    public ResponseDto<BusStaffResponse> updateBusStaff(AddBusStaffRequest request);
    public ResponseDto<BusDocumentUploadResponse> getBusDocuments(UUID busId);
    public ResponseDto<BusDocumentUploadResponse> updateBusDocuments(UUID busId, MultipartFile rc, MultipartFile ins, MultipartFile plate) throws IOException;
    public ResponseDto<String> deleteBusDocuments(UUID busId) throws IOException;
    public ResponseDto<DocumentUploadResponse> updateOperatorDocuments(UUID userId, MultipartFile aadhar, MultipartFile pan) throws IOException;
    public ResponseDto<String> deleteOperatorDocuments(UUID userId) throws IOException;
}
