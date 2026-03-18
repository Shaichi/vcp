package org.example.swd392_vneidcivicpoint.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.swd392_vneidcivicpoint.dto.ActivityIngestionRequest;
import org.example.swd392_vneidcivicpoint.dto.ActivityIngestionResponse;
import org.example.swd392_vneidcivicpoint.service.ActivityIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ingestion")
@RequiredArgsConstructor
public class ActivityIngestionController {

    private final ActivityIngestionService ingestionService;

    @PostMapping("/activities")
    public ResponseEntity<ActivityIngestionResponse> ingestActivity(@Valid @RequestBody ActivityIngestionRequest request) {
        ActivityIngestionResponse response = ingestionService.ingestActivity(request);
        return ResponseEntity.ok(response);
    }
}
