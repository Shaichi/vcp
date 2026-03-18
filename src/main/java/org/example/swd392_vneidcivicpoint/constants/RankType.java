package org.example.swd392_vneidcivicpoint.constants;

public enum RankType {
    ACTIVE("Hạng Tích cực"),
    BASIC("Hạng Cơ bản"),
    UNRANKED("Chưa xếp hạng");

    private final String displayName;

    RankType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
