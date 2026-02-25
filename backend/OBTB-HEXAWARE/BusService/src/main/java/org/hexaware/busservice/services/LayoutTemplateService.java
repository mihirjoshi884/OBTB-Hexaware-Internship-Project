package org.hexaware.busservice.services;

import org.hexaware.busservice.dtos.busDtos.LayoutLookupResponse;
import org.hexaware.busservice.dtos.ResponseDto;

import java.util.List;

public interface LayoutTemplateService {
    public ResponseDto<List<LayoutLookupResponse>> getLayoutTemplates();
}
