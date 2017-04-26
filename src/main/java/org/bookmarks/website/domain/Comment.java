package org.bookmarks.website.domain;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;

@Entity
public class Comment extends AbstractEntity {

    @NotNull
    @Size(max = 255)
    @Column(unique = true)
    @Field(index=Index.YES, store=Store.YES)
    private String name;
    
    @ContainedIn //TO-DO what's this doing here?
    @ManyToMany(mappedBy="authors")
    private Set<StockItem> stockItems;

    //Constructos
    public Comment() {
    	super();
    }
    public Comment(Long id, String name) {
    	this();
    	this.id = id;
    	this.name = name;
    }
    
    //Accessors/Modifiers
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<StockItem> getStockItems() {
		return stockItems;
	}

	public void setStockItems(Set<StockItem> stockItems) {
		this.stockItems = stockItems;
	} 
}
