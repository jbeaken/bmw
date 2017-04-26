package org.bookmarks.website.domain;

public enum PaymentType {
	CASH("Cash"),
	PAID("Paid"),
	CREDIT_CARD("Credit Card"),
	ACCOUNT("Account"),
	INSTITUTION("Institution"),
	CHEQUE("Cheque");

	private final String displayName;

    private PaymentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
