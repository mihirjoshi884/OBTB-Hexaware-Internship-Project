import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ResponseDto } from 'src/app/interfaces/bus-operator.models';
import { SearchRequestDto, TripSearchResponseDto } from 'src/app/interfaces/search-interface';
import { environment } from 'src/environments/environment.development';

@Injectable({
  providedIn: 'root',
})
export class SearchService {
  
  constructor(
    private readonly http: HttpClient
  ){}
  baseUrl: string = environment.baseUrls['bookingService.base-uri']+"/booking-api/public/v1";

  findBusesInstances(request: SearchRequestDto): Observable<ResponseDto<TripSearchResponseDto>>{
    return this.http.post<ResponseDto<TripSearchResponseDto>>(`${this.baseUrl}/find`,request);
  }

}
