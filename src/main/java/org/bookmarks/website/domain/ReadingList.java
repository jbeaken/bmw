package org.bookmarks.website.domain;


import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.FetchType;
import javax.persistence.CascadeType;
import javax.persistence.OrderColumn;


@Entity
public class ReadingList extends AbstractNamedEntity {

	@ManyToMany(fetch=FetchType.EAGER)
	@OrderColumn(name="stockItem_idx")
	private List<StockItem> stockItems;

	@Column(name="is_on_website") private Boolean isOnWebsite = Boolean.TRUE;
	@Column(name="is_on_sidebar") private Boolean isOnSidebar = Boolean.FALSE;

	//Constructors
	public ReadingList() {
		super();
	}

	public ReadingList(Long id) {
		this();
		setId(id);
	}
	public ReadingList(Long id, String name) {
		this(id);
		setName(name);
	}

	//Accesors
/*	public List<StockItem> getStockItems() {
		return stockItems;
	}

	public void setStockItems(List<StockItem> stockItems) {
		this.stockItems = stockItems;
	}*/

	public Boolean getIsOnWebsite() {
		return isOnWebsite;
	}

	public void setIsOnWebsite(Boolean isOnWebsite) {
		this.isOnWebsite = isOnWebsite;
	}

	public Boolean getIsOnSidebar() {
		return isOnSidebar;
	}

	public void setIsOnSidebar(Boolean isOnSidebar) {
		this.isOnSidebar = isOnSidebar;
	}

	public List<StockItem> getStockItems() {
		return stockItems;
	}

	public void setStockItems(List<StockItem> stockItems) {
		this.stockItems = stockItems;
	}

}
