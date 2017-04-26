package org.bookmarks.website.domain;

public enum Binding {
	PAPERBACK("Paperback"), 
	HARDBACK("Hardback"),
    KINDLE("Kindle"),
	DVD("DVD"),
	CD("CD"),
	OTHER("Other");
	
	
	private final String displayName;

    private Binding(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
