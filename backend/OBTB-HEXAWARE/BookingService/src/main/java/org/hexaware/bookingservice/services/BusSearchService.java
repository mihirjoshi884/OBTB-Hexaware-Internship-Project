package org.hexaware.bookingservice.services;

import org.hexaware.bookingservice.dtos.ResponseDto;
import org.hexaware.bookingservice.dtos.searchDtos.SearchRequestDto;
import org.hexaware.bookingservice.dtos.searchDtos.TripSearchResponseDto;

import java.util.List;

public interface BusSearchService {

    public ResponseDto<?> searchBuses(SearchRequestDto request);
}
