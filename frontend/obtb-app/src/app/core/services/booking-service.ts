import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { SeatMappingDto } from 'src/app/interfaces/booking-interfaces';
import { ResponseDto } from 'src/app/interfaces/bus-operator.models';
import { environment } from 'src/environments/environment.development';

@Injectable({
  providedIn: 'root',
})
export class BookingService {
  constructor(
    private readonly http: HttpClient
  ){}
  baseUrl: string = environment.baseUrls['bookingService.base-uri']+"/booking-api/bookings/v1";

  getSeatMapping(instanceId: string): Observable<ResponseDto<SeatMappingDto>>{
    return this.http.get<ResponseDto<SeatMappingDto>>(`${this.baseUrl}/instance/seat-mappings/${instanceId}`);
  }
}
