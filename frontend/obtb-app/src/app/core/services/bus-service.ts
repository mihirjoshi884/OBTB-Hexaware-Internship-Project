import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment.development';
import {
  BusCreationRequest,
  BusCreationResponse,
  BusFleetResponse,
  BusTemplate,
  BusTemplateCreationRequest,
  BusTemplateCreationResponse,
  CompanyCreationRequest,
  CompanyCreationResponse,
  DocumentResponse,
  DocumentUploadRequest,
  DocumentUploadResponse,
  LayoutLookupResponse,
  ResponseDto
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
      `${this.busServiceBaseUrl}/upload-documents`, 
      formData
    );
  }

  fetchExistingDocuments(userId: string): Observable<ResponseDto<DocumentResponse>>{
    return this.http.get<ResponseDto<DocumentResponse>>(`${this.busServiceBaseUrl}/documents/${userId}`);
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
  createBus(requestData: BusCreationRequest): Observable<ResponseDto<BusCreationResponse>>{
    return this.http.post<ResponseDto<BusCreationResponse>>(`${this.busServiceBaseUrl}/bus/create-bus`,requestData);
  }
  fetchBusTemplate(userId: string): Observable<ResponseDto<BusTemplate[]>>{
    return this.http.get<ResponseDto<BusTemplate[]>>(`${this.busServiceBaseUrl}/bus/get-bus-templates/${userId}`);
  }
  fetchBuses(companyId: string): Observable<ResponseDto<BusFleetResponse[]>>{
    return this.http.get<ResponseDto<BusFleetResponse[]>>(`${this.busServiceBaseUrl}/bus/get-buses/${companyId}`);
  }
}
