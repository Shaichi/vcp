package org.example.swd392_vneidcivicpoint.constants;

public enum IngestionStatus {
    VALID,
    DUPLICATE,
    REJECTED_NOT_FOUND,
    REJECTED_SUSPENDED,
    REJECTED_UNMAPPED,
    REJECTED_RATE_LIMIT,
    REJECTED_INVALID_DATA
}
