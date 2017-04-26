package org.bookmarks.website.search;

import org.bookmarks.website.domain.Author;
import org.bookmarks.website.domain.Category;
import org.bookmarks.website.domain.Publisher;
import org.bookmarks.website.domain.ReadingList;
import org.bookmarks.website.domain.StockItem;
import org.bookmarks.website.domain.StockItemType;


public class StockItemSearchBean extends SearchBean<StockItem> {
	
	private Author author;
	
	private Category category;
	
	private Publisher publisher;
	
	private ReadingList readingList;
	
	private StockItemType type;
	
	private String q;

	public String getQ() {
		return q;
	}

	public void setQ(String q) {
		this.q = q;
	}

	public ReadingList getReadingList() {
		return readingList;
	}

	public void setReadingList(ReadingList readingList) {
		this.readingList = readingList;
	}

	public Author getAuthor() {
		return author;
	}

	public void setAuthor(Author author) {
		this.author = author;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public Publisher getPublisher() {
		return publisher;
	}

	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
	}

	public StockItemType getType() {
		return type;
	}

	public void setType(StockItemType type) {
		this.type = type;
	}
}
