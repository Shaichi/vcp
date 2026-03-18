package org.example.swd392_vneidcivicpoint.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActivityIngestionRequest {
    @NotBlank
    private String cccdNumber;
    
    @NotBlank
    private String activityCode;
    
    @NotBlank
    private String sourceSystem;
    
    @NotBlank
    private String externalReference;
    
    // Optional
    private String partnerId;
}
