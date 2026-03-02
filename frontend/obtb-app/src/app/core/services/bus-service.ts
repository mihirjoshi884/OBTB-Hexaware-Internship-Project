import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment.development';
import {
  AddBusStaffRequest,
  BusCreationRequest,
  BusDocumentUploadRequest,
  BusDocumentUploadResponse,
  BusFleetResponse,
  BusStaffCreationResponse,
  BusStaffResponse,
  BusTemplate,
  BusTemplateCreationRequest,
  BusTemplateCreationResponse,
  CompanyCreationRequest,
  CompanyCreationResponse,
  DocumentResponse,
  DocumentUploadRequest,
  DocumentUploadResponse,
  LayoutLookupResponse,
  ResponseDto,
  RouteRequest,
  RouteResponse
} from '../../interfaces/bus-operator.models';


@Injectable({
  providedIn: 'root',
})
export class BusService {
  
  private busServiceBaseUrl = environment.baseUrls['busService.base-uri']+"/bus-api/private/v1";

  constructor(private http: HttpClient) {}

  uploadDocuments(
    requestData: DocumentUploadRequest, 
    aadharCard: File, 
    panCard: File
  ): Observable<ResponseDto<DocumentUploadResponse>> {
    const formData = new FormData();

    // Wrap the DTO in a Blob to specify application/json content type
    formData.append('data', new Blob([JSON.stringify(requestData)], {
      type: 'application/json'
    }));

    // Names must match @RequestPart names in PrivateController.java
    formData.append('aadharCard', aadharCard);
    formData.append('panCard', panCard);

    return this.http.post<ResponseDto<DocumentUploadResponse>>(
      `${this.busServiceBaseUrl}/operator/upload-documents`, 
      formData
    );
  }

  fetchExistingDocuments(userId: string): Observable<ResponseDto<DocumentResponse>>{
    return this.http.get<ResponseDto<DocumentResponse>>(`${this.busServiceBaseUrl}/operator/documents/${userId}`);
  }

  updateOperatorDocuments(
    userId: string, 
    aadhar?: File, 
    pan?: File
  ): Observable<ResponseDto<DocumentUploadResponse>> {
    const formData = new FormData();
    if (aadhar) formData.append('aadharCard', aadhar);
    if (pan) formData.append('panCard', pan);

    return this.http.patch<ResponseDto<DocumentUploadResponse>>(
      `${this.busServiceBaseUrl}/operator/documents/update/${userId}`,
      formData
    );
  }


  deleteOperatorDocuments(userId: string): Observable<ResponseDto<string>> {
    return this.http.delete<ResponseDto<string>>(
      `${this.busServiceBaseUrl}/operator/documents/${userId}`
    );
  }

  fetchExistingCompany(userId: string): Observable<ResponseDto<CompanyCreationResponse>>{
    return this.http.get<ResponseDto<CompanyCreationResponse>>(`${this.busServiceBaseUrl}/company/get-company/${userId}`);
  }

  fetchLayoutTemplates(): Observable<ResponseDto<LayoutLookupResponse>>{
    return this.http.get<ResponseDto<LayoutLookupResponse>>(`${this.busServiceBaseUrl}/layout-template/templates`);
  }
  createCompany(requestData: CompanyCreationRequest): Observable<ResponseDto<CompanyCreationResponse>>{
    return this.http.post<ResponseDto<CompanyCreationResponse>>(`${this.busServiceBaseUrl}/company/create-company`,requestData); 
  }
  createBusTemplate(requestData: BusTemplateCreationRequest): Observable<ResponseDto<BusTemplateCreationResponse>>{
    return this.http.post<ResponseDto<BusTemplateCreationResponse>>(`${this.busServiceBaseUrl}/bus/create-template`,requestData);
  }
  createBus(requestData: BusCreationRequest): Observable<ResponseDto<BusFleetResponse>>{
      return this.http.post<ResponseDto<BusFleetResponse>>(`${this.busServiceBaseUrl}/bus/create-bus`,requestData);
  }
  fetchBusTemplate(userId: string): Observable<ResponseDto<BusTemplate[]>>{
    return this.http.get<ResponseDto<BusTemplate[]>>(`${this.busServiceBaseUrl}/bus/get-bus-templates/${userId}`);
  }
  fetchBuses(companyId: string): Observable<ResponseDto<BusFleetResponse[]>>{
    return this.http.get<ResponseDto<BusFleetResponse[]>>(`${this.busServiceBaseUrl}/bus/get-buses/${companyId}`);
  }
  updateStaffLicense(staffId: string, licenseFile: File): Observable<ResponseDto<BusStaffResponse>> {
    const formData = new FormData();
    formData.append('driverLicense', licenseFile);
    
    return this.http.patch<ResponseDto<BusStaffResponse>>(
      `${this.busServiceBaseUrl}/bus/staff/${staffId}/update-license`,
      formData
    );
  }
  fetchCompanyStaff(companyId: string): Observable<ResponseDto<BusStaffResponse[]>>{
    return this.http.get<ResponseDto<BusStaffResponse[]>>(`${this.busServiceBaseUrl}/bus/staff/get-all-staffs/${companyId}`);
  }
  createBusStaff(formData: FormData): Observable<ResponseDto<BusStaffCreationResponse>>{
    return this.http.post<ResponseDto<BusStaffCreationResponse>>(`${this.busServiceBaseUrl}/bus/staff/create-staff`, formData);
  }
  updateBusStaff(requests: AddBusStaffRequest[]): Observable<any> {
      return this.http.patch(`${this.busServiceBaseUrl}/bus/staff/update-staff`, requests);
  }
  uploadBusDocuments(request: BusDocumentUploadRequest): Observable<ResponseDto<BusDocumentUploadResponse>>{
    const formData = new FormData();

    const metaData = {
      ownerId: request.ownerId,
      companyId: request.companyId,
      busId: request.busId
    }

    formData.append('data',new Blob([JSON.stringify(metaData)],{
      type: 'application/json'
    }));

    if (request.rcBook) formData.append('rcBook', request.rcBook);
    if (request.insurance) formData.append('insurance', request.insurance);
    if (request.registrationNumberPlate) formData.append('registrationNumberPlate', request.registrationNumberPlate);

    return this.http.post<ResponseDto<BusDocumentUploadResponse>>(`${this.busServiceBaseUrl}/bus/upload-documents`,formData);
  }

  fetchBusDocument(busId: string): Observable<ResponseDto<BusDocumentUploadResponse>>{
    return this.http.get<ResponseDto<BusDocumentUploadResponse>>(`${this.busServiceBaseUrl}/bus/documents/${busId}`);
  }

  updateBusDocument(busId: string, updates: { rcBook?: File, insurance?: File, plate?: File }): Observable<ResponseDto<BusDocumentUploadResponse>> {
    const formData = new FormData();
    
    if (updates.rcBook) formData.append('rcBook', updates.rcBook);
    if (updates.insurance) formData.append('insurance', updates.insurance);
    if (updates.plate) formData.append('registrationNumberPlate', updates.plate);

    return this.http.patch<ResponseDto<BusDocumentUploadResponse>>(
      `${this.busServiceBaseUrl}/bus/documents/update/${busId}`,
      formData
    );
  }

  deleteBusDocument(busId: string): Observable<ResponseDto<string>> {
    return this.http.delete<ResponseDto<string>>(
      `${this.busServiceBaseUrl}/bus/documents/${busId}`
    );
  }
  
  fetchAllExistingCompanyRoute(companyId: string): Observable<ResponseDto<RouteResponse []>>{
    return this.http.get<ResponseDto<RouteResponse []>>(`${this.busServiceBaseUrl}/bus/routes/company/${companyId}`);
  }

  createRouteForCompany(requestData: RouteRequest): Observable<ResponseDto<RouteResponse>>{
    return this.http.post<ResponseDto<RouteResponse>>(`${this.busServiceBaseUrl}/bus/routes/create`,requestData);
  }

  updateRouteForCompnay(requestData: RouteRequest,routeId: string): Observable<ResponseDto<RouteResponse>>{
    return this.http.patch<ResponseDto<RouteResponse>>(`${this.busServiceBaseUrl}/bus/routes/update/${routeId}`,requestData);
  }

  deleteRouteForCompany(routeId: string): Observable<ResponseDto<RouteResponse>>{
    return this.http.delete<ResponseDto<RouteResponse>>(`${this.busServiceBaseUrl}/bus/routes/${routeId}`);
  }
}
