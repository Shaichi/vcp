package org.example.swd392_vneidcivicpoint.constants;

public enum ActivityCategory {
    IDENTITY("Định danh & Hồ sơ"),
    SERVICE("Dịch vụ công"),
    FINANCIAL("Giao dịch tài chính"),
    HEALTHCARE("Y tế"),
    EDUCATION("Giáo dục & Khuyến học"),
    CIVIC("Hoạt động cộng đồng");

    private final String displayName;

    ActivityCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
