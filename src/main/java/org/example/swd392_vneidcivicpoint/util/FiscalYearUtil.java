package org.example.swd392_vneidcivicpoint.util;

import java.time.LocalDate;

public class FiscalYearUtil {
    
    // In Vietnam, the fiscal year generally aligns with the calendar year
    // but we encapsulate it here in case the logic needs to change.
    public static int getCurrentFiscalYear() {
        return LocalDate.now().getYear();
    }
    
    public static int getFiscalYearFromDate(LocalDate date) {
        return date.getYear();
    }
}
