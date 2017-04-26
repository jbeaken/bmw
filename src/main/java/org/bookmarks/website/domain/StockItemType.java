package org.bookmarks.website.domain;

public enum StockItemType {
	BOOK("Book"),
	ARTWORK("Art Work"),
	BADGE("Badge"),
	BAG("Bag"),
	BOOKMARK("Bookmark"),
	BUST("Bust"),
	CALENDAR("Calendar"),
	CARD("Card"),
	CD("CD"),
	COASTER("Coaster"),
	DIARY("Diary"),
	DVD("DVD"),
	EDIBLE("Edible"),
	FRIDGE_MAGNET("Fridge Magnet"),
	GAME("Game"),
	MISC("Misc"),	
	MUG("Mug"),
	MARXISM_CD("Marxism CD"),
	NEWSPAPER("Newspaper"),
	PAMPHLET("Pamphlet"),
	POSTCARD("Postcard"),
	POSTER("Poster"),
	TEESHIRT("T-Shirt"),
	TEA_TOWEL("Tea Towel");

	private final String displayName; 

    private StockItemType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
