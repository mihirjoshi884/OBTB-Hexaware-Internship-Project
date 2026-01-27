import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment.development';
import { DocumentResponse, DocumentUploadRequest, DocumentUploadResponse, ResponseDto } from '../../interfaces/bus-operator.models';
 

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
}
