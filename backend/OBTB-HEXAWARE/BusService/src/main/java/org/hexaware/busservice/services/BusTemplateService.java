package org.hexaware.busservice.services;

import org.hexaware.busservice.entities.LayoutTemplate;

public interface BusTemplateService {

    public String generateLayoutData(LayoutTemplate bluePrint, int totalSeats);
}
