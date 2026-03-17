package org.hexaware.bookingservice.controllers;


import org.hexaware.bookingservice.dtos.tripDtos.TripCreationRequest;
import org.hexaware.bookingservice.entites.TripTemplate;
import org.hexaware.bookingservice.services.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/booking-api/private/v1")
@PreAuthorize("hasRole('BUS_OPERATOR')")
public class PrivateController {

    @Autowired
    private TripService tripService;

    // CREATE: Define a new schedule and spawn the first instance
    @PostMapping("/trip/create-trip")
    public ResponseEntity<?> createTrip(@RequestBody TripCreationRequest request){
        var response = tripService.createTrip(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // READ: Get all master schedules (Templates) for the operator's company
    @GetMapping("/templates/company/{companyId}")
    public ResponseEntity<?> getMyTemplates(@PathVariable UUID companyId) {
        return ResponseEntity.ok(tripService.getTemplatesByCompany(companyId));
    }

    // READ: Get all upcoming bookable journeys (Instances) for a specific route
    @PostMapping("/instances/get-active-instances")
    public ResponseEntity<?> getActiveJourneys(@RequestBody List<UUID> templateIds) {
        var response = tripService.getUpcomingInstancesByTemplateIds(templateIds);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // UPDATE: Activate or Deactivate a schedule
    // Useful if a bus is undergoing maintenance or a route is seasonal
    @PatchMapping("/templates/{templateId}/toggle-status")
    public ResponseEntity<Void> toggleTemplate(@PathVariable UUID templateId, @RequestParam boolean active) {
        tripService.toggleTemplateStatus(templateId, active);
        return ResponseEntity.noContent().build();
    }

    // DELETE: Remove a schedule and cancel future scheduled instances
    @DeleteMapping("/templates/{templateId}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable UUID templateId) {
        tripService.deleteTemplate(templateId);
        return ResponseEntity.noContent().build();
    }
}
