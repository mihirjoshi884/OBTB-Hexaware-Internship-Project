import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  BookingInitiatedResponse,
  CurrentUserResponse,
  PrimaryPassangerDetailCreationRequest,
  PrimaryPassangerDetailDto,
  ResponseDto,
  SeatMappingDto
} from 'src/app/interfaces/booking-interfaces';
import { environment } from 'src/environments/environment.development';

@Injectable({
  providedIn: 'root',
})
export class BookingService {
  constructor(
    private readonly http: HttpClient
  ){}

  baseUrl: string = environment.baseUrls['bookingService.base-uri'] + "/booking-api/bookings/v1";
  authUrl: string = environment.baseUrls['authservice.base-uri']+"/auth-api/v1";

  getCurrentUser(): Observable<CurrentUserResponse>{
    return this.http.get<CurrentUserResponse>(`${this.authUrl}/user/get-current-user`);
  }

  // 1. Fetch Seats
  getSeatMapping(instanceId: string): Observable<ResponseDto<SeatMappingDto>> {
    return this.http.get<ResponseDto<SeatMappingDto>>(`${this.baseUrl}/instance/seat-mappings/${instanceId}`);
  }

  // 2. Check if Profile exists (Expected return type: boolean)
  checkProfileAvailability(userId: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.baseUrl}/is-profile-available/${userId}`);
  }

  // 3. Create Profile
  createProfile(request: PrimaryPassangerDetailCreationRequest): Observable<ResponseDto<PrimaryPassangerDetailDto>> {
    return this.http.post<ResponseDto<PrimaryPassangerDetailDto>>(`${this.baseUrl}/create-primary-passanger`, request);
  }

  // 4. Book Ticket (Accepts dynamic FormData straight from the component!)
  bookTicket(payload: FormData): Observable<ResponseDto<BookingInitiatedResponse>> {
    return this.http.post<ResponseDto<BookingInitiatedResponse>>(`${this.baseUrl}/book-ticket`, payload);
  }

  // 5. Fire up Payment Gateway (Triggering Kafka flow)
  initiatePayment(bookingId: string): Observable<ResponseDto<boolean>> {
    // Backend returns ResponseDto<Boolean>
    return this.http.post<ResponseDto<boolean>>(`${this.baseUrl}/${bookingId}/initiate-payment`, {});
  }

  // 6. Polling endpoint to check Kafka processing state
  getBookingStatus(bookingId: string): Observable<ResponseDto<string>> {
    // Backend returns ResponseDto<String>
    return this.http.get<ResponseDto<string>>(`${this.baseUrl}/${bookingId}/status`);
  }
}