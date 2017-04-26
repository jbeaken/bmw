package org.bookmarks.website.domain;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * @author hal
 *
 */
//@Entity
public class ReadingListStockItem extends AbstractEntity {

	@ManyToOne
	@JoinColumn(name="stockitem_id")
	private StockItem stockItem;
	
	@ManyToOne
	@JoinColumn(name="readinglist_id")
	private ReadingList readingList;
	
	private Long position;

	public StockItem getStockItem() {
		return stockItem;
	}

	public void setStockItem(StockItem stockItem) {
		this.stockItem = stockItem;
	}

	public ReadingList getReadingList() {
		return readingList;
	}

	public void setReadingList(ReadingList readingList) {
		this.readingList = readingList;
	}

	public Long getPosition() {
		return position;
	}

	public void setPosition(Long position) {
		this.position = position;
	}

	
}
