package org.bookmarks.website.domain;

import java.io.Serializable;

public enum OrderLineStatus implements Serializable{
	ON_ORDER("On Order"),
	CANCELLED("Cancelled"),
	AWAITING_CUSTOMER_FEEDBACK("Awaiting Customer Feedback"),
	READY_TO_POST("Ready to post"),
	AWAITING_COLLECTION("Awaiting collection"), 
	COMPLETE("Complete");

	private final String displayName;

    private OrderLineStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
