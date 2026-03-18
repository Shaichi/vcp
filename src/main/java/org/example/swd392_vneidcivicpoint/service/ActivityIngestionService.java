package org.example.swd392_vneidcivicpoint.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.swd392_vneidcivicpoint.constants.IngestionStatus;
import org.example.swd392_vneidcivicpoint.dto.ActivityIngestionRequest;
import org.example.swd392_vneidcivicpoint.dto.ActivityIngestionResponse;
import org.example.swd392_vneidcivicpoint.entity.ActivityLog;
import org.example.swd392_vneidcivicpoint.entity.Citizen;
import org.example.swd392_vneidcivicpoint.repository.ActivityLogRepository;
import org.example.swd392_vneidcivicpoint.repository.CitizenRepository;
import org.example.swd392_vneidcivicpoint.util.FiscalYearUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ActivityIngestionService {

    private final ActivityLogRepository activityLogRepository;
    private final CitizenRepository citizenRepository;
    private final PointEngineService pointEngineService;

    @Transactional
    public ActivityIngestionResponse ingestActivity(ActivityIngestionRequest request) {
        
        ActivityIngestionResponse.ActivityIngestionResponseBuilder responseBuilder = 
                ActivityIngestionResponse.builder()
                .externalReference(request.getExternalReference());

        // 1. Validate Duplicate
        Optional<ActivityLog> existingLog = activityLogRepository.findByExternalReferenceAndPartnerId(
                request.getExternalReference(), request.getPartnerId());
        
        if (existingLog.isPresent()) {
            return responseBuilder
                    .status(IngestionStatus.DUPLICATE)
                    .message("Activity already ingested and processed.")
                    .logId(existingLog.get().getLogId())
                    .build();
        }

        // 2. Validate Citizen
        Optional<Citizen> citizenOpt = citizenRepository.findByCccdNumber(request.getCccdNumber());
        if (citizenOpt.isEmpty()) {
            return responseBuilder
                    .status(IngestionStatus.REJECTED_NOT_FOUND)
                    .message("Citizen associated with CCCD not found or not opted-in.")
                    .build();
        }
        
        Citizen citizen = citizenOpt.get();
        
        // 3. Create Activity Log
        ActivityLog log = new ActivityLog();
        log.setCitizen(citizen);
        log.setActivityCode(request.getActivityCode());
        log.setSourceSystem(request.getSourceSystem());
        log.setExternalReference(request.getExternalReference());
        log.setPartnerId(request.getPartnerId());
        log.setActivityDate(LocalDateTime.now());
        log.setFiscalYear(FiscalYearUtil.getCurrentFiscalYear());
        log.setIngestionStatus(IngestionStatus.VALID);
        log.setForwardedToEngine(true);
        
        log = activityLogRepository.save(log);
        
        // 4. Trigger Point Engine asynchronously (Synchronous here for demo simplicity)
        pointEngineService.processActivity(log);
        
        return responseBuilder
                .status(IngestionStatus.VALID)
                .message("Activity ingested and points calculation triggered.")
                .logId(log.getLogId())
                .build();
    }
}
