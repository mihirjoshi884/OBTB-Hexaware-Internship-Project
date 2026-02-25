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

        // 1. Determine how many decks and seats per deck
        int decks = (bluePrint.getIsDoubleDecker() != null && bluePrint.getIsDoubleDecker()) ? 2 : 1;

        // For odd numbers, the lower deck usually takes the extra seat
        int seatsPerDeck = totalSeats / decks;
        int remainingSeats = totalSeats;

        for (int d = 0; d < decks; d++) {
            int currentRow = 0;
            int seatsInThisDeck = 0;

            // Calculate target for this specific deck
            int targetForDeck = (decks == 2 && d == 0) ? (totalSeats - seatsPerDeck) : seatsPerDeck;

            while (seatsInThisDeck < targetForDeck) {
                for (int col = 0; col < bluePrint.getColumns(); col++) {

                    // 1. Walkway Logic (X and Y coordinates stay the same, but we add Deck)
                    if (col == bluePrint.getAisleAfterColumn()) {
                        seatDetails.add(new SeatDetail(
                                "WALKWAY-" + d + "-" + currentRow + "-" + col,
                                SeatType.WALKWAY,
                                false,
                                col,
                                currentRow,
                                d  // <--- Pass deck index (0 or 1)
                        ));
                    }
                    // 2. Seat Logic
                    else if (seatsInThisDeck < targetForDeck) {
                        seatsInThisDeck++;

                        // Prefix label: L for Lower (0), U for Upper (1)
                        String prefix = (decks == 2) ? (d == 0 ? "L" : "U") : "";
                        String label = prefix + generateSeatLabel(currentRow, col, bluePrint.getAisleAfterColumn());

                        boolean isWindow = (col == 0 || col == bluePrint.getColumns() - 1);
                        SeatType seatType = determineSeatType(bluePrint);

                        seatDetails.add(new SeatDetail(
                                label,
                                seatType,
                                isWindow,
                                col,
                                currentRow,
                                d // <--- Pass deck index
                        ));
                    }
                }
                currentRow++;
            }
        }

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