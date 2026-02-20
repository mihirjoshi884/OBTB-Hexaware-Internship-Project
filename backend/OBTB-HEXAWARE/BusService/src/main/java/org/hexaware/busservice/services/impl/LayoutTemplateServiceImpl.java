package org.hexaware.busservice.services.impl;

import org.hexaware.busservice.dtos.LayoutLookupResponse;
import org.hexaware.busservice.dtos.ResponseDto;
import org.hexaware.busservice.repositories.LayoutRepository;
import org.hexaware.busservice.services.LayoutTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LayoutTemplateServiceImpl implements LayoutTemplateService {

    @Autowired
    private LayoutRepository layoutRepository;

    @Override
    public ResponseDto<List<LayoutLookupResponse>> getLayoutTemplates() {
        // 1. Fetch all layout templates from the database
        var templates = layoutRepository.findAll();

        // 2. Transform the entities into a list of clean Lookup DTOs
        List<LayoutLookupResponse> result = templates.stream()
                .map(template -> new LayoutLookupResponse(
                        template.getLayoutId(),
                        template.getName(),
                        template.getDescription()
                ))
                .toList();

        // 3. Wrap in the generic ResponseDto for consistent API structure
        return new ResponseDto<>(result, 200, "Layout templates fetched successfully");
    }
}
