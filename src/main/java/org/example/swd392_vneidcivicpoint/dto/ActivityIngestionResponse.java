package org.example.swd392_vneidcivicpoint.dto;

import lombok.Builder;
import lombok.Data;
import org.example.swd392_vneidcivicpoint.constants.IngestionStatus;

@Data
@Builder
public class ActivityIngestionResponse {
    private String externalReference;
    private IngestionStatus status;
    private String message;
    private Long logId;
}
