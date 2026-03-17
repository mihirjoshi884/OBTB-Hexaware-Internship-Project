import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ResponseDto } from 'src/app/interfaces/bus-operator.models';
import { environment } from 'src/environments/environment.development';
import {
  TripCreationRequest,
  TripInstanceDto,
  TripTemplateDto
} from '../../interfaces/trip-model';

@Injectable({
  providedIn: 'root',
})
export class TripService {

  constructor(private readonly http: HttpClient) {}
  
  private baseUrl = environment.baseUrls['bookingService.base-uri'] + "/booking-api/private/v1";

  /**
   * CREATE: Define a new schedule and spawn the first instance
   * Maps to: POST /trip/create-trip
   */
  createTrip(request: TripCreationRequest): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/trip/create-trip`, request);
  }

  /**
   * READ: Get all master schedules (Templates) for the operator's company
   * Maps to: GET /templates/company/{companyId}
   */
  getMyTemplates(companyId: string): Observable<ResponseDto<TripTemplateDto []>> {
    return this.http.get<ResponseDto<TripTemplateDto []>>(`${this.baseUrl}/templates/company/${companyId}`);
  }

  /**
   * READ: Get all upcoming bookable journeys (Instances) for a specific route
   * Maps to: GET /instances/route/{routeId}
   */
  getActiveJourneys(templateIds: string []): Observable<ResponseDto<TripInstanceDto []>> {
    return this.http.post<ResponseDto<TripInstanceDto []>>(`${this.baseUrl}/instances/get-active-instances`,templateIds);
  }

  /**
   * UPDATE: Activate or Deactivate a schedule
   * Maps to: PATCH /templates/{templateId}/toggle-status?active=true/false
   */
  toggleTemplateStatus(templateId: string, active: boolean): Observable<void> {
    const params = new HttpParams().set('active', active.toString());
    return this.http.patch<void>(`${this.baseUrl}/templates/${templateId}/toggle-status`, {}, { params });
  }

  /**
   * DELETE: Remove a schedule and cancel future scheduled instances
   * Maps to: DELETE /templates/{templateId}
   */
  deleteTemplate(templateId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/templates/${templateId}`);
  }
}