package org.bookmarks.website.domain;

public enum DeliveryType {
	MAIL("Mail Order"), COLLECTION("Collection"), SPONSORSHIP("Sponsorship");

	private final String displayName;

    private DeliveryType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
