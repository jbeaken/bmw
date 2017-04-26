package org.bookmarks.website.domain;

import java.util.List;

public class Layout {
	
	private List<StockItem> bouncies;

	private List<Event> upcomingEvents;
	
	private List<StockItem> merchandise;
	
	public List<StockItem> getBouncies() {
		return bouncies;
	}

	public void setBouncies(List<StockItem> bouncies) {
		this.bouncies = bouncies;
	}

	public List<Event> getUpcomingEvents() {
		return upcomingEvents;
	}

	public void setUpcomingEvents(List<Event> upcomingEvents) {
		this.upcomingEvents = upcomingEvents;
	}

	public List<StockItem> getMerchandise() {
		return merchandise;
	}

	public void setMerchandise(List<StockItem> merchandise) {
		this.merchandise = merchandise;
	}
}
