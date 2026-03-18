package org.example.swd392_vneidcivicpoint.constants;

public enum TransactionType {
    EARNED("Tích lũy"),
    BONUS_VULNERABLE("Thưởng đối tượng yếu thế"),
    ADJUSTMENT("Điều chỉnh trực tiếp"),
    YEAR_END_CARRYOVER("Chuyển nguồn cuối năm");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
