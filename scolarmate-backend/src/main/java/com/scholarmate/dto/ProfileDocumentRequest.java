package com.scholarmate.dto;
public record ProfileDocumentRequest(String documentType, String fileName, String contentType, Long sizeBytes, String storageKey, String status, String extractedData, String expiresAt) { }
