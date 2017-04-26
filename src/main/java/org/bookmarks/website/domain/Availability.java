package org.bookmarks.website.domain;

public enum Availability {
	OUT_OF_PRINT("Out of print"),
	PUBLISHED("Published"),
	NOT_YET_PUBLISHED("Not yet published"),
	AVAILABLE_NEW_FROM_THIRD_PARTY("Available new from third party");
	
	private final String displayName;

    private Availability(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }	
}
