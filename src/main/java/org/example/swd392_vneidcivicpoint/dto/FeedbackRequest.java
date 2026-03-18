package org.example.swd392_vneidcivicpoint.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FeedbackRequest {
    @NotBlank
    private String category;
    
    @NotBlank
    private String title;
    
    @NotBlank
    private String description;
}
