package org.hexaware.bookingservice.services;

import org.hexaware.bookingservice.dtos.ResponseDto;
import org.hexaware.bookingservice.dtos.tripDtos.TripCreationRequest;
import org.hexaware.bookingservice.dtos.tripDtos.TripDetails;
import org.hexaware.bookingservice.dtos.tripDtos.TripTemplateDto;
import org.hexaware.bookingservice.entites.TripInstance;
import org.hexaware.bookingservice.entites.TripTemplate;

import java.util.List;
import java.util.UUID;

public interface TripService {

    public ResponseDto<TripDetails> createTrip(TripCreationRequest request);
    public ResponseDto<List<TripTemplateDto>> getTemplatesByCompany(UUID companyId);
    public List<TripInstance> getUpcomingInstancesByRoute(UUID routeId);
    public void toggleTemplateStatus(UUID templateId, boolean active);
    public void deleteTemplate(UUID templateId);

}
