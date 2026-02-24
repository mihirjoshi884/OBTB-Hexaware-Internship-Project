package org.hexaware.busservice.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hexaware.busservice.dtos.SeatDetail;
import org.hexaware.busservice.entities.BusTemplate;
import org.hexaware.busservice.entities.LayoutTemplate;
import org.hexaware.busservice.enums.SeatType;
import org.hexaware.busservice.repositories.BusTemplateRepository;
import org.hexaware.busservice.services.BusTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BusTemplateServiceImpl implements BusTemplateService {

    // Initialize the ObjectMapper instance
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private BusTemplateRepository busTemplateRepository;

    @Override
    public String generateLayoutData(LayoutTemplate bluePrint, int totalSeats) {
        List<SeatDetail> seatDetails = new ArrayList<>();
        int seatsCreated = 0;
        int currentRow = 0;

        // OUTER LOOP: Create rows until the total seat capacity is reached
        while (seatsCreated < totalSeats) {

            // INNER LOOP: Iterate through columns defined in the blueprint
            for (int col = 0; col < bluePrint.getColumns(); col++) {

                // 1. Handle Walkway (Aisle) Logic
                if (col == bluePrint.getAisleAfterColumn()) {
                    seatDetails.add(new SeatDetail(
                            "WALKWAY-" + currentRow + "-" + col,
                            SeatType.WALKWAY,
                            false,
                            col,       // x_coordinate
                            currentRow // y_coordinate
                    ));
                }
                // 2. Handle Seat creation (Only if we still need more seats)
                else if (seatsCreated < totalSeats) {
                    seatsCreated++;

                    // Generate alphabetical label (e.g., 1A, 1B, 1C)
                    String label = generateSeatLabel(currentRow, col, bluePrint.getAisleAfterColumn());

                    // Window calculation: True if it's the first or last column
                    boolean isWindow = (col == 0 || col == bluePrint.getColumns() - 1);

                    // Determine if the icon should be a SEATER or SLEEPER
                    SeatType seatType = determineSeatType(bluePrint);

                    seatDetails.add(new SeatDetail(
                            label,
                            seatType,
                            isWindow,
                            col,       // x_coordinate
                            currentRow // y_coordinate
                    ));
                }
            }
            // Move to the next row index after finishing all columns in the current row
            currentRow++;
        }

        // Convert the list of objects into a JSON string for the DB
        try {
            return objectMapper.writeValueAsString(seatDetails);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error generating layout JSON", e);
        }
    }

    @Override
    public List<BusTemplate> fetchBusTemplates(UUID companyId) {
        var busTemplate = (List<BusTemplate>) busTemplateRepository.findByCompany_CompanyId(companyId);
        return busTemplate;
    }

    private String generateSeatLabel(int row, int col, int aisleIndex) {
        // Shift column index if past the aisle so letters remain sequential (A, B, C, D)
        int letterIndex = (col > aisleIndex) ? col - 1 : col;
        char seatLetter = (char) ('A' + letterIndex);
        return (row + 1) + String.valueOf(seatLetter);
    }

    private SeatType determineSeatType(LayoutTemplate bluePrint) {
        // Check blueprint default type to determine seat icons
        if (bluePrint.getDefaultType() != null &&
                bluePrint.getDefaultType().name().contains("SLEEPER")) {
            return SeatType.SLEEPER;
        }
        return SeatType.SEATER;
    }
}