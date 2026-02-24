package org.hexaware.busservice.services;

import org.hexaware.busservice.entities.BusTemplate;
import org.hexaware.busservice.entities.LayoutTemplate;

import java.util.List;
import java.util.UUID;

public interface BusTemplateService {

    public String generateLayoutData(LayoutTemplate bluePrint, int totalSeats);
    public List<BusTemplate> fetchBusTemplates(UUID companyId);
}
