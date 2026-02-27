package org.hexaware.busservice.dtos.documentDtos;

public record BusDocumentResponse (
        String docId,
        String docName,
        String url
){ }
