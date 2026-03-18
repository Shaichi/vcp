package org.example.swd392_vneidcivicpoint.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class TrackingIdGenerator {
    
    public static String generateFeedbackTrackingId() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        String uuidPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "FB-" + timestamp + "-" + uuidPart;
    }
}
