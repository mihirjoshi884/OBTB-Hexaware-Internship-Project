package org.hexaware.bookingservice.controllers;

import org.hexaware.bookingservice.dtos.searchDtos.SearchRequestDto;
import org.hexaware.bookingservice.dtos.searchDtos.TripSearchResponseDto;
import org.hexaware.bookingservice.services.BusSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/booking-api/public/v1")
public class PublicController {


    @Autowired
    private BusSearchService searchService;

    @PostMapping("/find")
    public ResponseEntity<?> searchBuses(@RequestBody SearchRequestDto request) {
        var results = searchService.searchBuses(request);
        return ResponseEntity.status(results.getStatus()).body(results);
    }
}
