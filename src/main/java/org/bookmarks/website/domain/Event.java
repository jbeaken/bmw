package org.bookmarks.website.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name="event")
public class Event extends AbstractNamedEntity {

	@NotNull
	@DateTimeFormat(pattern="dd/MM/yy")
	@Column(name="startDate")
	private Date startDate;

	@Column(columnDefinition="text")
	private String description;

	private String startTime;

	private String endTime;

	@NotNull
	@Column(name="show_author")
	private Boolean showAuthor;

	@Column(name="show_bookmarks_address")
	private Boolean showBookmarksAddress = true;

	@Column(name="show_name_not_stock_title")
	private Boolean showName;

	@Column(name="entrance_price")
	private Float entrancePrice;

	@NotNull
	@DateTimeFormat(pattern="dd/MM/yy")
	@Column(name="endDate")
	private Date endDate;

	@ManyToOne
	@JoinColumn(name="stockitem_id")
	private StockItem stockItem;

	//Constructors
	public Event() {
		super();
	}


	public StockItem getStockItem() {
		return stockItem;
	}

	public void setStockItem(StockItem stockItem) {
		this.stockItem = stockItem;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public Boolean getShowName() {
		return showName;
	}

	public void setShowName(Boolean showName) {
		this.showName = showName;
	}

	public Boolean getShowAuthor() {
		return showAuthor;
	}

	public void setShowAuthor(Boolean showAuthor) {
		this.showAuthor = showAuthor;
	}

	public Boolean getShowBookmarksAddress() {
		return showBookmarksAddress;
	}

	public void setShowBookmarksAddress(Boolean showBookmarksAddress) {
		this.showBookmarksAddress = showBookmarksAddress;
	}

	public Float getEntrancePrice() {
		return entrancePrice;
	}


	public void setEntrancePrice(Float entrancePrice) {
		this.entrancePrice = entrancePrice;
	}
}
